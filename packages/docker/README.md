# AI-OA Docker Compose部署包 (Kingbase + RabbitMQ)

## 包含内容

```
docker/
├── docker-compose.yml        # 完整编排文件 (Kingbase + RabbitMQ)
├── .env                      # 环境变量模板
├── Dockerfile.app            # 应用镜像构建
├── kingbase/
│   └── init/
│       └── 01-init.sql       # Kingbase数据库初始化
├── rabbitmq/                 # RabbitMQ配置目录
├── nginx/
│   └── nginx.conf            # Nginx配置
├── scripts/
│   ├── common.sh             # 公共函数
│   └── deploy.sh             # 部署脚本
├── README.md
└── VERSION
```

## 技术架构

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| Kingbase | kingbase:v9.1 | 54321 | 金仓数据库 |
| Redis | redis:7-alpine | 6379 | 缓存 |
| RabbitMQ | rabbitmq:3.13-management-alpine | 5672, 15672 | 消息队列 |
| AI-OA后端 | build | 8080-8092 | 13个微服务 |
| Frontend | nginx:alpine | 80 | Vue 3前端 |
| Nginx | nginx:alpine | 8000 | API网关 |

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
sudo ./scripts/deploy.sh

# 4. 查看状态
docker compose ps

# 5. 查看日志
docker compose logs -f

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
docker compose up -d

# 停止
docker compose down

# 查看日志
docker compose logs -f [服务名]

# 进入Kingbase容器
docker exec -it aioa-kingbase bash

# 进入RabbitMQ管理界面
open http://localhost:15672
```

## 数据持久化

| 数据类型 | 存储位置 |
|----------|----------|
| Kingbase数据 | kingbase_data volume |
| Redis数据 | redis_data volume |
| RabbitMQ数据 | rabbitmq_data volume |
| Nginx日志 | ./nginx/logs |

## 注意事项

1. Kingbase V9 首次启动需要60秒初始化
2. 确保 54321 端口未被占用
3. 生产环境请修改默认密码