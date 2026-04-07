# ─────────────────────────────────────────────────
# Stage 1: Build the application
# ─────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy dependency manifests first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────────
# Stage 2: Runtime image (lean JRE only)
# ─────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the fat JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Render sets PORT env var; Spring Boot will honour server.port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
