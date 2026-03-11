# =====================================================
# MyLiu Flowchart Backend Dockerfile
# 多阶段构建
# =====================================================

# 构建阶段
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# 复制Maven配置和源码
COPY pom.xml .
COPY src ./src

# 安装Maven并构建
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# =====================================================
# 运行阶段
# =====================================================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="MyLiu <admin@myliu.com>"
LABEL description="MyLiu Flowchart Backend Service"
LABEL version="1.0.0"

# 设置工作目录
WORKDIR /app

# 安装必要工具
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建非root用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 从构建阶段复制JAR包
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
