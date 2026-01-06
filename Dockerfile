# 1. 실행에 필요한 최소한의 자바 환경만 가져옵니다.
FROM eclipse-temurin:17-jdk

# 2. 컨테이너 내부의 작업 디렉토리 설정
WORKDIR /app

# 3. GitHub Actions 워크플로우에서 빌드 완료된 jar 파일만 복사합니다.
# 워크플로우의 'context: .' 설정 덕분에 이 경로를 찾을 수 있습니다.
COPY build/libs/*SNAPSHOT.jar app.jar

# 4. 앱이 사용할 포트 명시
EXPOSE 8080

# 5. 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]

