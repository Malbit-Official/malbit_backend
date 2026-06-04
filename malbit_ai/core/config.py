# 환경 변수 및 모델 경로 설정
import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    # AWS Bedrock 설정
    AWS_ACCESS_KEY = os.getenv("AWS_ACCESS_KEY_ID")
    AWS_SECRET_KEY = os.getenv("AWS_SECRET_ACCESS_KEY")
    REGION_NAME = "ap-northeast-2"
    LLM_MODEL = os.getenv("LLM_MODEL", "anthropic.claude-3-haiku-20240307-v1:0")

    DEFAULT_MODEL_PATH = os.getenv("WHISPER_MODEL_PATH", "./model_ct2")
    SUB_FOLDER = ""

    ASR_DEVICE = os.getenv("ASR_DEVICE", "cpu")
    ASR_COMPUTE_TYPE = os.getenv("ASR_COMPUTE_TYPE", "int8")
    ASR_CPU_THREADS = int(os.getenv("ASR_CPU_THREADS", "4"))

    BACKEND_URL = os.getenv("BACKEND_URL", "http://3.37.239.105:8080")