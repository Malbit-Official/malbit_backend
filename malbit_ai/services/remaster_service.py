# 단문 리마스터링 서비스
from core.stt_engine import transcribe_audio
from services.llm_service import refine_text_with_llm
from core.config import Config

def run_remastering(asr_pipe, audio_path: str) -> dict:
    """음성 -> STT -> LLM 보정 프로세스 실행"""

    # STT로 날것의 텍스트 추출
    raw_text = transcribe_audio(asr_pipe, audio_path)

    # LLM으로 정중한 문장으로 보정
    refined_text =refine_text_with_llm(raw_text)

    return {
        "raw": raw_text,
        "refined": refined_text
    }