# Whisper 파이프라인 로드 및 음성 전사 로직
from transformers import WhisperForConditionalGeneration, AutoProcessor, pipeline
from core.config import Config
import librosa
import numpy as np

import torch
from transformers import WhisperForConditionalGeneration, AutoProcessor, pipeline
from core.config import Config
import librosa
import numpy as np

def load_asr_pipeline():
    """Whisper 파이프라인 로드 (서버 시작 시 1회 호출)"""
    device = "cuda:0" if torch.cuda.is_available() else "cpu"
    torch_dtype = torch.float16 if torch.cuda.is_available() else torch.float32

    # 모델 로드
    model = WhisperForConditionalGeneration.from_pretrained(
        Config.DEFAULT_MODEL_PATH,
        #subfolder=Config.SUB_FOLDER,
        torch_dtype=torch_dtype,
        low_cpu_mem_usage=True,
    )
    if torch.cuda.is_available():
        model.to(device)

    processor = AutoProcessor.from_pretrained(
        Config.DEFAULT_MODEL_PATH,
        #subfolder="model/whisper-dysarthria-ko"
    )

    return pipeline(
        task="automatic-speech-recognition",
        model=model,
        tokenizer=processor.tokenizer,
        feature_extractor=processor.feature_extractor,
        torch_dtype=torch_dtype,
        device=0 if torch.cuda.is_available() else -1,
        chunk_length_s=30,
        return_timestamps=False,
    )

def transcribe_audio(asr_pipe, audio_path: str) -> str:
    """음성 파일을 텍스트로 변환"""
    audio, _ = librosa.load(audio_path, sr=16000, mono=True)
    
    result = asr_pipe(
        audio,
        chunk_length_s=30,   
        stride_length_s=5,     
        batch_size=8,
        generate_kwargs={
            "language": "ko",
            "task": "transcribe",
            "repetition_penalty": 1.1, 
        }
    )
    return result["text"]