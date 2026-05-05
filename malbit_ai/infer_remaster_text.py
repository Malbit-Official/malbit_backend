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
SUB_FOLDER = "model/whisper-dysarthria-ko/checkpoint-5500"
DEFAULT_LLM_MODEL = os.getenv("LLM_MODEL", "gpt-5-mini")

DEVICE = "cuda:0" if torch.cuda.is_available() else "cpu"
TORCH_DTYPE = torch.float16 if torch.cuda.is_available() else torch.float32

def load_audio(audio_path: str, target_sr: int = 16000) -> np.ndarray:
    audio, _ = librosa.load(audio_path, None)

    if sr != target_sr:
        audio = librosa.resample(audio, orig_sr=sr, target_sr=target_sr)

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


def refine_text_with_llm(raw_text: str, llm_model: str) -> str:
    if not raw_text.strip():
        return ""

    try:
        client = boto3.client("bedrock-runtime", region_name="ap-northeast-2")

        # Haiku 모델 ID
        model_id = "anthropic.claude-3-haiku-20240307-v1:0"

        # Claude 3용 프롬프트 구성
        prompt = f"""
        ## Role
        You are "Malbbit," a professional AI communication assistant specialized in reconstructing dysarthric speech (motor speech disorders) into clear, natural business Korean.

        ## Instructions
        1. **Deduplicate & Clean**: Dysarthric speech often results in meaningless repetitions. Merge repeated words or phrases into a single, natural instance.
        2. **Handle Hallucinations**: STT often produces random words like "Christmas," "Subway," or "Life" when it fails to recognize slurred speech. If a word is contextually nonsensical, infer the most plausible business term based on phonetic similarity or omit it.
        3. **Contextual Reconstruction**: Restore the speaker's original intent into a professional register (Polite/Honorific Korean). 
        4. **Minimal Intervention**: Do not add new information or creatively expand the sentence. Keep the output concise and relevant to a workplace setting.
        5. **Strict Output**: Return ONLY the final corrected Korean sentence. No explanations, no English, no conversational filler.

        ## Examples (Few-shot)
        - Input: "회의 회의 회의 시작 시작 할게요"
        - Output: "회의 시작하도록 하겠습니다."

        - Input: "오늘 오늘 크리스마스 자료 준비했어요" (Context: Phonetic similarity '커뮤니케이션')
        - Output: "오늘 커뮤니케이션 자료 준비했습니다."

        - Input: "지하철 타고 보니 배달하는 사람이 주문서" (Context: Unrelated hallucination)
        - Output: "주문서 확인 부탁드립니다." (Or keep it as original if intent is unclear but clean the structure)

        ## Current STT Input to Process:
        {raw_text}

        ## Final Reconstructed Korean:
        """

        native_request = {
            "anthropic_version": "bedrock-2023-05-31",
            "max_tokens": 500,
            "temperature": 0, # 정확도를 위해 0으로 설정
            "messages": [
                {
                    "role": "user",
                    "content": [{"type": "text", "text": prompt}]
                }
            ],
        }

        response = client.invoke_model(model_id=model_id, body=json.dumps(native_request))
        response_body = json.loads(response["body"].read())
        
        return response_body["content"][0]["text"].strip()

    except Exception as e:
        print(f"[Bedrock LLM Error] {e}")
        return raw_text 

if __name__ == "__main__":
    print("이 파일은 라이브러리용입니다. app.py를 실행하세요.")