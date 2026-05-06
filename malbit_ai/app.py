from fastapi import FastAPI, UploadFile, File, HTTPException,Header, Request
import os
import shutil
from pathlib import Path
import uvicorn
import uuid
from datetime import datetime
from dotenv import load_dotenv

load_dotenv()

from processor.stt_engine import load_asr_pipeline, transcribe_audio
from processor.llm_handler import refine_text_with_llm
from processor.llm_handler import summarize_meeting_and_schedule
from processor.calendar_sync import sync_schedules_to_backend

# 설정값 로드
from processor.infer_remaster_text import (
    DEFAULT_MODEL_PATH, 
    SUB_FOLDER,
    DEFAULT_LLM_MODEL
)

app = FastAPI()

asr_pipe = None

@app.on_event("startup")
def load_models():
    global asr_pipe
    print(" [Malbit AI] 모델 로딩 시작...")
    asr_pipe = load_asr_pipeline(DEFAULT_MODEL_PATH, SUB_FOLDER)
    print(" [Malbit AI] 로딩 완료!")

@app.get("/")
async def root():
    return {"message": "Malbit AI Server is Running"}

# 단문 리마스터링 엔드포인트
@app.post("/api/analyze")
async def analyze_voice(file: UploadFile = File(...)):
    # 고유 파일 저장
    unique_id = uuid.uuid4().hex[:8]
    temp_file = Path(f"temp_{unique_id}_{file.filename}")

    print(f"수신된 파일명: {file.filename} -> 저장명: {temp_file}")

    try:
        # 파일 스트림의 위치를 처음으로 되돌림 
        await file.seek(0)

        # 업로드된 파일 저장
        with temp_file.open("wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # 파일이 제대로 저장되었는지 크기 확인 로그
        file_size = os.path.getsize(temp_file)
        print(f"파일 저장 완료 (크기: {file_size} bytes)")

        if file_size == 0:
            raise ValueError("수신된 파일이 비어있습니다 (0 bytes).")
        
        # 분리된 STT 엔진 호출
        raw_text = transcribe_audio(asr_pipe, str(temp_file))

        # Bedrock 보정 추출
        refined_text = refine_text_with_llm(raw_text, DEFAULT_LLM_MODEL)

        print(f"--- AI 최종 응답 (ID: {unique_id}) ---")
        print(f"raw: {raw_text}")
        print(f"refined: {refined_text}")

        return {
            "status": "SUCCESS",
            "data": {
                "log_id": unique_id,
                "raw_text": raw_text,
                "refined_text": refined_text
            }
        }
    
    except Exception as e:
        print(f"Error during analysis: {e}")
        raise HTTPException(status_code=500, detail=str(e))
    
    finally:
        # 사용한 임시 파일 삭제
        if temp_file.exists():
            temp_file.unlink()
            print(f"임시 파일 삭제 완료: {temp_file}")

# 회의록 STT 및 요약 엔드포인트
@app.post("/api/analyze-meeting")
async def analyze_meeting(
    file: UploadFile = File(...),
    authorization: str = Header(None)
    ):
    unique_id = uuid.uuid4().hex[:8]
    temp_file = Path(f"meeting_{unique_id}_{file.filename}")

    try:
        await file.seek(0)
        with temp_file.open("wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # STT 수행 (음성 -> 텍스트)
        raw_text = transcribe_audio(asr_pipe, str(temp_file))

        # 현재 날짜 정보 생성
        current_date_str = datetime.now().strftime("%Y-%m-%d (%A)")

        # LMM 분석 수행
        analysis_result = summarize_meeting_and_schedule(
            raw_text=raw_text, 
            current_date=current_date_str, 
            llm_model=DEFAULT_LLM_MODEL
        )

        # 회의록 요약 
        print(f" [Meeting STT 완료] ID: {unique_id}, 길이: {len(raw_text)}")

        # 캘린더 API로 전송
        if analysis_result.get("schedules"):
            # 헤더에 토큰이 있는지 확인
            if not authorization:
                print(" [Warning] 인증 토큰이 없어 캘린더 동기화를 건너뜁니다.")
            else:
                await sync_schedules_to_backend(analysis_result["schedules"], authorization)

        return {
            "status": "SUCCESS",
            "data": {
                "meeting_id": unique_id,
                "raw_text": raw_text,
                "summary": analysis_result.get("summary_text", ""), 
                "checklists": analysis_result.get("checklists", []),
                "schedules": analysis_result.get("schedules", [])
            }
        }

    except Exception as e:
        print(f" [Meeting Error] {e}")
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        if temp_file.exists():
            temp_file.unlink()
            print(f" 임시 파일 삭제 완료: {temp_file}")

if __name__ == "__main__": 
    uvicorn.run(app, host="0.0.0.0", port=8000)