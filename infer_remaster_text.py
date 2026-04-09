import argparse
import os
from pathlib import Path

import librosa
import numpy as np
import torch
from dotenv import load_dotenv
from openai import OpenAI
from scipy.io import wavfile
from transformers import AutoModelForSpeechSeq2Seq, AutoProcessor, pipeline

load_dotenv()

TARGET_SR = 16000
DEFAULT_MODEL_PATH = r"D:\malbit_ai\models\whisper-dysarthria-ko"
DEFAULT_LLM_MODEL = os.getenv("LLM_MODEL", "gpt-5-mini")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")

DEVICE = "cuda:0" if torch.cuda.is_available() else "cpu"
TORCH_DTYPE = torch.float16 if torch.cuda.is_available() else torch.float32


def load_audio(audio_path: str, target_sr: int = 16000) -> np.ndarray:
    sr, audio = wavfile.read(audio_path)

    if len(audio.shape) > 1:
        audio = np.mean(audio, axis=1)

    if audio.dtype == np.int16:
        audio = audio.astype(np.float32) / 32768.0
    elif audio.dtype == np.int32:
        audio = audio.astype(np.float32) / 2147483648.0
    elif audio.dtype == np.uint8:
        audio = (audio.astype(np.float32) - 128.0) / 128.0
    else:
        audio = audio.astype(np.float32)

    if sr != target_sr:
        audio = librosa.resample(audio, orig_sr=sr, target_sr=target_sr)

    return audio


def load_asr_pipeline(model_path: str):
    model = AutoModelForSpeechSeq2Seq.from_pretrained(
        model_path,
        torch_dtype=TORCH_DTYPE,
        low_cpu_mem_usage=True,
    )

    if torch.cuda.is_available():
        model.to(DEVICE)

    processor = AutoProcessor.from_pretrained(model_path)

    asr_pipe = pipeline(
        task="automatic-speech-recognition",
        model=model,
        tokenizer=processor.tokenizer,
        feature_extractor=processor.feature_extractor,
        torch_dtype=TORCH_DTYPE,
        device=0 if torch.cuda.is_available() else -1,
        chunk_length_s=30,
        stride_length_s=5,
        return_timestamps=True,
        generate_kwargs={
            "language": "ko",
            "task": "transcribe",
        },
    )
    return asr_pipe


def transcribe_audio(audio_path: str, model_path: str) -> str:
    asr_pipe = load_asr_pipeline(model_path)
    audio_array = load_audio(audio_path, TARGET_SR)
    result = asr_pipe({"array": audio_array, "sampling_rate": TARGET_SR})

    if isinstance(result, dict):
        if "text" in result:
            return result["text"].strip()
    return str(result).strip()


def refine_text_with_llm(raw_text: str, llm_model: str) -> str:
    if not OPENAI_API_KEY:
        return raw_text

    try:
        client = OpenAI(api_key=OPENAI_API_KEY)

        prompt = f"""
너는 구음장애 화자의 발화를 자연스럽고 읽기 쉬운 한국어 문장으로 복원하는 시스템이다.

규칙:
1. 입력은 STT 결과다.
2. 화자의 의도를 유지한다.
3. 의미를 과도하게 지어내지 않는다.
4. 확실하지 않은 부분은 원문을 최대한 유지한다.
5. 맞춤법과 띄어쓰기는 자연스럽게 보정한다.
6. 출력은 복원된 최종 문장만 한다.

입력:
{raw_text}
"""

        response = client.responses.create(
            model=llm_model,
            input=prompt,
            max_output_tokens=150,
        )

        if hasattr(response, "output_text") and response.output_text:
            return response.output_text.strip()

        return response.output[0].content[0].text.strip()

    except Exception as e:
        print(f"[LLM SKIP] {e}")
        return raw_text


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--audio", required=True, help="입력 wav 파일 경로")
    parser.add_argument("--model", default=DEFAULT_MODEL_PATH, help="학습된 Whisper 모델 경로")
    parser.add_argument("--llm-model", default=DEFAULT_LLM_MODEL, help="리마스터링에 쓸 LLM 이름")
    args = parser.parse_args()

    audio_path = Path(args.audio)
    model_path = Path(args.model)

    if not audio_path.exists():
        raise FileNotFoundError(f"audio file not found: {audio_path}")

    if not model_path.exists():
        raise FileNotFoundError(f"model path not found: {model_path}")

    print("torch.cuda.is_available() =", torch.cuda.is_available())
    if torch.cuda.is_available():
        print("GPU =", torch.cuda.get_device_name(0))

    raw_text = transcribe_audio(str(audio_path), str(model_path))
    refined_text = refine_text_with_llm(raw_text, args.llm_model)

    print("\n=== ASR 원문 ===")
    print(raw_text)
    print("\n=== 리마스터링 결과 ===")
    print(refined_text)


if __name__ == "__main__":
    main()