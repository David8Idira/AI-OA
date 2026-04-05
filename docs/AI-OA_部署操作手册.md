# AI-OA 部署操作手册

> 文档版本：V1.0
> 适用版本：AI-OA V1.0+
> 更新日期：2026-04-05

---

## 目录

1. [部署前准备](#1-部署前准备)
2. [单体部署](#2-单体部署)
3. [微服务部署](#3-微服务部署)
4. [Docker部署](#4-docker部署)
5. [Kubernetes部署](#5-kubernetes部署)
6. [部署验证](#6-部署验证)
7. [常见问题](#7-常见问题)

---

## 1. 部署前准备

### 1.1 环境要求

#### 单体部署（1-2台）

| 项目 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 8核+ | 16核+ |
| 内存 | 32GB | 64GB |
| 系统盘 | 100GB SSD | 200GB SSD |
| 数据盘 | 500GB SSD | 1TB NVMe SSD |
| 网络 | 1Gbps | 1Gbps BGP |

#### 微服务部署（8-12台）

| 角色 | 数量 | CPU | 内存 | 磁盘 |
|------|------|-----|------|------|
| 负载均衡 | 2 | 8核 | 16GB | 100GB SSD |
| 应用服务器 | 6 | 16核 | 32GB | 200GB SSD |
| 数据库服务器 | 2 | 16核 | 64GB | 500GB SSD RAID10 |
| Redis | 3 | 8核 | 32GB | 100GB SSD |
| RabbitMQ | 3 | 8核 | 16GB | 200GB SSD |
| MinIO | 4 | 8核 | 16GB | 2TB HDD |

#### Kubernetes部署（15-20+台）

| 节点类型 | 数量 | CPU | 内存 | 磁盘 |
|----------|------|-----|------|------|
| Master | 3 | 8核 | 16GB | 100GB SSD |
| Worker | 6-10 | 16核 | 32GB | 200GB SSD |
| 数据库节点 | 3 | 16核 | 64GB | 500GB SSD |
| 存储节点 | 4 | 8核 | 16GB | 2TB HDD × 4 |

### 1.2 操作系统支持

| 操作系统 | 版本 | 状态 |
|----------|------|------|
| CentOS | 7/8 | ✅ 支持 |
| RHEL | 7/8 | ✅ 支持 |
| Rocky Linux | 8+ | ✅ 支持 |
| Ubuntu | 20.04/22.04 | ✅ 支持 |
| Debian | 10/11 | ✅ 支持 |

### 1.3 前置检查

```bash
# 1. 检查系统版本
cat /etc/os-release

# 2. 检查CPU核心数
nproc

# 3. 检查内存
free -h

# 4. 检查磁盘空间
df -h

# 5. 检查网络
curl -I https://www.baidu.com

# 6. 检查端口占用
netstat -tlnp | grep -E '80|443|3306|6379|8080'
```

### 1.4 端口规划

| 端口 | 服务 | 说明 |
|------|------|------|
| 80/443 | Nginx | Web入口 |
| 3306 | MySQL | 数据库 |
| 6379 | Redis | 缓存 |
| 8080 | 应用 | API服务 |
| 5672 | RabbitMQ | 消息队列 |
| 9000 | MinIO | 对象存储 |
| 15672 | RabbitMQ管理 | 管理界面 |

---

## 2. 单体部署

### 2.1 一键部署

```bash
# 1. 下载部署包
wget https://github.com/David8Idira/AI-OA/releases/download/v1.0/AI-OA-standalone-v1.0.tar.gz

# 2. 解压
tar -xzvf AI-OA-standalone-v1.0.tar.gz
cd standalone

# 3. 配置
vim config/application.yml

# 4. 执行部署
chmod +x scripts/deploy.sh
sudo ./scripts/deploy.sh --env prod

# 5. 查看日志
tail -f /tmp/aioa_install_*.log
```

### 2.2 手动部署

#### 步骤1：安装JDK

```bash
# CentOS/RHEL
yum install -y java-17-openjdk java-17-openjdk-devel

# Ubuntu/Debian
apt-get install -y openjdk-17-jdk

# 验证
java -version
```

#### 步骤2：安装MySQL

```bash
# CentOS 8
yum install -y https://dev.mysql.com/get/mysql80-community-release-el8-7.noarch.rpm
yum install -y mysql-community-server

# 启动
systemctl enable mysqld
systemctl start mysqld

# 获取临时密码
grep 'temporary password' /var/log/mysqld.log

# 登录并修改密码
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourPassword123!';
CREATE DATABASE aioa DEFAULT CHARACTER SET utf8mb4;
```

#### 步骤3：安装Redis

```bash
# 安装
yum install -y redis  # CentOS
apt-get install -y redis-server  # Ubuntu

# 配置
sed -i 's/bind 127.0.0.1/bind 0.0.0.0/' /etc/redis.conf
sed -i 's/requirepass.*/requirepass RedisPassword123/' /etc/redis.conf

# 启动
systemctl enable redis
systemctl start redis
```

#### 步骤4：安装Nginx

```bash
# 安装
yum install -y nginx  # CentOS
apt-get install -y nginx  # Ubuntu

# 配置
cp nginx.conf /etc/nginx/nginx.conf

# 启动
systemctl enable nginx
systemctl start nginx
```

#### 步骤5：部署应用

```bash
# 创建目录
mkdir -p /opt/aioa
mkdir -p /var/www/aioa
mkdir -p /var/log/aioa

# 上传JAR包
cp aioa-app.jar /opt/aioa/

# 配置
cp application.yml /opt/aioa/

# 启动
nohup java -Xms4g -Xmx8g -jar /opt/aioa/aioa-app.jar \
  --spring.config.location=/opt/aioa/application.yml \
  > /var/log/aioa/stdout.log 2>&1 &

# 检查
curl http://localhost:8080/actuator/health
```

---

## 3. 微服务部署

### 3.1 服务器规划

```
VIP: 192.168.1.100 (Keepalived)

192.168.1.101 - Nginx LB
192.168.1.111 - MySQL Master
192.168.1.112 - MySQL Slave
192.168.1.121-123 - Redis Cluster
192.168.1.131-133 - RabbitMQ Cluster
192.168.1.141-144 - MinIO分布式
192.168.1.151-156 - 应用服务器
```

### 3.2 部署顺序

```
1. 基础环境配置 (所有服务器)
       ↓
2. MySQL主备集群部署
       ↓
3. Redis Cluster部署
       ↓
4. RabbitMQ集群部署
       ↓
5. MinIO分布式部署
       ↓
6. Nginx+Keepalived部署
       ↓
7. 微服务JAR部署
       ↓
8. 健康检查
```

### 3.3 集群创建命令

#### Redis集群创建

```bash
# 在任意Redis节点执行
redis-cli -a 'RedisPassword123' --cluster create \
  192.168.1.121:6379 \
  192.168.1.122:6379 \
  192.168.1.123:6379 \
  --cluster-replicas 0
```

#### RabbitMQ集群创建

```bash
# 在所有节点安装后，执行
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl join_cluster rabbit@192.168.1.131
rabbitmqctl start_app

# 创建高可用队列
rabbitmqctl set_policy ha-all "^aioa\." '{"ha-mode":"all"}'
```

### 3.4 微服务启动

```bash
# Gateway
nohup java -Xms2g -Xmx4g -jar /opt/aioa/gateway/aioa-gateway.jar \
  --spring.config.location=/opt/aioa/gateway/application.yml \
  > /var/log/aioa/gateway.log 2>&1 &

# User Service
nohup java -Xms2g -Xmx4g -jar /opt/aioa/user/aioa-user.jar \
  --spring.config.location=/opt/aioa/user/application.yml \
  > /var/log/aioa/user.log 2>&1 &

# Workflow Service
nohup java -Xms2g -Xmx4g -jar /opt/aioa/workflow/aioa-workflow.jar \
  --spring.config.location=/opt/aioa/workflow/application.yml \
  > /var/log/aioa/workflow.log 2>&1 &
```

---

## 4. Docker部署

### 4.1 环境要求

| 项目 | 最低配置 | 推荐配置 |
|------|----------|----------|
| Docker | 20.10+ | 20.10+ |
| Docker Compose | 2.0+ | 2.0+ |
| CPU | 4核 | 8核 |
| 内存 | 8GB | 16GB |
| 磁盘 | 100GB | 200GB |

### 4.2 快速部署

```bash
# 1. 下载
wget https://github.com/David8Idira/AI-OA/releases/download/v1.0/AI-OA-docker-v1.0.tar.gz

# 2. 解压
tar -xzvf AI-OA-docker-v1.0.tar.gz
cd docker

# 3. 配置环境变量
cp .env.example .env
vim .env

# 4. 启动服务
chmod +x scripts/start.sh
sudo ./scripts/start.sh

# 5. 查看状态
docker-compose ps

# 6. 查看日志
docker-compose logs -f
```

### 4.3 Docker管理命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 进入容器
docker exec -it aioa-mysql bash

# 查看日志
docker-compose logs -f [服务名]

# 重建单个服务
docker-compose up -d --force-recreate mysql
```

---

## 5. Kubernetes部署

### 5.1 环境要求

| 项目 | 版本 |
|------|------|
| Kubernetes | 1.24+ |
| Helm | 3.10+ |
| kubectl | 1.24+ |

### 5.2 快速部署

```bash
# 1. 下载
wget https://github.com/David8Idira/AI-OA/releases/download/v1.0/AI-OA-k8s-v1.0.tar.gz

# 2. 解压
tar -xzvf AI-OA-k8s-v1.0.tar.gz
cd k8s

# 3. 修改配置
vim config/secret.yaml  # 修改密码

# 4. 创建命名空间
kubectl create namespace aioa

# 5. 一键部署
chmod +x scripts/deploy.sh
./scripts/deploy.sh

# 6. 查看状态
kubectl get pods -n aioa
kubectl get svc -n aioa
```

### 5.3 Helm部署

```bash
# 添加Helm仓库
helm repo add aioa ./helm/aioa
helm repo update

# 安装
helm install aioa aioa/aioa -n aioa --create-namespace

# 查看状态
helm status aioa -n aioa

# 卸载
helm uninstall aioa -n aioa
```

### 5.4 K8s管理命令

```bash
# 查看Pod
kubectl get pods -n aioa

# 查看日志
kubectl logs -f deployment/aioa -n aioa

# 进入容器
kubectl exec -it deployment/aioa -n aioa -- /bin/bash

# 扩缩容
kubectl scale deployment/aioa --replicas=5 -n aioa

# 更新配置
kubectl apply -f configmap/app-config.yaml -n aioa
```

---

## 6. 部署验证

### 6.1 服务状态检查

```bash
# 检查所有服务端口
for port in 80 3306 6379 8080 5672 9000 15672; do
  nc -zv localhost $port 2>/dev/null && echo "✓ Port $port OK" || echo "✗ Port $port FAILED"
done
```

### 6.2 功能验证

| 功能 | 验证方法 | 预期结果 |
|------|----------|----------|
| Web访问 | http://服务器IP | 登录页面 |
| API健康 | http://服务器IP:8080/actuator/health | {"status":"UP"} |
| 数据库连接 | 登录MySQL执行SHOW DATABASES | aioa数据库存在 |
| Redis连接 | redis-cli ping | PONG |
| 文件上传 | MinIO控制台上传文件 | 上传成功 |

### 6.3 性能验证

```bash
# 压力测试
ab -n 1000 -c 100 http://localhost/api/user/list

# 检查响应时间
curl -o /dev/null -s -w "Time: %{time_total}s\n" http://localhost/api/health
```

---

## 7. 常见问题

### 7.1 端口被占用

```bash
# 查找占用端口的进程
netstat -tlnp | grep 8080
# 或
ss -tlnp | grep 8080

# 杀掉进程
kill -9 <PID>
```

### 7.2 内存不足

```bash
# 查看内存使用
free -h

# 增加Swap
dd if=/dev/zero of=/swapfile bs=1M count=2048
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
```

### 7.3 数据库连接失败

```bash
# 检查MySQL状态
systemctl status mysqld

# 检查MySQL日志
tail -f /var/log/mysqld.log

# 检查防火墙
firewall-cmd --list-ports
```

### 7.4 网络不通

```bash
# 检查DNS
nslookup aioa.example.com

# 检查路由
traceroute 8.8.8.8

# 检查防火墙
iptables -L -n
```

---

## 附录A：配置文件说明

### A.1 application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aioa
    username: aioa
    password: AioaPassword123!
  
  redis:
    host: localhost
    port: 6379
    password: RedisPassword123

minio:
  endpoint: http://localhost:9000
  access-key: aioaadmin
  secret-key: MinioPassword123!
```

### A.2 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|---------|
| MYSQL_PASSWORD | MySQL密码 | AioaPassword123! |
| REDIS_PASSWORD | Redis密码 | RedisPassword123 |
| MINIO_PASSWORD | MinIO密码 | MinioPassword123! |
| RABBITMQ_PASSWORD | RabbitMQ密码 | AioaPassword123 |

---

*手册版本：V1.0*
*更新日期：2026-04-05*
