from fastapi import FastAPI, UploadFile, File, HTTPException
import os
import shutil
from pathlib import Path
import uvicorn
import uuid

from infer_remaster_text import (
    load_asr_pipeline, 
    load_audio, 
    refine_text_with_llm, 
    DEFAULT_MODEL_PATH, 
    DEFAULT_LLM_MODEL
)

app = FastAPI()

asr_pipe = None

@app.on_event("startup")
def load_models():
    global asr_pipe
    print(" [Malbit AI] 모델 로딩 시작...")
    asr_pipe = load_asr_pipeline(DEFAULT_MODEL_PATH)
    print(" [Malbit AI] 로딩 완료!")

@app.get("/")
async def root():
    return {"message": "Malbit AI Server is Running"}

@app.post("/analyze")
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

        # 오디오 로드 및 STT 수행
        audio_array = load_audio(str(temp_file), target_sr=16000)

        result = asr_pipe(
            {"array": audio_array, "sampling_rate": 16000},
            generate_kwargs={
                "repetition_penalty": 1.1,
                "no_repeat_ngram_size": 3,
                "language": "ko",
                "task": "transcribe",
                "return_timestamps": False
            }
        )

        raw_text = result["text"].strip() if isinstance(result, dict) else str(result).strip()

        # Claude 3 보정 (Bedrock)
        refined_text = refine_text_with_llm(raw_text, DEFAULT_LLM_MODEL)

        print(f"--- AI 최종 응답 (ID: {unique_id}) ---")
        print(f"raw: {raw_text}")
        print(f"refined: {refined_text}")

        return {
            "status": "success",
            "raw_text": raw_text,
            "refined_text": refined_text
        }
    
    except Exception as e:
        print(f"Error during analysis: {e}")
        raise HTTPException(status_code=500, detail=str(e))
    
    finally:
        # 사용한 임시 파일 삭제
        if temp_file.exists():
            temp_file.unlink()
            print(f"임시 파일 삭제 완료: {temp_file}")

if __name__ == "__main__": 
    uvicorn.run(app, host="0.0.0.0", port=8000)