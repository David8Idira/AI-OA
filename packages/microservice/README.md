# AI-OA 微服务部署包

## 包含内容

```
microservice/
├── scripts/
│   ├── init-cluster.sh      # 集群初始化脚本
│   ├── deploy-kingbase.sh       # Kingbase主备部署
│   ├── deploy-redis.sh      # Redis Cluster部署
│   ├── deploy-rabbitmq.sh    # RabbitMQ集群部署
│   ├── deploy-minio.sh      # MinIO分布式部署
│   ├── deploy-lb.sh         # Nginx+Keepalived部署
│   ├── deploy-services.sh    # 微服务JAR部署
│   └── health-check.sh       # 健康检查脚本
├── config/
│   ├── gateway.yml           # 网关配置
│   ├── user-service.yml      # 用户服务配置
│   ├── workflow-service.yml  # 审批服务配置
│   ├── report-service.yml    # 报表服务配置
│   ├── ai-service.yml       # AI服务配置
│   ├── chat-service.yml      # 聊天服务配置
│   └── lb.conf              # Nginx负载均衡配置
├── sql/
│   └── init.sql             # 数据库初始化
├── systemd/
│   ├── aioa-gateway.service
│   ├── aioa-user.service
│   ├── aioa-workflow.service
│   ├── aioa-report.service
│   ├── aioa-ai.service
│   └── aioa-chat.service
├── Dockerfile
├── README.md
└── VERSION
```

## 服务器规划

| IP | 角色 | 服务 |
|-----|------|------|
| 192.168.1.101 | 负载均衡 | Nginx+Keepalived |
| 192.168.1.111 | Kingbase主 | Kingbase Master |
| 192.168.1.112 | Kingbase从 | Kingbase Slave |
| 192.168.1.121-123 | Redis | Redis Cluster |
| 192.168.1.131-133 | RabbitMQ | RabbitMQ Cluster |
| 192.168.1.141-144 | MinIO | MinIO分布式 |

## 快速部署

```bash
# 1. 解压
tar -xzvf AI-OA-microservice-v1.0.tar.gz
cd AI-OA-microservice-v1.0

# 2. 修改配置
vim config/*.yml  # 修改数据库密码、IP等

# 3. 初始化集群
chmod +x scripts/*.sh
sudo ./scripts/init-cluster.sh

# 4. 部署中间件
sudo ./scripts/deploy-kingbase.sh
sudo ./scripts/deploy-redis.sh
sudo ./scripts/deploy-rabbitmq.sh
sudo ./scripts/deploy-minio.sh

# 5. 部署负载均衡
sudo ./scripts/deploy-lb.sh

# 6. 部署微服务
sudo ./scripts/deploy-services.sh

# 7. 健康检查
./scripts/health-check.sh
```

## 环境要求

| 角色 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| 负载均衡 | 8核 | 16GB | 100GB SSD |
| Kingbase | 16核 | 64GB | 500GB SSD |
| Redis | 8核 | 32GB | 100GB SSD |
| RabbitMQ | 8核 | 16GB | 200GB SSD |
| MinIO | 8核 | 16GB | 2TB HDD |
| 应用服务器 | 16核 | 32GB | 200GB SSD |

## 服务端口

| 服务 | 端口 |
|------|------|
| Nginx | 80, 443 |
| Gateway | 8080 |
| User Service | 8081 |
| Workflow Service | 8082 |
| Report Service | 8083 |
| AI Service | 8084 |
| Chat Service | 8085 |
| Kingbase | 3306, 3307 |
| Redis | 6379-6381 |
| RabbitMQ | 5672, 15672 |
| MinIO | 9000, 9001 |

---

版本：1.0.0
更新日期：2026-04-05
