# AI-OA Docker Compose部署包

## 包含内容

```
docker/
├── docker-compose.yml        # 完整编排文件
├── .env                     # 环境变量模板
├── Dockerfile.app           # 应用镜像构建
├── Dockerfile.mysql         # MySQL镜像
├── Dockerfile.minio        # MinIO镜像
├── nginx/
│   └── nginx.conf          # Nginx配置
├── scripts/
│   ├── start.sh            # 启动脚本
│   ├── stop.sh             # 停止脚本
│   ├── restart.sh           # 重启脚本
│   ├── logs.sh              # 日志查看
│   ├── backup.sh            # 数据备份
│   └── restore.sh           # 数据恢复
├── data/                    # 数据持久化目录
├── html/                    # 前端静态文件
├── backups/                 # 备份目录
├── README.md
└── VERSION
```

## 快速部署

```bash
# 1. 解压
tar -xzvf AI-OA-docker-v1.0.tar.gz
cd AI-OA-docker-v1.0

# 2. 配置环境变量
cp .env.example .env
vim .env  # 修改密码等

# 3. 启动服务
chmod +x scripts/*.sh
sudo ./scripts/start.sh

# 4. 查看状态
docker-compose ps

# 5. 查看日志
./scripts/logs.sh -f

# 6. 访问
open http://localhost
```

## Docker版本要求

- Docker: 20.10+
- Docker Compose: 2.0+
- 推荐配置: 4C/8G+

## 服务列表

| 服务 | 镜像 | 端口 |
|------|------|------|
| Kingbase | kingbase:v9.1 | 54321 |
| Redis | redis:7-alpine | 6379 |
| RabbitMQ | rabbitmq:3.13-management-alpine | 5672, 15672 |
| AI-OA | aioa/app | 8080 |
| Nginx | nginx:alpine | 80, 443 |

## 管理命令

```bash
# 启动
./scripts/start.sh

# 停止
./scripts/stop.sh

# 重启
./scripts/restart.sh

# 查看日志
./scripts/logs.sh -f [服务名]

# 备份
./scripts/backup.sh

# 恢复
./scripts/restore.sh backup_20260405.tar.gz

# 进入容器
docker exec -it aioa-mysql bash

# 重建单个服务
docker-compose up -d --force-recreate mysql
```

## 数据持久化

| 服务 | 宿主机目录 |
|------|------------|
| MySQL | ./data/mysql |
| Redis | ./data/redis |
| MinIO | ./data/minio |
| RabbitMQ | ./data/rabbitmq |
| n8n | ./data/n8n |

## 健康检查

```bash
# 检查所有服务
curl http://localhost/api/health

# 检查单个服务
curl http://localhost:3306  # MySQL
curl http://localhost:6379  # Redis
curl http://localhost:9000/minio/health/live  # MinIO
curl http://localhost:15672  # RabbitMQ
curl http://localhost:5678/healthz  # n8n
```

---

版本：1.0.0
更新日期：2026-04-05
