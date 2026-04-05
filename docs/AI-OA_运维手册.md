# AI-OA 运维手册

> 文档版本：V1.0
> 适用版本：AI-OA V1.0+
> 更新日期：2026-04-05

---

## 目录

1. [日常运维](#1-日常运维)
2. [监控告警](#2-监控告警)
3. [备份恢复](#3-备份恢复)
4. [性能优化](#4-性能优化)
5. [安全管理](#5-安全管理)
6. [故障处理](#6-故障处理)
7. [扩容指南](#7-扩容指南)
8. [日志管理](#8-日志管理)

---

## 1. 日常运维

### 1.1 日常检查清单

```bash
# 每日执行
#!/bin/bash
echo "=== AI-OA 日常检查 ==="

# 1. 检查服务状态
echo "[1] 服务状态"
systemctl status nginx mysqld redis rabbitmq minio

# 2. 检查端口
echo "[2] 端口状态"
for port in 80 3306 6379 8080 5672 9000; do
  nc -zv localhost $port 2>/dev/null && echo "✓ Port $port OK" || echo "✗ Port $port FAILED"
done

# 3. 检查磁盘
echo "[3] 磁盘使用"
df -h | grep -E '/$|/data'

# 4. 检查内存
echo "[4] 内存使用"
free -h

# 5. 检查CPU
echo "[5] CPU负载"
uptime

# 6. 检查日志错误
echo "[6] 最近错误"
tail -50 /var/log/aioa/error.log 2>/dev/null | grep ERROR
```

### 1.2 定期维护任务

| 周期 | 任务 | 说明 |
|------|------|------|
| 每日 | 服务状态检查 | 检查所有服务是否正常 |
| 每日 | 日志清理 | 清理超过7天的日志 |
| 每周 | 数据库优化 | OPTIMIZE TABLE |
| 每月 | 备份验证 | 验证备份完整性 |
| 每月 | 安全更新 | 系统补丁更新 |

### 1.3 服务管理命令

```bash
# 单体部署
systemctl start|stop|restart aioa     # 启动/停止/重启应用
systemctl status aioa                # 查看状态

# MySQL
systemctl start|stop|restart mysqld
mysql -u root -p -e "SHOW DATABASES;"

# Redis
systemctl start|stop|restart redis
redis-cli -a 'RedisPassword123' ping

# Nginx
systemctl start|stop|restart nginx
nginx -t                               # 测试配置

# RabbitMQ
systemctl start|stop|restart rabbitmq-server
rabbitmqctl status

# MinIO
systemctl start|stop|restart minio
mc admin info local
```

---

## 2. 监控告警

### 2.1 监控指标

| 指标类别 | 指标项 | 告警阈值 | 严重程度 |
|----------|--------|----------|----------|
| **系统** | CPU使用率 | >80% | 警告 |
| **系统** | 内存使用率 | >85% | 警告 |
| **系统** | 磁盘使用率 | >90% | 严重 |
| **数据库** | 连接数 | >80%max | 警告 |
| **数据库** | 慢查询 | >100/分钟 | 警告 |
| **缓存** | Redis内存 | >80% | 警告 |
| **队列** | 消息堆积 | >10000 | 警告 |
| **应用** | JVM堆使用 | >80% | 警告 |
| **应用** | API响应时间 | >500ms | 警告 |

### 2.2 告警配置

```bash
# 钉钉机器人告警配置
# 在 application.yml 中配置
dingtalk:
  webhook: https://oapi.dingtalk.com/robot/send?access_token=xxx
  secret: xxxx
  at-mobiles:
    - 13800138000
```

### 2.3 监控命令

```bash
# 查看实时资源使用
top -c
htop

# 查看IO
iostat -x 1 5
iotop

# 查看网络连接
netstat -an | wc -l
ss -s

# 查看进程
ps aux | grep java
ps aux | grep nginx
```

---

## 3. 备份恢复

### 3.1 备份策略

| 备份类型 | 周期 | 保留时间 | 存储位置 |
|----------|------|----------|----------|
| 全量备份 | 每天凌晨2点 | 30天 | 本地+异地 |
| 增量备份 | 每6小时 | 7天 | 本地 |
| 实时备份 | 实时 | - | MinIO |
| 配置备份 | 每次变更 | 90天 | 本地 |

### 3.2 数据库备份

```bash
#!/bin/bash
# 备份脚本 - 每天凌晨2点执行
BACKUP_DIR="/backup/aioa"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# MySQL全量备份
mysqldump -u root -p'YourPassword123!' \
  --single-transaction \
  --routines --triggers \
  aioa | gzip > $BACKUP_DIR/aioa_$DATE.sql.gz

# 保留30天
find $BACKUP_DIR -name "aioa_*.sql.gz" -mtime +30 -delete

# 输出日志
echo "[$(date)] 备份完成: aioa_$DATE.sql.gz"
```

### 3.3 Redis备份

```bash
#!/bin/bash
# Redis备份 - 每天凌晨3点执行
redis-cli -a 'RedisPassword123' BGSAVE
sleep 30
cp /var/lib/redis/dump.rdb /backup/redis/redis_$(date +%Y%m%d).rdb
```

### 3.4 文件备份

```bash
#!/bin/bash
# MinIO数据备份 - 每天凌晨3点执行
mc mirror local/aioa-files /backup/minio/aioa-files_$(date +%Y%m%d)/
```

### 3.5 恢复操作

```bash
# 恢复MySQL
gunzip < aioa_20260405_020000.sql.gz | mysql -u root -p aioa

# 恢复Redis
systemctl stop redis
cp redis_20260405.rdb /var/lib/redis/dump.rdb
systemctl start redis

# 恢复MinIO
mc rm -r local/aioa-files/
mc mirror /backup/minio/aioa-files_20260405/ local/aioa-files/
```

---

## 4. 性能优化

### 4.1 JVM调优

```bash
# 推荐JVM参数
JAVA_OPTS="
  -Xms4g                   # 初始堆大小
  -Xmx8g                   # 最大堆大小
  -XX:+UseG1GC            # G1垃圾回收器
  -XX:MaxGCPauseMillis=200  # 最大GC暂停时间
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/aioa/heapdump.hprof
  -Djava.security.egd=file:/dev/./urandom
"
```

### 4.2 MySQL优化

```sql
-- 检查慢查询
SHOW VARIABLES LIKE 'slow_query%';
SHOW VARIABLES LIKE 'long_query_time';

-- 查看当前连接
SHOW STATUS LIKE 'Threads_connected';
SHOW PROCESSLIST;

-- 优化表
OPTIMIZE TABLE user_info;
OPTIMIZE TABLE approval_record;

-- 调整参数
SET GLOBAL max_connections = 1000;
SET GLOBAL innodb_buffer_pool_size = 8G;
```

### 4.3 Nginx优化

```nginx
# nginx.conf 优化
worker_processes auto;
worker_connections 10240;
keepalive_timeout 65;

# Gzip压缩
gzip on;
gzip_types text/plain text/css application/json application/javascript;
gzip_min_length 1000;
```

### 4.4 Redis优化

```bash
# redis.conf 优化
maxmemory 8gb
maxmemory-policy allkeys-lru
appendonly yes
appendfsync everysec
```

---

## 5. 安全管理

### 5.1 账号安全

```bash
# 强密码策略
# - 长度≥12位
# - 包含大小写字母
# - 包含数字
# - 包含特殊字符

# 定期更换密码
# - 应用密码：每90天更换
# - 数据库密码：每180天更换
# - API密钥：每30天更换

# 删除无用账号
DROP USER 'unused_user'@'localhost';
```

### 5.2 防火墙配置

```bash
# 只开放必要端口
firewall-cmd --permanent --add-port=80/tcp      # HTTP
firewall-cmd --permanent --add-port=443/tcp     # HTTPS
firewall-cmd --permanent --add-port=8080/tcp    # API (仅内网)
firewall-cmd --permanent --add-port=9000/tcp    # MinIO (仅内网)

# 内部服务禁止对外
firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="192.168.0.0/16" accept'
firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.0.0.0/8" accept'

firewall-cmd --reload
```

### 5.3 SSL证书配置

```bash
# Let's Encrypt 免费证书
certbot --nginx -d aioa.example.com

# 或使用商业证书
# 将证书放到 /etc/nginx/ssl/
# 修改 nginx.conf
ssl_certificate /etc/nginx/ssl/aioa.crt;
ssl_certificate_key /etc/nginx/ssl/aioa.key;
```

### 5.4 安全日志审计

```bash
# 审计登录日志
grep "Failed password" /var/log/secure | tail -20

# 审计敏感操作
grep "DROP\|DELETE\|TRUNCATE" /var/log/aioa/sql.log

# 审计文件变更
auditctl -w /opt/aioa -p rwxa -k aioa_changes
```

---

## 6. 故障处理

### 6.1 故障等级

| 等级 | 定义 | 响应时间 | 恢复时间 |
|------|------|----------|----------|
| P0 | 系统宕机 | 5分钟 | 1小时 |
| P1 | 核心功能不可用 | 15分钟 | 4小时 |
| P2 | 非核心功能异常 | 1小时 | 24小时 |
| P3 | 轻微问题 | 4小时 | 72小时 |

### 6.2 故障排查流程

```bash
# Step 1: 收集信息
echo "=== 故障时间 ==="
date
echo "=== 服务状态 ==="
systemctl status nginx mysqld redis aioa
echo "=== 端口状态 ==="
netstat -tlnp | grep -E '80|3306|6379|8080'
echo "=== 最近错误日志 ==="
tail -100 /var/log/aioa/error.log | grep ERROR

# Step 2: 检查资源
echo "=== CPU/内存 ==="
free -h
echo "=== 磁盘 ==="
df -h
echo "=== 进程 ==="
ps aux | grep java | head -5

# Step 3: 检查依赖
echo "=== MySQL ==="
mysql -u root -p -e "SELECT 1;"
echo "=== Redis ==="
redis-cli -a 'RedisPassword123' ping
echo "=== MinIO ==="
mc admin info local
```

### 6.3 常见故障处理

#### 故障1：服务无法启动

```bash
# 1. 检查端口占用
netstat -tlnp | grep 8080

# 2. 检查配置文件
java -jar /opt/aioa/aioa.jar --spring.config.location=/opt/aioa/application.yml --dry-run

# 3. 检查日志
tail -500 /var/log/aioa/stdout.log
```

#### 故障2：数据库连接失败

```bash
# 1. 检查MySQL状态
systemctl status mysqld

# 2. 检查连接数
mysql -u root -p -e "SHOW STATUS LIKE 'Max_used_connections';"

# 3. 重启MySQL
systemctl restart mysqld

# 4. 如果是连接池满，等待连接释放或重启应用
```

#### 故障3：Redis连接失败

```bash
# 1. 检查Redis状态
systemctl status redis
redis-cli -a 'RedisPassword123' ping

# 2. 检查内存
redis-cli -a 'RedisPassword123' info memory

# 3. 如果是内存满，执行内存回收
redis-cli -a 'RedisPassword123' BGSAVE
```

#### 故障4：磁盘空间不足

```bash
# 1. 查看磁盘使用
df -h

# 2. 查找大文件
du -sh /* | sort -h | tail -10

# 3. 清理日志
rm -rf /var/log/aioa/*.log
rm -rf /tmp/*.log

# 4. 清理旧备份
find /backup -name "*.gz" -mtime +7 -delete
```

---

## 7. 扩容指南

### 7.1 水平扩容（增加实例）

```bash
# 单体部署扩容为微服务
# 1. 添加应用服务器
# 2. 部署应用实例
# 3. 配置Nginx负载均衡
upstream aioa_backend {
    server 192.168.1.151:8080;
    server 192.168.1.152:8080;
    server 192.168.1.153:8080;
}

# 4. 重载Nginx
nginx -s reload
```

### 7.2 垂直扩容（增加配置）

```bash
# 1. 停止服务
systemctl stop aioa

# 2. 修改JVM参数
# 编辑 /opt/aioa/start.sh
-Xms8g -Xmx16g  # 原来是 4g-8g

# 3. 重启服务
systemctl start aioa
```

### 7.3 数据库扩容

```sql
-- 读写分离配置
-- 主库: 写操作
-- 从库: 读操作

-- 在 application.yml 中配置
spring:
  datasource:
    primary:
      url: jdbc:mysql://master:3306/aioa
    secondary:
      url: jdbc:mysql://slave:3306/aioa
```

---

## 8. 日志管理

### 8.1 日志配置

```yaml
# application.yml
logging:
  file:
    name: /var/log/aioa/application.log
    max-size: 100MB
    max-history: 30
  level:
    root: INFO
    com.aioa: DEBUG
    org.springframework.web: INFO
    com.mysql: WARN
```

### 8.2 日志归档

```bash
#!/bin/bash
# 日志归档 - 每天凌晨1点执行
LOG_DIR="/var/log/aioa"
ARCHIVE_DIR="/var/log/aioa/archive"

mkdir -p $ARCHIVE_DIR

# 归档昨天的日志
mv $LOG_DIR/application.log $ARCHIVE_DIR/application_$(date -d yesterday +%Y%m%d).log

# 压缩归档
gzip $ARCHIVE_DIR/application_*.log

# 保留90天
find $ARCHIVE_DIR -name "*.gz" -mtime +90 -delete
```

### 8.3 日志分析

```bash
# 统计错误数量
grep ERROR /var/log/aioa/application.log | wc -l

# 统计API响应时间
grep "API" /var/log/aioa/application.log | awk '{print $NF}' | sort -n | tail -10

# 统计用户操作
grep "UserAction" /var/log/aioa/application.log | cut -d: -f5 | sort | uniq -c
```

---

## 附录A：应急联系人

| 角色 | 姓名 | 电话 | 邮箱 |
|------|------|------|------|
| 运维负责人 | - | - | - |
| DBA | - | - | - |
| 安全负责人 | - | - | - |
| 厂商支持 | - | - | support@aioa.com |

---

## 附录B：变更记录

| 日期 | 变更内容 | 变更人 |
|------|----------|--------|
| 2026-04-05 | 初始版本 | A1 |

---

*手册版本：V1.0*
*更新日期：2026-04-05*
