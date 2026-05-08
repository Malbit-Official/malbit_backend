# 설정값을 관리하고 모듈들을 호출
import os
from dotenv import load_dotenv
from processor.stt_engine import load_asr_pipeline, transcribe_audio
from processor.llm_handler import refine_text_with_llm

load_dotenv()

# 설정값
DEFAULT_MODEL_PATH = "tepo6640/malbit_ai"  
SUB_FOLDER = "model/whisper-dysarthria-ko/checkpoint-6825"
DEFAULT_LLM_MODEL = os.getenv("LLM_MODEL", "anthropic.claude-3-haiku-20240307-v1:0")

# 파이프라인 초기화 (서버 시작 시 한 번만 실행)
asr_pipe = load_asr_pipeline(DEFAULT_MODEL_PATH, SUB_FOLDER)

def run_remastering(audio_path: str) -> dict:
    """전체 리마스터링 프로세스 실행"""
    # STT 수행 (긴 문장 처리 가능)
    raw_text = transcribe_audio(asr_pipe, audio_path)
    
    # LLM 정제 수행
    refined_text = refine_text_with_llm(raw_text, LLM_MODEL)
    
    return {
        "raw": raw_text,
        "refined": refined_text
    }