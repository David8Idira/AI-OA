# AI-OA Docker 部署说明

## 📦 目录结构

```
packages/docker/
├── docker-compose.yml      # 完整编排文件（13个后端 + MySQL + Redis + Nginx + Frontend）
├── Dockerfile.app           # Spring Boot 多阶段构建镜像
├── Dockerfile.mysql         # MySQL 初始化镜像
├── .env                     # 环境变量模板
├── README.md                # 本文档
├── DEPLOY.md                # 部署详细说明
├── nginx/
│   ├── nginx.conf           # Nginx 反向代理配置
│   ├── conf.d/              # Nginx 额外配置（可选）
│   ├── ssl/                 # SSL 证书目录（可选）
│   └── logs/                # Nginx 日志目录
└── mysql/
    └── init/
        └── 01-databases.sql # MySQL 数据库初始化脚本
```

---

## 🏗️ 架构概览

```
                                    ┌─────────────────────────────────────┐
                                    │          Nginx (端口 8000)           │
                                    │   统一入口 / 反向代理 / SSL 终结      │
                                    └──────────────┬──────────────────────┘
                                                   │
            ┌──────────────────────────────────────┼──────────────────────┐
            │                    Docker Network   │                       │
            │                                      │                       │
    ┌───────┴───────┐                    ┌─────────┴────────┐    ┌────────┴───────┐
    │   MySQL 3306  │                    │     Redis 6379   │    │  Frontend :80  │
    └───────────────┘                    └──────────────────┘    └────────────────┘
                                                                          │
                                       ┌─────────────────────────────────────┼────┐
                                       │                                     │    │
                          ┌────────────┴───┐  ┌────────────┴───┐  ┌────────────┴───┐
                          │ aioa-gateway  │  │  aioa-system  │  │ aioa-workflow  │
                          │    :8080      │  │    :8081      │  │    :8082      │
                          └───────────────┘  └────────────────┘  └────────────────┘
                          ┌────────────┬───┴┬────────────┬───┴┬────────────┬────┐
                          │ aioa-       │    │ aioa-       │    │ aioa-       │
                          │ knowledge   │    │ ai          │    │ asset       │
                          │ :8083      │    │ :8084       │    │ :8085       │
                          └────────────┘    └──────────────┘    └─────────────┘
                          ┌────────────┬───┬────────────┬───┬────────────┬────┐
                          │ aioa-       │    │ aioa-       │    │ aioa-       │
                          │ attendance  │    │ hr          │    │ license    │
                          │ :8086      │    │ :8087       │    │ :8088      │
                          └────────────┘    └──────────────┘    └─────────────┘
                          ┌────────────┬───┬────────────┬───┬────────────┬────┐
                          │ aioa-       │    │ aioa-       │    │ aioa-       │
                          │ ocr         │    │ reimburse   │    │ im          │
                          │ :8089      │    │ :8090       │    │ :8092      │
                          └────────────┘    └──────────────┘    └─────────────┘
                          ┌────────────┐
                          │ aioa-report│
                          │ :8091      │
                          └────────────┘
```

---

## 🔧 快速部署

### 前置条件

- Docker 20.10+
- Docker Compose v2.0+ (`docker compose` 或 `docker-compose`)
- 建议可用内存：**8GB+**

### 步骤 1：配置环境变量

```bash
cd /root/workspace/AI-OA/packages/docker

# 复制并编辑环境变量文件
cp .env .env.local

# 编辑 .env.local 自定义配置
vim .env.local
```

主要配置项：

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DB_PASSWORD` | MySQL root 密码 | `root123456` |
| `REDIS_PASSWORD` | Redis 密码（留空无密码） | （空） |
| `JWT_SECRET` | JWT 签名密钥（≥32字符） | `aioa-jwt-secret...` |
| `MIMI_API_KEY` | MiniMax API Key | 来自源码 |
| `FRONTEND_DIST_PATH` | 前端 dist 目录路径 | `../../source/frontend/dist` |

### 步骤 2：构建并启动所有服务

```bash
# 使用 Docker Compose 启动（推荐前台）
docker compose --env-file .env.local up --build

# 或后台运行
docker compose --env-file .env.local up --build -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

### 步骤 3：验证部署

```bash
# 健康检查 - Gateway
curl http://localhost:8000/actuator/health

# 直接访问 Gateway（绕过 Nginx）
curl http://localhost:8080/actuator/health

# 查看所有服务日志
docker compose logs --tail=100 -f

# 查看特定服务
docker compose logs -f aioa-gateway
docker compose logs -f aioa-ai
```

---

## 🚀 单独构建某个服务

```bash
# 单独构建并启动 Gateway
docker compose build aioa-gateway
docker compose up -d aioa-gateway

# 单独重启某个服务
docker compose restart aioa-ai

# 重新构建某个服务（清除缓存）
docker compose build --no-cache aioa-report
docker compose up -d aioa-report
```

---

## 🌐 服务访问地址

### 通过 Nginx 反向代理（推荐）

| 服务 | 地址 |
|------|------|
| 统一入口（Frontend + API） | `http://<host>:8000` |
| Frontend | `http://<host>:8000/` |
| API Gateway | `http://<host>:8000/api/` |
| WebSocket (IM) | `ws://<host>:8000/ws/` |

### 直接访问微服务（开发调试）

| 服务 | 端口 | 地址 |
|------|------|------|
| aioa-gateway | 8080 | `http://<host>:8080` |
| aioa-system | 8081 | `http://<host>:8081` |
| aioa-workflow | 8082 | `http://<host>:8082` |
| aioa-knowledge | 8083 | `http://<host>:8083` |
| aioa-ai | 8084 | `http://<host>:8084` |
| aioa-asset | 8085 | `http://<host>:8085` |
| aioa-attendance | 8086 | `http://<host>:8086` |
| aioa-hr | 8087 | `http://<host>:8087` |
| aioa-license | 8088 | `http://<host>:8088` |
| aioa-ocr | 8089 | `http://<host>:8089` |
| aioa-reimburse | 8090 | `http://<host>:8090` |
| aioa-report | 8091 | `http://<host>:8091` |
| aioa-im | 8092 | `http://<host>:8092` |

---

## 🗄️ 数据库说明

### 自动创建的数据库

| 数据库名 | 用途 | 微服务 |
|----------|------|--------|
| `ai_oa` | 主数据库 | gateway, system, workflow, knowledge, ai, asset, hr, license, reimburse, report |
| `ai_oa_attendance` | 考勤模块 | attendance |
| `aioa` | IM 模块 | im |
| `aioa_ocr` | OCR 模块 | ocr |
| `aioa_knowledge` | 知识库模块 | knowledge |

### 连接数据库

```bash
# 从容器内连接
docker exec -it aioa-mysql mysql -u root -p

# 从宿主机连接
mysql -h 127.0.0.1 -P 3306 -u root -p
```

---

## 🔒 安全建议

### 1. 修改默认密码

```bash
# 在 .env.local 中设置强密码
DB_PASSWORD=YourStrongPassword123!
REDIS_PASSWORD=YourRedisPassword
JWT_SECRET=YourVeryLongSecretKeyAtLeast256BitsForHS256Algorithm
```

### 2. 生产环境 HTTPS

编辑 `nginx/nginx.conf`，取消注释 HTTPS server 块并配置 SSL 证书：

```bash
# 将证书放入 ssl 目录
cp your-cert.pem packages/docker/nginx/ssl/cert.pem
cp your-key.pem packages/docker/nginx/ssl/key.pem

# 重启 Nginx
docker compose restart nginx
```

### 3. 网络隔离

生产环境建议：
- 不暴露 MySQL 端口（删除 `ports` 映射）
- 使用 Docker 网络隔离
- 考虑使用 `docker secret` 管理敏感信息

---

## 📊 健康检查

所有后端服务都配置了 Spring Actuator 健康检查：

```bash
# 检查所有服务健康状态
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090 8091 8092; do
  echo -n "Port $port: "
  curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health || echo "down"
done
```

---

## 🧹 清理

```bash
# 停止所有服务（保留数据卷）
docker compose stop

# 删除所有容器和网络（保留数据卷）
docker compose down

# 删除所有容器、网络和数据卷（⚠️ 会删除所有数据）
docker compose down -v

# 完全重建
docker compose down -v --rmi all
docker compose --env-file .env.local up --build -d
```

---

## 🐛 常见问题

### 1. Maven 构建失败（依赖下载超时）

```bash
# 增加 Maven 内存并重试
docker compose build --build-arg MAVEN_OPTS="-Xmx1024m" aioa-gateway
```

### 2. MySQL 健康检查超时

MySQL 首次启动较慢，等待 60-90 秒后自动重试。

```bash
docker compose logs mysql
# 查看具体错误
```

### 3. 端口冲突

```bash
# 检查端口占用
netstat -tlnp | grep -E '3306|6379|8080|8000'

# 或修改 .env.local 中的端口映射
```

### 4. 前端 502 Bad Gateway

检查 frontend 是否正确挂载了 dist 目录：

```bash
docker exec aioa-frontend ls /usr/share/nginx/html/
# 应该看到 index.html 和 static/ 目录
```

### 5. AI 服务调用失败

检查 MIMI_API_KEY 是否正确配置：

```bash
docker compose exec aioa-ai env | grep -i api
```

---

## 📝 Dockerfiles 说明

### Dockerfile.app（多阶段构建）

```
Stage 1 (builder):  Maven 3.9 + JDK 17
  - 下载所有依赖（利用 Docker 层缓存）
  - mvn package 构建 JAR

Stage 2 (runtime):  JRE 17 Alpine
  - 仅复制最终 JAR（体积小 ~200MB）
  - 使用 entrypoint.sh 脚本注入环境变量
  - 以非 root 用户运行
```

### Dockerfile.mysql

```
基于 mysql:8.0
  - 自动执行 init/*.sql 创建数据库
  - 配置 UTF8MB4 字符集
  - 设置时区为 Asia/Shanghai
```

---

## ⚙️ 高级配置

### 使用已有的 JAR 文件（跳过构建）

如果 JAR 已构建好，可在 `.env.local` 中指定路径：

```bash
# 在 .env.local 中为每个服务指定 JAR 路径
GATEWAY_JAR=/path/to/aioa-gateway.jar
```

### 资源限制

在 `docker-compose.yml` 中为服务添加资源限制：

```yaml
services:
  aioa-ai:
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 2G
        reservations:
          cpus: '0.5'
          memory: 512M
```

### 外部数据库

使用已有的 MySQL/Redis 实例：

```bash
# 在 .env.local 中
DB_HOST=your-existing-mysql-host
REDIS_HOST=your-existing-redis-host
```

然后从 `docker-compose.yml` 中移除 `mysql` 和 `redis` 服务。
