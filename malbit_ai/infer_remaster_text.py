import os
from pathlib import Path
import librosa
import numpy as np
import torch
from dotenv import load_dotenv
from transformers import WhisperForConditionalGeneration, AutoProcessor, pipeline
import boto3
import json

load_dotenv()

TARGET_SR = 16000
DEFAULT_MODEL_PATH = "tepo6640/malbit_ai"  
SUB_FOLDER = "model/whisper-dysarthria-ko/checkpoint-6825"
DEFAULT_LLM_MODEL = os.getenv("LLM_MODEL", "anthropic.claude-3-haiku-20240307-v1:0")

DEVICE = "cuda:0" if torch.cuda.is_available() else "cpu"
TORCH_DTYPE = torch.float16 if torch.cuda.is_available() else torch.float32

def load_audio(audio_path: str, target_sr: int = 16000) -> np.ndarray:
    audio, _ = librosa.load(audio_path, sr=target_sr, mono=True)

    audio, _ = librosa.effects.trim(audio)

    if np.max(np.abs(audio)) > 0:
        audio = audio / np.max(np.abs(audio))

    return audio.astype(np.float32)


def load_asr_pipeline(model_path: str):

    model = WhisperForConditionalGeneration.from_pretrained(
        model_path,
        subfolder=SUB_FOLDER,
        dtype=TORCH_DTYPE,
        low_cpu_mem_usage=True,
    )

    if torch.cuda.is_available():
        model.to(DEVICE)

    processor = AutoProcessor.from_pretrained(
        model_path,
        subfolder="model/whisper-dysarthria-ko"
    )

    return pipeline(
        task="automatic-speech-recognition",
        model=model,
        tokenizer=processor.tokenizer,
        feature_extractor=processor.feature_extractor,
        torch_dtype=TORCH_DTYPE,
        device=0 if torch.cuda.is_available() else -1,
        chunk_length_s=30,
        return_timestamps=True,
    )

bedrock_client = boto3.client("bedrock-runtime", region_name="ap-northeast-2")

def refine_text_with_llm(raw_text: str, llm_model: str) -> str:
    # 입력값이 비어있는지 확인
    if not raw_text.strip():
        print(" [Bedrock] 경고: raw_text가 비어있어 호출을 건너뜁니다.")
        return ""

    prompt = f"""You are "Malbbit," a professional AI communication assistant specialized in reconstructing dysarthric speech into clear, natural business Korean.

    ### Instructions
    1. Deduplicate meaningless repetitions.
    2. If a word clearly breaks the context (STT hallucination), replace it with a plausible term or omit it.
    3. Use professional Polite/Honorific Korean.
    4. Return ONLY the final corrected Korean sentence without any explanations.

    ### Input to Process:
    {raw_text}

    ### Final Reconstructed Korean:"""

    native_request = {
        "anthropic_version": "bedrock-2023-05-31",
        "max_tokens": 500,
        "temperature": 0, # 정확도를 위해 0으로 설정
        "top_p": 0.9,
        "messages": [
            {
                "role": "user",
                "content": [{"type": "text", "text": prompt}]
            }
        ],
    }
    
    print(f" [Bedrock Request] 전송 데이터: {raw_text}")

    try:
        response = bedrock_client.invoke_model(
            modelId=llm_model, 
            contentType="application/json", 
            accept="application/json",
            body=json.dumps(native_request)
        )

        response_body = json.loads(response["body"].read())
        refined_text = response_body["content"][0]["text"].strip()
    
        print(f" [Bedrock Response] 정제 완료: {refined_text}")
        return refined_text

    except Exception as e:
        print(f"[Bedrock LLM Error] {e}")
        return raw_text 

if __name__ == "__main__":
    pass