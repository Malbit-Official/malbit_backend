# 1. Python 3.9 환경 사용 (Whisper와 호환성 좋음)
FROM python:3.9-slim

# 2. 필수 시스템 패키지 설치 (음성 처리를 위한 ffmpeg 포함)
RUN apt-get update && apt-get install -y \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# 3. 작업 디렉토리 설정
WORKDIR /app

# 4. 라이브러리 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. 소스 코드 복사
COPY . .

# 6. 실행
CMD ["python", "infer_remaster_text.py"]