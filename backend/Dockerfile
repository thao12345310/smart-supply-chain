# ---------- Stage 1: Build ----------
# Build file JAR bằng Maven (không cần cài Maven/JDK ở máy local)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependency: copy pom trước, tải dependency, rồi mới copy source
COPY pom.xml .
RUN mvn -q dependency:go-offline -B

COPY src ./src
RUN mvn -q clean package -DskipTests -B

# ---------- Stage 2: Runtime ----------
# Image gọn chỉ chứa JRE để chạy app
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render sẽ truyền biến PORT; Spring đọc qua server.port=${PORT:8080}
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
