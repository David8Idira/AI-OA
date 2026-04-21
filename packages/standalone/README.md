# AI-OA 单体部署包

## 包含内容

```
standalone/
├── scripts/
│   ├── deploy.sh           # 一键部署脚本
│   ├── install-mysql.sh    # MySQL安装脚本
│   ├── install-redis.sh    # Redis安装脚本
│   ├── install-minio.sh    # MinIO安装脚本
│   ├── install-kafka.sh    # Kafka安装脚本
│   ├── install-n8n.sh      # n8n安装脚本
│   ├── install-nginx.sh    # Nginx安装脚本
│   └── uninstall.sh        # 卸载脚本
├── config/
│   ├── application.yml     # 应用配置模板
│   ├── nginx.conf         # Nginx配置
│   ├── redis.conf         # Redis配置
│   ├── mysql.cnf          # MySQL配置
│   └── minio.conf         # MinIO配置
├── sql/
│   └── init.sql           # 数据库初始化脚本
├── docker/
│   └── Dockerfile          # 可选Docker化部署
├── README.md              # 部署文档
└── VERSION                # 版本号
```

## 快速部署

```bash
# 1. 解压
tar -xzvf AI-OA-standalone-v1.0.tar.gz
cd AI-OA-standalone-v1.0

# 2. 配置
vim config/application.yml

# 3. 执行部署
chmod +x scripts/deploy.sh
sudo ./scripts/deploy.sh --env prod

# 4. 验证
curl http://localhost:80
```

## 环境要求

| 项目 | 最低配置 |
|------|----------|
| CPU | 8核+ |
| 内存 | 32GB |
| 磁盘 | 500GB SSD |
| OS | CentOS 7+ / Ubuntu 20.04+ |

## 服务端口

| 服务 | 端口 |
|------|------|
| Nginx | 80, 443 |
| MySQL | 3306 |
| Redis | 6379 |
| MinIO | 9000, 9001 |
| Kafka | 9092 |
| n8n | 5678 |
| AI-OA | 8080 |

## 默认账号

| 服务 | 账号 | 密码 |
|------|------|------|
| MySQL root | root | (安装时设置) |
| MySQL aioa | aioa | AioaPassword123! |
| Redis | - | RedisPassword123 |
| MinIO | aioaadmin | MinioPassword123! |
| n8n | admin@aioa.com | N8nPassword123! |
| AI-OA | admin | admin123 |

---

版本：1.0.0
更新日期：2026-04-05
