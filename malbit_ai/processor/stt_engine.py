# Whisper 모델 로드 및 긴 음성 처리
import numpy as np
import librosa
import torch
from transformers import WhisperForConditionalGeneration, AutoProcessor, pipeline

def load_audio(audio_path: str, target_sr: int = 16000) -> np.ndarray:
    """오디오 로드 및 전처리 (정규화 및 트리밍)"""
    audio, _ = librosa.load(audio_path, sr=target_sr, mono=True)
    audio, _ = librosa.effects.trim(audio)

    if np.max(np.abs(audio)) > 0:
        audio = audio / np.max(np.abs(audio))

    return audio.astype(np.float32)

def load_asr_pipeline(model_path: str, sub_folder: str):
    """Whisper 파이프라인 로드"""
    device = "cuda:0" if torch.cuda.is_available() else "cpu"
    torch_dtype = torch.float16 if torch.cuda.is_available() else torch.float32

    model = WhisperForConditionalGeneration.from_pretrained(
        model_path,
        subfolder=sub_folder,
        dtype=torch_dtype,
        low_cpu_mem_usage=True,
    )
    if torch.cuda.is_available():
        model.to(device)

    processor = AutoProcessor.from_pretrained(
        model_path,
        subfolder="model/whisper-dysarthria-ko"
    )

    return pipeline(
        task="automatic-speech-recognition",
        model=model,
        tokenizer=processor.tokenizer,
        feature_extractor=processor.feature_extractor,
        torch_dtype=torch_dtype,
        device=0 if torch.cuda.is_available() else -1,
        chunk_length_s=30, # 30초 단위로 나누어 처리
        return_timestamps=False,
    )

def transcribe_audio(asr_pipe, audio_path: str) -> str:
    """긴 음성 파일을 처리하여 텍스트 반환"""
    audio = load_audio(audio_path)
    
    result = asr_pipe(
        audio,
        chunk_length_s=30,   
        stride_length_s=5,     
        batch_size=8,
        return_timestamps=True, 
        generate_kwargs={
            "language": "ko",
            "task": "transcribe",
            "repetition_penalty": 1.1, 
        }
    )
    return result["text"]