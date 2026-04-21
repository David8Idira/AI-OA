# AI-OA Dockerfile
# 多阶段构建

# 阶段1: 构建
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制pom和源码
COPY source/backend/pom.xml .
COPY source/backend/aioa-*/pom.xml aioa-*/
COPY source/backend/aioa-*/src aioa-*/src

# 构建
RUN mvn clean package -DskipTests

# 阶段2: 运行
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制jar
COPY --from=builder /build/aioa-*/target/*.jar /app/*.jar

# 配置
VOLUME /app/config

# 环境变量
ENV JAVA_OPTS="-Xms512m -Xmx2g"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/*.jar"]