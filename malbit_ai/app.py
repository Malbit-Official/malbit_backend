from fastapi import FastAPI, UploadFile, File, HTTPException, Header
import uvicorn
import uuid
from datetime import datetime
from pydantic import BaseModel

# 우리가 정리한 모듈들 import
from core.config import Config
from core.stt_engine import load_asr_pipeline
from services.remaster_service import run_remastering
from services.meeting_service import summarize_meeting_and_schedule
from services.speech_service import suggest_speech_by_situation
from services.calendar_service import sync_schedules_to_backend

app = FastAPI(title="Malbit AI Server")

# 전역 변수로 Whisper 파이프라인 관리
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
async def analyze_voice(file: UploadFile = File(...)):
    unique_id = uuid.uuid4().hex[:8]
    temp_path = f"temp_{unique_id}_{file.filename}"

    try:
        with open(temp_path, "wb") as buffer:
            buffer.write(await file.read())

        # remaster_service 호출
        result = run_remastering(asr_pipe, temp_path)

        return {
            "status": "SUCCESS",
            "data": { "log_id": unique_id, **result }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        import os
        if os.path.exists(temp_path): os.remove(temp_path)

# 회의록 요약 및 캘린더 동기화 엔드포인트
@app.post("/api/analyze-meeting")
async def analyze_meeting(file: UploadFile = File(...), authorization: str = Header(None)):
    unique_id = uuid.uuid4().hex[:8]
    temp_path = f"meeting_{unique_id}_{file.filename}"

    try:
        with open(temp_path, "wb") as buffer:
            buffer.write(await file.read())

        # meeting_service 로직 수행
        current_date = datetime.now().strftime("%Y-%m-%d (%A)")
        analysis_result = summarize_meeting_and_schedule(temp_path, current_date, asr_pipe)

        if authorization and analysis_result.get("schedules"):
            await sync_schedules_to_backend(analysis_result["schedules"], authorization)

        return {"status": "SUCCESS", "data": analysis_result}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        import os
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