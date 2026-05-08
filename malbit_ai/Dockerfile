# 1. Python 3.9 환경 사용 
FROM python:3.9-slim

# 2. 필수 시스템 패키지 설치
RUN apt-get update && apt-get install -y \
    ffmpeg \
    gcc \
    python3-dev \
    libsndfile1 \
    && rm -rf /var/lib/apt/lists/*

# 3. 작업 디렉토리 설정
WORKDIR /app

# 4. 라이브러리 설치 (캐시 활용)
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# 5. 소스 코드 및 모델 데이터 복사
COPY . .

# 6. 환경 변수 설정
ENV PYTHONUNBUFFERED=1
ENV MODEL_PATH="tepo6640/malbit_ai" 

# 7. 실행 설정
RUN mkdir -p /app/uploads

EXPOSE 8000

CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]