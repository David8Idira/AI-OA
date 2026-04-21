# AI-OA Docker 配置文件

## 目录结构

```
docker/
├── Dockerfile.api          # Spring Boot API Dockerfile
├── Dockerfile.web         # Vue3 前端 Dockerfile
├── Dockerfile.ai          # Python AI 服务 Dockerfile
├── Dockerfile.ocr         # Python OCR 服务 Dockerfile
├── Dockerfile.n8n         # n8n 工作流引擎
├── docker-compose.yml     # 本地开发环境
├── docker-compose.prod.yml # 生产环境
├── nginx.conf             # Nginx 配置
└── entrypoint.sh          # 启动脚本
```

## 快速启动

### 本地开发环境

```bash
# 构建所有镜像
docker-compose build

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止所有服务
docker-compose down
```

### 生产环境

```bash
# 使用 Docker Compose Prod
docker-compose -f docker-compose.prod.yml up -d

# 或使用 Kubernetes
kubectl apply -f ../k8s/
```

## 服务列表

| 服务 | 端口 | 说明 |
|------|------|------|
| oa-web | 80 | 前端 Vue3 |
| oa-api | 8080 | 后端 API |
| oa-ai | 8000 | AI 服务 |
| oa-ocr | 8001 | OCR 服务 |
| n8n | 5678 | 工作流引擎 |
| mysql | 3306 | 数据库 |
| redis | 6379 | 缓存 |
| kafka | 9092 | Kafka 消息队列 |
| rabbitmq | 5672/15672 | RabbitMQ 消息队列 |
| minio | 9000/9001 | 对象存储 |
| nacos | 8848 | 配置中心 |
| nginx | 443 | 反向代理 |
