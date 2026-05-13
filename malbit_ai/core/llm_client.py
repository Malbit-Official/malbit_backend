# Bedrock 클라이언트 초기화 및 공통 호출 모듈
import boto3
from core.config import Config

# 공통 클라이언트 생성
bedrock_client = boto3.client(
    "bedrock-runtime",
    region_name=Config.REGION_NAME,
    aws_access_key_id=Config.AWS_ACCESS_KEY,
    aws_secret_access_key=Config.AWS_SECRET_KEY
)