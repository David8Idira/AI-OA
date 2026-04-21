# AI-OA 自动化部署脚本

> 支持跨平台(CentOS/Ubuntu/Debian/macOS)的自动化部署脚本

---

## 目录结构

```
scripts/deploy/
├── README.md              # 本文档
├── common.sh              # 通用函数库
├── deploy.sh              # 主入口脚本
├── deploy-standalone.sh   # 单体部署
├── deploy-microservice.sh # 非容器化微服务部署
├── deploy-docker.sh       # Docker Compose部署
└── deploy-k8s.sh         # Kubernetes部署
```

---

## 快速开始

### 一键部署

```bash
# 克隆项目
git clone https://github.com/David8Idira/AI-OA.git
cd AI-OA/scripts/deploy

# 给脚本执行权限
chmod +x *.sh

# 单体部署 (自动检测OS)
./deploy.sh --plan standalone --env prod

# Docker Compose部署
./deploy.sh --plan docker --os auto --env prod

# Kubernetes部署
./deploy.sh --plan k8s --os ubuntu --env prod
```

---

## 部署方案

| 方案 | 命令 | 适用场景 | 服务器数量 |
|------|------|----------|------------|
| 单体部署 | `--plan standalone` | 中小企业 | 1-2台 |
| 微服务 | `--plan microservice` | 中大型企业 | 8-12台 |
| Docker | `--plan docker` | 开发/测试 | 2-4台 |
| Kubernetes | `--plan k8s` | 大型企业 | 15-20+台 |

---

## 支持的操作系统

| 操作系统 | 版本 | 支持状态 |
|----------|------|----------|
| CentOS | 7/8 | ✅ 支持 |
| RHEL | 7/8 | ✅ 支持 |
| Rocky Linux | 8+ | ✅ 支持 |
| Ubuntu | 20.04/22.04 | ✅ 支持 |
| Debian | 10/11 | ✅ 支持 |
| macOS | Intel/Apple Silicon | ⚠️ 部分支持 |

---

## 使用方法

### 通用参数

| 参数 | 说明 | 可选值 |
|------|------|--------|
| `--plan` | 部署方案 | `standalone`, `microservice`, `docker`, `k8s` |
| `--os` | 操作系统 | `auto`, `centos`, `ubuntu`, `debian`, `macos` |
| `--env` | 部署环境 | `dev`, `test`, `prod` |
| `--help` | 显示帮助 | - |

### 示例命令

```bash
# 1. 单体部署 (自动检测系统)
./deploy.sh --plan standalone --env prod

# 2. 微服务部署 (Ubuntu)
./deploy.sh --plan microservice --os ubuntu --env prod

# 3. Docker Compose部署 (CentOS)
./deploy.sh --plan docker --os centos --env dev

# 4. Kubernetes部署 (自动检测)
./deploy.sh --plan k8s --env prod
```

---

## 单体部署 (standalone)

适用场景：中小企业，低并发(<100用户)

```bash
./deploy.sh --plan standalone --env prod
```

**部署内容**：
- MySQL 8.0
- Redis 7.0
- MinIO
- Kafka (可选)
- n8n
- Nginx
- AI-OA 应用

---

## 微服务部署 (microservice)

适用场景：中大型企业，传统部署(100-500用户)

```bash
./deploy.sh --plan microservice --env prod
```

**部署内容**：
- MySQL 主备集群
- Redis Cluster
- RabbitMQ 集群
- MinIO 分布式
- Nginx + Keepalived
- 6个微服务 (Gateway/User/Workflow/Report/AI/Chat)

---

## Docker Compose部署

适用场景：开发/测试环境(<200用户)

```bash
./deploy.sh --plan docker --env dev
```

**部署内容**：
- Docker + Docker Compose
- MySQL 容器
- Redis 容器
- MinIO 容器
- RabbitMQ 容器
- n8n 容器
- AI-OA 应用容器
- Nginx 容器

---

## Kubernetes部署

适用场景：大型企业，高可用(1000+用户)

```bash
./deploy.sh --plan k8s --env prod
```

**部署内容**：
- kubeadm 集群
- Calico 网络插件
- MySQL StatefulSet
- Redis StatefulSet
- RabbitMQ StatefulSet
- MinIO StatefulSet
- AI-OA Deployment + HPA
- Ingress Controller

---

## 环境变量

部署前可设置以下环境变量：

```bash
# MySQL
export MYSQL_ROOT_PASSWORD="ChangeMe123!"
export MYSQL_PASSWORD="AioaPassword123!"

# Redis
export REDIS_PASSWORD="RedisPassword123"

# MinIO
export MINIO_USER="aioaadmin"
export MINIO_PASSWORD="MinioPassword123!"

# RabbitMQ
export RABBITMQ_USER="aioa"
export RABBITMQ_PASSWORD="AioaPassword123"

# n8n
export N8N_USER="admin"
export N8N_PASSWORD="N8nPassword123"
```

---

## 故障排查

### 查看日志

```bash
# 安装日志
cat /tmp/aioa_install_*.log

# 服务状态
systemctl status mysqld
systemctl status redis
systemctl status nginx
systemctl status docker
```

### 常见问题

| 问题 | 解决方案 |
|------|----------|
| 端口被占用 | 检查 `netstat -tlnp | grep <端口>` |
| 权限不足 | 使用 root 或 sudo 运行 |
| 内存不足 | 增加服务器内存或调整 JVM 参数 |
| 网络不通 | 检查防火墙和安全组规则 |

---

## 卸载

```bash
# 单体部署卸载
./deploy-standalone.sh uninstall

# Docker卸载
cd /opt/aioa/docker
docker-compose down -v

# 微服务卸载 (手动)
systemctl stop mysqld redis nginx
rm -rf /opt/aioa /data/minio /var/log/aioa
```

---

*最后更新：2026-04-05*
