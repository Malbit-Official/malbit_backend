# 1. Java 21 런타임 이미지 사용 (경량화 버전)
FROM eclipse-temurin:21-jre-alpine

# 2. 필수 패키지 설치 (시간대 설정을 위해 필요할 수 있음)
RUN apk add --no-cache tzdata
ENV TZ=Asia/Seoul

# 3. 작업 디렉토리
WORKDIR /app

# 4. 빌드된 jar 파일을 app.jar로 복사
# build/libs/*.jar 로 두면 파일이 2개 이상일 때 에러가 날 수 있으므로
# plain.jar를 제외한 파일만 가져오도록 명시하는 것이 좋습니다.
COPY build/libs/*-SNAPSHOT.jar app.jar

# 5. 실행 설정
# 환경 변수는 docker run 단계에서 주입하므로 여기서는 기본 실행 명령만 둡니다.
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]