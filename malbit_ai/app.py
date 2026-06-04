from fastapi import FastAPI, UploadFile, File, HTTPException, Header, Form
import uvicorn
import uuid
import os
from datetime import datetime
from pydantic import BaseModel

from core.config import Config
from core.stt_engine import load_asr_pipeline
from services.remaster_service import run_remastering
from services.meeting_service import summarize_meeting_and_schedule
from services.calendar_service import sync_schedules_to_backend
from services.speech_service import suggest_speech_by_situation

from fastapi.concurrency import run_in_threadpool

app = FastAPI(title="Malbit AI Server")
asr_pipe = None

# 서버 시작 시 모델 로드
@app.on_event("startup")
def startup_event():
    global asr_pipe
    print(f" [Malbit AI] 모델 로딩 시작... (Path: {Config.DEFAULT_MODEL_PATH})")
    asr_pipe = load_asr_pipeline()
    print(" [Malbit AI] 모든 모델 로딩 완료 및 서비스 준비 완료!")

@app.get("/")
async def root():
    return {"message": "Malbit AI Server is running", "status": "ONLINE"}

# 단문 리마스터링 엔드포인트
@app.post("/api/analyze")
async def analyze_voice(
    file: UploadFile = File(...),
    ground_truth: str = Form(None)
):
    unique_id = uuid.uuid4().hex[:8]
    temp_path = f"temp_{unique_id}_{file.filename}"
    print(f"unique_id = {unique_id}\n temp_path = {temp_path}")

    try:
        with open(temp_path, "wb") as buffer:
            buffer.write(await file.read())

        #ground_truth = "오늘 점심 약을 까먹지 말고 꼭 먹어야 합니다"
        #ground_truth = "선생님 이따가 이메일로 과제 보낼 테니까 확인해 주세요"
        #ground_truth = "아 진짜 짜증 나고 답답하니까 그냥 다 때려치우고 싶어"
            
        result = await run_in_threadpool(run_remastering, asr_pipe, temp_path, ground_truth=ground_truth)
        return {
            "status": "SUCCESS",
            "data": { "log_id": unique_id, **result }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if os.path.exists(temp_path): os.remove(temp_path)

# AI 성능 지표 측정 엔드포인트 
@app.post("/api/test-metrics")
async def test_metrics(
    file: UploadFile = File(...),
    ground_truth: str = Form(...)  
):
    unique_id = "test_" + uuid.uuid4().hex[:8]
    temp_path = f"temp_{unique_id}_{file.filename}"
    print(f" \n[Metrics Test] 요청 접수됨 | ID: {unique_id}")
    print(f" [Metrics Test] 목표 정답 문장: {ground_truth}")

    try:
        with open(temp_path, "wb") as buffer:
            buffer.write(await file.read())
            
        result = await run_in_threadpool(run_remastering, asr_pipe, temp_path, ground_truth=ground_truth)
        
        return {
            "status": "SUCCESS",
            "message": "AI 성능 지표가 서버 터미널(logs)에 성공적으로 출력되었습니다.",
            "data": {
                "test_id": unique_id,
                "ground_truth": ground_truth,
                **result  
            }
        }
    except Exception as e:
        print(f" [Metrics Test Error] {e}")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if os.path.exists(temp_path): os.remove(temp_path)        

# 회의록 요약 및 캘린더 동기화 엔드포인트
@app.post("/api/analyze-meeting")
async def analyze_meeting(
    file: UploadFile = File(...), 
    authorization: str = Header(None),
    ground_truth: str = Form(None)
):
    unique_id = uuid.uuid4().hex[:8]
    temp_path = f"meeting_{unique_id}_{file.filename}"

    try:
        with open(temp_path, "wb") as buffer:
            buffer.write(await file.read())

        current_date = datetime.now().strftime("%Y-%m-%d (%A)")
        analysis_result = await run_in_threadpool(
            summarize_meeting_and_schedule, temp_path, current_date, asr_pipe, ground_truth=ground_truth
        )

        print(f"\n==================== [WORKPLACE AI ANALYSIS LOG] ====================")
        print(f" [분석 로그 ID] : {unique_id}")
        print(f" [기준 날짜 (Ref)] : {current_date}")
        print(f" ---------------------------------------------------------------------")
        
        # STT 받아쓰기 원문 검증 (Whisper가 노이즈/오타를 얼마나 출력했는지 확인)
        raw_transcript = analysis_result.get("raw_text") or analysis_result.get("transcript") or "텍스트를 추출할 수 없습니다."
        print(f" [Whisper STT 원문 변환 결과] :\n \"{raw_transcript}\"")
        print(f" ---------------------------------------------------------------------")
        
        # LLM 3줄 요약 결과 검증
        print(f" [Claude 핵심 업무 요약 (Summary)] :\n {analysis_result.get('summary_text')}")
        print(f" ---------------------------------------------------------------------")
        
        # 단기 체크리스트 검증
        print(f" [즉각 수행할 체크리스트] : {analysis_result.get('checklists')}")
        print(f" ---------------------------------------------------------------------")
        
        # 캘린더 연동용 구조화 일정 검증 (상대 시간 -> 절대 시간 변환 체크)
        print(f" [API 연동용 구조화 일정 추출 (Schedules)] :")
        schedules = analysis_result.get('schedules', [])
        if not schedules:
            print("    ❌ 추출된 특정 날짜/시간 일정이 없습니다.")
        for idx, sched in enumerate(schedules, 1):
            print(f"    [{idx}] 카테고리: {sched.get('category')} | 중요도: {sched.get('importance')}")
            print(f"        📌 할 일: {sched.get('title')}")
            print(f"        ⏰ 기한: {sched.get('date')} ({sched.get('time')})")
        print(f"======================================================================\n")

        if authorization and analysis_result.get("schedules"):
            await sync_schedules_to_backend(analysis_result["schedules"], authorization)

        return {"status": "SUCCESS", "data": analysis_result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if os.path.exists(temp_path): os.remove(temp_path)

# 상황별 발화 추천 엔드포인트
class SuggestionRequest(BaseModel):
    category: str
    user_input: str = None

@app.post("/api/suggest-speech")
async def suggest_speech(req: SuggestionRequest):
    try:
        print(f" [Malbit AI] 발화 추천 요청: {req.category}")
        result = suggest_speech_by_situation(req.category, req.user_input)
        return {"status": "SUCCESS", "data": result}
    except Exception as e:
        print(f" [Error] {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=5000)