from pathlib import Path
from faster_whisper import WhisperModel
from core.config import Config


def load_asr_pipeline():
    model_path = Config.DEFAULT_MODEL_PATH
    device = Config.ASR_DEVICE
    compute_type = Config.ASR_COMPUTE_TYPE

    print(" [STT Engine] faster-whisper 모델 로딩 중...")
    print(f" [STT Engine] model_path   = {model_path}")
    print(f" [STT Engine] device       = {device}")
    print(f" [STT Engine] compute_type = {compute_type}")

    if model_path.startswith(".") or model_path.startswith("/"):
        resolved_path = Path(model_path).resolve()

        if not resolved_path.exists():
            raise FileNotFoundError(f"모델 경로가 없습니다: {resolved_path}")

        if not (resolved_path / "model.bin").exists():
            raise FileNotFoundError(
                f"model.bin이 없습니다: {resolved_path}\n"
                f"faster-whisper는 ./model이 아니라 CT2 변환된 ./model_ct2를 써야 합니다."
            )

        model_path = str(resolved_path)
        print(f" [STT Engine] resolved_path = {model_path}")

    model = WhisperModel(
        model_path,
        device=device,
        compute_type=compute_type,
        cpu_threads=Config.ASR_CPU_THREADS
    )

    print(" [STT Engine] 모델 로딩 완료")
    return model


def transcribe_audio(asr_model, audio_path: str) -> str:
    segments, info = asr_model.transcribe(
        audio_path,
        language="ko",
        task="transcribe",
        beam_size=5,
        repetition_penalty=1.1,
        vad_filter=False
    )

    return " ".join([segment.text for segment in segments]).strip()
