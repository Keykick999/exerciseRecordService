# 1단계: Gradle을 사용하여 JAR 빌드
FROM gradle:8.1.1-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test



# 2단계: 실행용 이미지 (최종 배포 이미지)
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
