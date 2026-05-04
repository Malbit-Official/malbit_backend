# 기존 infer 코드의 로직을 API 형태로 변환
from fastapi import FastAPI, UploadFile, File, HTTPException
import os
import torch
import librosa
import io
import shutil
from pathlib import Path
from infer_remaster_text import load_asr_pipeline, load_audio, refine_text_with_llm, DEFAULT_MODEL_PATH, DEFAULT_LLM_MODEL

app = FastAPI()

# 서버 시작 시 모델을 미리 로드 (속도 향상)
print("AI 모델 로딩 중...")
asr_pipe = load_asr_pipeline(DEFAULT_MODEL_PATH)
print("모델 로딩 완료!")

@app.post("/analyze")
async def analyze_voice(file: UploadFile = File(...)):
    try:
        # 파일을 메모리에서 직접 읽기
        file_contents = await file.read()

        # 오디오 로드 (ID3 태그 무시 및 자동 리샘플링)
        # sr=16000으로 설정하여 Whisper 최적 주파수로 변환
        audio_array, _ = librosa.load(io.BytesIO(file_contents), sr=16000)

        # 앞뒤 무음 및 노이즈 제거 (VAD 적용)
        audio_array, _ = librosa.effects.trim(audio_array, top_db=20)

        if len(audio_array) == 0:
            return {"status": "success", "raw_text": "", "refined_teext": "인식된 음성이 없습니다."}
        
        # STT 수행
        result = asr_pipe(
            {"array": audio_array, "sampling_rate": 16000},
            generate_kwargs={
                "repetition_penalty": 1.5,     
                "no_repeat_ngram_size": 3,      
                "language": "ko",             
                "task": "transcribe",
                "do_sample": False,
                "temperature": 0.0,
                "condition_on_prev_tokens": False
            }
        )

        raw_text = result["text"].strip() if isinstance(result, dict) else str(result).strip()

        # LLM 보정
        refined_text = refine_text_with_llm(raw_text, DEFAULT_LLM_MODEL)

        return {
            "status": "success",
            "raw_text": raw_text,
            "refined_text": refined_text
        }
    
    except Exception as e:
        print(f"Error during analysis: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)