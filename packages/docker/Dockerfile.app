# ============================================================
# AI-OA Spring Boot Multi-stage Build Dockerfile
# Builds all 13 backend microservices
#
# Build args:
#   MODULE      - Maven module name (e.g. aioa-gateway)
#   JAR_NAME    - Output JAR artifact name (e.g. aioa-gateway)
#
# Usage:
#   docker build -f Dockerfile.app \
#     --build-arg MODULE=aioa-gateway \
#     --build-arg JAR_NAME=aioa-gateway \
#     -t aioa-gateway:latest \
#     ../../source/backend
#
# Or use docker-compose (recommended)
# ============================================================

# ── Stage 1: Build ──────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

ARG MODULE=aioa-gateway
ARG JAR_NAME=aioa-gateway

WORKDIR /build

# Copy parent pom first for dependency caching
COPY pom.xml /build/
COPY aioa-common/pom.xml /build/aioa-common/
COPY aioa-core/pom.xml /build/aioa-core/

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -pl aioa-common,aioa-core -am || true

# Copy all module source code
COPY aioa-common/ /build/aioa-common/
COPY aioa-core/ /build/aioa-core/
COPY ${MODULE}/ /build/${MODULE}/

# Build the target JAR
# Using package instead of spring-boot:repackage to support custom naming
RUN mvn package -pl ${MODULE} -am -DskipTests \
    && ls -lh ${MODULE}/target/*.jar

# ── Stage 2: Runtime ───────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

ARG JAR_NAME=aioa-gateway

# Create non-root user for security
RUN addgroup -S aioa && adduser -S aioa -G aioa

WORKDIR /app

# Copy the JAR from build stage
COPY --from=builder /build/${MODULE}/target/${JAR_NAME}-*.jar /app/app.jar

# Copy entrypoint script
COPY --chmod=+x <<'SCRIPT' /app/entrypoint.sh
#!/bin/sh
set -e

# Override Spring Boot config via environment variables
JAVA_OPTS="${JAVA_OPTS:-} \
  -XX:+UseG1GC \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -XX:+ExitOnOutOfMemoryError"

# Export all SPRING_*, DB_*, REDIS_* env vars as Spring properties
# This allows docker-compose env vars to override application.yml
export SPRING_DATASOURCE_URL="jdbc:mysql://${DB_HOST:-mysql}:${DB_PORT:-3306}/${DB_NAME:-ai_oa}?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false"
export SPRING_DATASOURCE_USERNAME="${DB_USERNAME:-root}"
export SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD:-root123456}"
export SPRING_DATASOURCE_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver"

export SPRING_DATA_REDIS_HOST="${REDIS_HOST:-redis}"
export SPRING_DATA_REDIS_PORT="${REDIS_PORT:-6379}"
export SPRING_DATA_REDIS_PASSWORD="${REDIS_PASSWORD:-}"
export SPRING_DATA_REDIS_DATABASE="0"

export SPRING_CLOUD_CONSUL_HOST="${CONSUL_HOST:-}"
export SPRING_CLOUD_CONSUL_PORT="${CONSUL_PORT:-8500}"

echo "[aioa] Starting ${JAR_NAME}..."
echo "[aioa] DB: ${SPRING_DATASOURCE_URL}"
echo "[aioa] Redis: ${SPRING_DATA_REDIS_HOST}:${SPRING_DATA_REDIS_PORT}"

exec java ${JAVA_OPTS} -jar /app/app.jar \
  --spring.profiles.active=${SPRING_PROFILES:-dev} \
  --server.port=${SERVER_PORT:-8080}
SCRIPT

# Set ownership
RUN chown -R aioa:aioa /app

USER aioa

EXPOSE ${SERVER_PORT:-8080}

ENTRYPOINT ["/app/entrypoint.sh"]
