# ETAP 1: Maven 3.9 z Javą 25 do zbudowania paczki (Builder)
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# ETAP 2: Środowisko uruchomieniowe (Runtime) z Javą 25
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8102

ENTRYPOINT ["java", "-jar", "app.jar"]