# ─────────────────────────────────────────────────────────────
# Stage 1: Build with Maven + Java 17
# ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build production JAR (skip tests for speed and reliability)
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────────────────────
# Stage 2: Run with lightweight Java 17 JRE
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Render exposes the port via the PORT environment variable
EXPOSE 10000

# Start the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
