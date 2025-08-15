# 기존 Java 기반
FROM openjdk:17-jdk-slim

# 작업 디렉터리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# -------------------------------
# Playwright 실행에 필요한 최소 Linux 라이브러리 설치
# Node.js 설치
# Chromium 설치
# -------------------------------
RUN apt-get update && apt-get install -y --no-install-recommends \
        curl gnupg ca-certificates \
        libnss3 libatk1.0-0 libatk-bridge2.0-0 libcups2 libxkbcommon0 libxcomposite1 \
        libxdamage1 libxfixes3 libxrandr2 libgbm1 libpango-1.0-0 libcairo2 libasound2 \
        libdbus-1-3 libexpat1 libglib2.0-0 \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# Playwright Chromium 브라우저 설치
RUN npx playwright install chromium

# (선택) root가 아닌 사용자로 실행
RUN adduser --disabled-password --gecos "" appuser && chown -R appuser:appuser /app
USER appuser

# 기존 Spring Boot 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
