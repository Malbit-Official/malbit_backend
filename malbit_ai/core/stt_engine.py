# stt_engine.py
from faster_whisper import WhisperModel
import torch

def load_asr_pipeline():
    """Whisper 기본 모델 로드 (서버 시작 시 1회 호출)"""
    device = "cuda" if torch.cuda.is_available() else "cpu"
    
    # CPU 환경에서는 int8 양자화로 연산 속도를 극대화합니다.
    compute_type = "float16" if device == "cuda" else "int8"

    print(f" [STT Engine] {device.upper()} 모드로 faster-whisper 기본 모델 로딩 중...")
    
    # "base", "small", "tiny" 등 원하는 사이즈를 문자열로 바로 넣으면 됩니다.
    # 처음 실행할 때만 모델을 다운로드하고, 이후에는 캐시된 모델을 써서 바로 켜집니다.
    model = WhisperModel(
        "base",  # 혹은 "small" (속도와 정확도 타협점)
        device=device,
        compute_type=compute_type,
        cpu_threads=4  # EC2 코어 수에 맞게 설정
    )
    return model

def transcribe_audio(asr_model, audio_path: str) -> str:
    """음성 파일을 텍스트로 변환 (faster-whisper 기본 모델 버전)"""
    
    # 무거운 librosa 과정 없이 파일 경로를 직접 넣습니다.
    segments, info = asr_model.transcribe(
        audio_path,
        language="ko",
        beam_size=3,  # 3~5 사이 추천 (낮을수록 빨라짐)
        repetition_penalty=1.1
    )
    
    # 결과 텍스트 병합
    text_list = [segment.text for segment in segments]
    return "".join(text_list).strip()
