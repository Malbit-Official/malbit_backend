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

    # Whisper 모델 설정
    #DEFAULT_MODEL_PATH = "tepo6640/malbit_ai"
    #SUB_FOLDER = "model/whisper-dysarthria-ko/checkpoint-6825"

    DEFAULT_MODEL_PATH = "openai/whisper-medium"
    SUB_FOLDER = ""

    # 백엔드(Java) 연동 설정
    BACKEND_URL = os.getenv("BACKEND_URL", "http://3.37.239.105:8080")