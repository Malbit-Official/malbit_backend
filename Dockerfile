# 1. Java 21 런타임 이미지 사용
FROM eclipse-temurin:21-jre-alpine

# 2. 작업 디렉토리
WORKDIR /app

# 3. 빌드된 jar 파일을 app.jar로 복사
# Gradle 빌드 시 생성되는 위치 기준
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 4. 실행 (prod 프로필 적용)
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]