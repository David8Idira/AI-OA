# AI-OA 单体部署方案

> 适用场景：中小企业快速部署，低并发（<100用户），资源有限环境
> 
> 部署模式：All-in-One 单体架构

---

## 一、部署架构

```
┌─────────────────────────────────────────────────────────────┐
│                    AI-OA 单体部署架构                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────────────────────────────────────────────────┐   │
│   │                   应用服务器                          │   │
│   │  ┌─────────┬─────────┬─────────┬─────────┬───────┐ │   │
│   │  │ 用户服务 │ 审批服务 │ 报表服务 │ AI服务  │ 聊天  │ │   │
│   │  │  (Tomcat)│ (Tomcat)│ (Tomcat)│ (Tomcat)│ (Netty)│ │   │
│   │  └─────────┴─────────┴─────────┴─────────┴───────┘ │   │
│   │                      │                                │   │
│   │              ┌───────┴───────┐                      │   │
│   │              │   MySQL 8.0    │                      │   │
│   │              │  (主备复制)    │                      │   │
│   │              └───────────────┘                      │   │
│   └─────────────────────────────────────────────────────┘   │
│                           │                                │
│   ┌───────────┬───────────┴───────────┬───────────────┐   │
│   │   Redis   │      MinIO存储        │   n8n服务     │   │
│   │  (缓存)   │     (文件存储)         │  (工作流)     │   │
│   └───────────┴───────────────────────┴───────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 二、硬件配置要求

### 2.1 最低配置（单台服务器）

| 组件 | 配置 | 说明 |
|------|------|------|
| CPU | 8核+ | 推荐 Intel Xeon / AMD EPYC |
| 内存 | 32GB+ | 建议 DDR4 ECC |
| 系统盘 | 100GB+ | SSD RAID 1 |
| 数据盘 | 500GB+ | SSD 用于MySQL/Redis |
| 网络 | 1Gbps+ | 稳定网络连接 |

### 2.2 推荐配置（单台服务器）

| 组件 | 配置 | 说明 |
|------|------|------|
| CPU | 16核+ | 高并发支持 |
| 内存 | 64GB+ | Redis缓存+应用缓冲 |
| 系统盘 | 200GB+ | SSD RAID 1 |
| 数据盘 | 1TB+ | NVMe SSD |
| 网络 | 1Gbps+ | 独立公网IP |

---

## 三、软件依赖

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | OpenJDK 或 Oracle JDK |
| MySQL | 8.0+ | 建议8.0.35+ |
| Redis | 7.0+ | 缓存+会话存储 |
| MinIO | 最新版 | S3兼容存储 |
| n8n | 1.0+ | 工作流引擎 |
| Nginx | 1.24+ | 反向代理+静态资源 |
| Kafka | 3.6+ | 单节点模式 |
| RabbitMQ | 3.12+ | 消息队列 |

---

## 四、安装步骤

### 4.1 系统环境准备

```bash
# 1. 更新系统
yum update -y  # CentOS/RHEL
apt update -y  # Ubuntu/Debian

# 2. 安装基础软件
yum install -y wget curl vim git unzip jdk-17

# 3. 配置 hosts
cat >> /etc/hosts << EOF
127.0.0.1 aioa-server
EOF
```

### 4.2 MySQL 8.0 安装

```bash
# 1. 安装MySQL
yum install -y https://dev.mysql.com/get/mysql80-community-release-el8-7.noarch.rpm
yum install -y mysql-community-server

# 2. 启动MySQL
systemctl start mysqld
systemctl enable mysqld

# 3. 获取初始密码
grep 'temporary password' /var/log/mysqld.log

# 4. 登录并修改密码
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourPassword123!';

# 5. 创建数据库
CREATE DATABASE aioa DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE aioa_chat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'aioa'@'localhost' IDENTIFIED BY 'YourPassword123!';
GRANT ALL PRIVILEGES ON aioa.* TO 'aioa'@'localhost';
GRANT ALL PRIVILEGES ON aioa_chat.* TO 'aioa'@'localhost';
FLUSH PRIVILEGES;
```

### 4.3 Redis 7.0 安装

```bash
# 1. 安装Redis
yum install -y redis

# 2. 配置Redis
cat > /etc/redis.conf << 'EOF'
bind 127.0.0.1
port 6379
requirepass YourRedisPassword
maxmemory 8gb
maxmemory-policy allkeys-lru
appendonly yes
EOF

# 3. 启动Redis
systemctl start redis
systemctl enable redis
```

### 4.4 MinIO 安装

```bash
# 1. 下载MinIO
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
mv minio /usr/local/bin/

# 2. 创建数据目录
mkdir -p /data/minio

# 3. 配置MinIO
cat > /etc/default/minio << 'EOF'
MINIO_ROOT_USER=aioaadmin
MINIO_ROOT_PASSWORD=YourMinioPassword123
MINIO_VOLUMES="/data/minio"
EOF

# 4. 创建systemd服务
cat > /etc/systemd/system/minio.service << 'EOF'
[Unit]
Description=MinIO
After=network.target

[Service]
ExecStart=/usr/local/bin/minio server /data/minio --console-address ":9001"
User=root
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# 5. 启动MinIO
systemctl start minio
systemctl enable minio
```

### 4.5 Kafka 单节点安装

```bash
# 1. 下载Kafka
wget https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz
tar -xzf kafka_2.13-3.6.0.tgz -C /opt/
ln -s /opt/kafka_2.13-3.6.0 /opt/kafka

# 2. 配置Kafka
cat > /opt/kafka/config/server.properties << 'EOF'
broker.id=0
listeners=PLAINTEXT://localhost:9092
log.dirs=/var/lib/kafka/logs
num.partitions=3
zookeeper.connect=localhost:2181
EOF

# 3. 启动Zookeeper
/opt/kafka/bin/zookeeper-server-start.sh -daemon /opt/kafka/config/zookeeper.properties

# 4. 启动Kafka
/opt/kafka/bin/kafka-server-start.sh -daemon /opt/kafka/config/server.properties
```

### 4.6 n8n 安装

```bash
# 1. 安装Node.js
curl -fsSL https://rpm.nodesource.com/setup_20.x | bash -
yum install -y nodejs

# 2. 安装n8n
npm install -g n8n

# 3. 配置n8n
mkdir -p /data/n8n
cd /data/n8n
n8n start

# 4. 创建systemd服务
cat > /etc/systemd/system/n8n.service << 'EOF'
[Unit]
Description=n8n
After=network.target

[Service]
ExecStart=/usr/bin/n8n start
WorkingDirectory=/data/n8n
User=root
Restart=always
Environment=DB_TYPE=sqlite
N8N_PORT=5678

[Install]
WantedBy=multi-user.target
EOF

systemctl start n8n
systemctl enable n8n
```

### 4.7 Nginx 安装与配置

```bash
# 1. 安装Nginx
yum install -y nginx

# 2. 配置Nginx
cat > /etc/nginx/conf.d/aioa.conf << 'EOF'
server {
    listen 80;
    server_name aioa.example.com;

    # 前端静态资源
    location / {
        root /var/www/aioa;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # WebSocket代理
    location /ws/ {
        proxy_pass http://127.0.0.1:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # n8n代理
    location /n8n/ {
        proxy_pass http://127.0.0.1:5678/;
        proxy_set_header Host $host;
    }
}
EOF

# 3. 启动Nginx
systemctl start nginx
systemctl enable nginx
```

### 4.8 应用部署

```bash
# 1. 创建部署目录
mkdir -p /opt/aioa
mkdir -p /var/www/aioa

# 2. 上传WAR包（由开发提供）
# 假设文件名为 aioa-all-1.0.0.war
cp aioa-all-1.0.0.war /opt/aioa/

# 3. 配置application.yml
cat > /opt/aioa/application.yml << 'EOF'
server:
  port: 8080
  tomcat:
    max-threads: 200
    connection-timeout: 20000

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aioa?useUnicode=true&characterEncoding=utf8
    username: aioa
    password: YourPassword123!
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  redis:
    host: localhost
    port: 6379
    password: YourRedisPassword
    database: 0

  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 100MB

minio:
  endpoint: http://localhost:9000
  accessKey: aioaadmin
  secretKey: YourMinioPassword123
  bucket: aioa

ai:
  openai:
    api-key: your-openai-api-key
    base-url: https://api.openai.com

kafka:
  bootstrap-servers: localhost:9092

n8n:
  url: http://localhost:5678
  api-key: your-n8n-api-key
EOF

# 4. 上传前端
# 假设前端文件在 dist/ 目录
cp -r dist/* /var/www/aioa/

# 5. 启动应用
nohup java -jar /opt/aioa/aioa-all-1.0.0.war --spring.config.location=/opt/aioa/application.yml > /var/log/aioa.log 2>&1 &
```

---

## 五、验证步骤

### 5.1 服务状态检查

```bash
# 检查所有服务状态
systemctl status mysqld
systemctl status redis
systemctl status minio
systemctl status nginx
systemctl status n8n

# 检查端口
netstat -tlnp | grep -E '3306|6379|9000|5678|8080|80'
```

### 5.2 功能验证

| 功能 | 验证方法 | 预期结果 |
|------|----------|----------|
| 登录 | 访问 http://aioa.example.com | 显示登录页面 |
| 用户管理 | 使用管理员账号登录 | 可进入后台 |
| 文件上传 | 上传测试图片 | 上传成功 |
| AI助手 | 提问测试 | 正常回答 |
| 审批流程 | 提交测试审批 | 流程正常 |
| 消息推送 | 发送测试消息 | 实时收到 |

---

## 六、性能调优

### 6.1 JVM参数

```bash
# 编辑启动脚本
cat > /opt/aioa/start.sh << 'EOF'
#!/bin/bash
java -Xms4g -Xmx8g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -jar /opt/aioa/aioa-all-1.0.0.war \
     --spring.config.location=/opt/aioa/application.yml
EOF
chmod +x /opt/aioa/start.sh
```

### 6.2 MySQL优化

```sql
-- 编辑 my.cnf
[mysqld]
innodb_buffer_pool_size = 8G
innodb_log_file_size = 1G
max_connections = 500
slow_query_log = 1
long_query_time = 2
```

### 6.3 Nginx优化

```nginx
# worker进程数
worker_processes auto;

# 开启 gzip
gzip on;
gzip_types text/plain text/css application/json application/javascript;
gzip_min_length 1000;

# 静态资源缓存
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
    expires 30d;
    add_header Cache-Control "public, immutable";
}
```

---

## 七、备份策略

### 7.1 数据库备份

```bash
# 每天凌晨2点执行备份
0 2 * * * mysqldump -u aioa -p'YourPassword123!' aioa | gzip > /backup/aioa_$(date +\%Y\%m\%d).sql.gz
```

### 7.2 文件备份

```bash
# 备份MinIO数据
0 3 * * * rclone sync /data/minio aioa-remote:aioa-backup --exclude "*.tmp"
```

---

## 八、监控配置

### 8.1 应用监控

| 监控项 | 工具 | 端口 |
|--------|------|------|
| Java应用 | JavaMelody | 8080/monitoring |
| MySQL | phpMyAdmin / Prometheus | 3306 |
| Redis | RedisInsight | 6379 |
| MinIO | Console | 9001 |
| n8n | 内置监控 | 5678 |

### 8.2 系统监控

```bash
# 安装监控代理
yum install -y prometheus-node-exporter

# 配置告警（可选）
# 可接入钉钉/企微/飞书机器人通知
```

---

## 九、运维手册

### 9.1 日常维护

```bash
# 查看日志
tail -f /var/log/aioa.log

# 重启应用
systemctl restart aioa

# 查看服务状态
systemctl status mysqld redis nginx minio n8n
```

### 9.2 故障排查

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| 登录失败 | MySQL连接失败 | 检查MySQL状态和密码 |
| 文件上传失败 | MinIO异常 | 检查MinIO服务和磁盘空间 |
| AI无响应 | API配额用尽 | 检查AI服务配置 |
| 审批卡住 | Kafka异常 | 检查Kafka状态 |

---

## 十、卸载步骤

```bash
# 停止所有服务
systemctl stop mysqld redis minio nginx n8n

# 删除服务
systemctl disable mysqld redis minio nginx n8n
rm /etc/systemd/system/mysqld.service
rm /etc/systemd/system/redis.service
rm /etc/systemd/system/minio.service
rm /etc/systemd/system/n8n.service

# 删除数据（谨慎操作）
rm -rf /opt/aioa /var/www/aioa /data/minio /data/n8n
```

---

*文档版本：V1.0*
*创建时间：2026-04-05*
*适用版本：AI-OA V1.7+*
