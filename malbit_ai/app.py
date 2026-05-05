from fastapi import FastAPI, UploadFile, File, HTTPException
import os
import shutil
from pathlib import Path

from infer_remaster_text import (
    load_asr_pipeline, 
    load_audio, 
    refine_text_with_llm, 
    DEFAULT_MODEL_PATH, 
    DEFAULT_LLM_MODEL
)

app = FastAPI()

# 서버 시작 시 모델을 로드
print("AI 모델 로딩 중...")
asr_pipe = load_asr_pipeline(DEFAULT_MODEL_PATH)
print("모델 로딩 완료!")

@app.post("/analyze")
async def analyze_voice(file: UploadFile = File(...)):
    # 임시 파일 경로
    temp_file = Path(f"temp_{file.filename}")

    try:
        # 업로드된 파일 저장
        with temp_file.open("wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # 오디오 로드 및 STT 수행
        audio_array = load_audio(str(temp_file), target_sr=16000)

        result = asr_pipe(
            {"array": audio_array, "sampling_rate": 16000},
            generate_kwargs={
                "repetition_penalty": 1.5,
                "language": "ko",
                "task": "transcribe"
            }
        )

        raw_text = result["text"].strip() if isinstance(result, dict) else str(result).strip()

        # Claude 3 보정 (Bedrock)
        refined_text = refine_text_with_llm(raw_text, DEFAULT_LLM_MODEL)

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

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)