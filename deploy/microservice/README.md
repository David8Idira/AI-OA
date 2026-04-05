# AI-OA 非容器化微服务部署方案

> 适用场景：中大型企业，高并发（100-500用户），需要微服务架构但无K8s能力
> 
> 部署模式：传统微服务架构（JAR包部署）

---

## 一、部署架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          AI-OA 微服务部署架构                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────────────────────────────────────────────────────────┐     │
│   │                        Nginx 负载均衡层                            │     │
│   │                    (1台 + Keepalived)                           │     │
│   └──────────────────────────────┬───────────────────────────────────┘     │
│                                 │                                           │
│   ┌────────────────────────────┴────────────────────────────────────┐     │
│   │                      应用服务层（6台服务器）                       │     │
│   ├─────────────┬─────────────┬─────────────┬─────────────┬───────────┤     │
│   │  Server 1   │  Server 2   │  Server 3   │  Server 4   │ Server 5-6│     │
│   │ ┌─────────┐│ ┌─────────┐│ ┌─────────┐│ ┌─────────┐│ ┌───────┐│     │
│   │ │网关服务  ││ │用户服务 ││ │审批服务 ││ │报表服务 ││ │ AI服务 ││     │
│   │ │Gateway  ││ │ System  ││ │Workflow ││ │ Report  ││ │  Chat  ││     │
│   │ └─────────┘│ └─────────┘│ └─────────┘│ └─────────┘│ └───────┘│     │
│   └─────────────┴─────────────┴─────────────┴─────────────┴───────────┘     │
│                                 │                                           │
│   ┌────────────────────────────┴────────────────────────────────────┐     │
│   │                         数据服务层                                  │     │
│   ├─────────────────┬──────────────────┬─────────────────────────────┤     │
│   │    MySQL主备     │   Redis Cluster  │      MinIO 分布式存储        │     │
│   │  (主:3306)      │  (3节点)         │        (4节点)              │     │
│   │  (从:3307)      │  (6379/6380/6381)│                             │     │
│   └─────────────────┴──────────────────┴─────────────────────────────┘     │
│                                                                             │
│   ┌─────────────────┬──────────────────┬─────────────────────────────┐     │
│   │   Kafka（可选高吞吐场景）      │   RabbitMQ集群    │       n8n 集群              │     │
│   │   (3节点)        │    (3节点)       │        (2节点)              │     │
│   └─────────────────┴──────────────────┴─────────────────────────────┘     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 二、服务器规划

### 2.1 硬件配置建议

| 角色 | 数量 | CPU | 内存 | 磁盘 | 说明 |
|------|------|-----|------|------|------|
| 负载均衡 | 2 | 8核 | 16GB | 100GB | Nginx+Keepalived |
| 应用服务 | 6 | 16核 | 32GB | 200GB | 微服务JAR部署 |
| MySQL主 | 1 | 16核 | 64GB | 500GB | SSD RAID10 |
| MySQL从 | 1 | 16核 | 64GB | 500GB | SSD RAID10 |
| Redis | 3 | 8核 | 32GB | 100GB | SSD |
| Kafka | 3 | 8核 | 32GB | 1TB | SSD |
| RabbitMQ | 3 | 8核 | 16GB | 200GB | SSD |
| MinIO | 4 | 8核 | 16GB | 2TB | 大容量HDD |
| n8n | 2 | 4核 | 8GB | 100GB | - |

### 2.2 端口规划

| 服务 | 端口 | 协议 |
|------|------|------|
| Nginx | 80/443 | HTTP/HTTPS |
| Gateway | 8080 | HTTP |
| User Service | 8081 | HTTP |
| Workflow Service | 8082 | HTTP |
| Report Service | 8083 | HTTP |
| AI Service | 8084 | HTTP |
| Chat Service | 8085 | WebSocket |
| MySQL Master | 3306 | TCP |
| MySQL Slave | 3307 | TCP |
| Redis | 6379/6380/6381 | TCP |
| Kafka | 9092/9093/9094 | TCP |
| RabbitMQ | 5672/15672 | AMQP/MQTT |
| MinIO | 9000/9001 | S3/API |

---

## 三、软件依赖

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | OpenJDK 17 LTS |
| MySQL | 8.0.35+ | 主备复制 |
| Redis | 7.0+ | Cluster模式 |
| Kafka | 3.6+ | KRaft模式 |
| RabbitMQ | 3.12+ | 镜像队列 |
| MinIO | 最新版 | 分布式模式 |
| n8n | 1.0+ | 多节点 |
| Nginx | 1.24+ | 带nginx_upstream_check |
| Keepalived | 2.2+ | VIP漂移 |

---

## 四、安装步骤

### 4.1 基础环境配置（所有服务器）

```bash
# 1. 所有服务器执行
hostnamectl set-hostname aioa-app-01  # 按服务器角色修改

# 2. 配置hosts（所有服务器）
cat >> /etc/hosts << 'EOF'
192.168.1.101 aioa-gw-01
192.168.1.102 aioa-gw-02
192.168.1.111 aioa-app-01
192.168.1.112 aioa-app-02
192.168.1.113 aioa-app-03
192.168.1.121 aioa-mysql-master
192.168.1.122 aioa-mysql-slave
192.168.1.131 aioa-redis-01
192.168.1.132 aioa-redis-02
192.168.1.133 aioa-redis-03
192.168.1.141 aioa-kafka-01
192.168.1.142 aioa-kafka-02
192.168.1.143 aioa-kafka-03
192.168.1.151 aioa-minio-01
192.168.1.152 aioa-minio-02
192.168.1.153 aioa-minio-03
192.168.1.154 aioa-minio-04
EOF

# 3. 安装JDK
yum install -y java-17-openjdk java-17-openjdk-devel
java -version

# 4. 创建应用用户
useradd -m -s /bin/bash aioa
mkdir -p /opt/aioa
chown -R aioa:aioa /opt/aioa
```

### 4.2 MySQL 主备集群部署

```bash
# 在 MySQL Master (192.168.1.121) 上执行

# 1. 安装MySQL
yum install -y https://dev.mysql.com/get/mysql80-community-release-el8-7.noarch.rpm
yum install -y mysql-community-server

# 2. 配置my.cnf
cat > /etc/my.cnf << 'EOF'
[mysqld]
server-id=1
log-bin=mysql-bin
binlog-format=ROW
gtid-mode=on
enforce-gtid-consistency=on
innodb_buffer_pool_size=48G
innodb_log_file_size=4G
max_connections=1000
character_set_server=utf8mb4
collation_server=utf8mb4_unicode_ci
EOF

# 3. 启动MySQL
systemctl start mysqld
systemctl enable mysqld

# 4. 创建复制用户
mysql -u root -p
CREATE USER 'repl'@'%' IDENTIFIED BY 'ReplPassword123!';
GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';
CREATE DATABASE aioa DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE aioa_chat DEFAULT CHARACTER SET utf8mb4;
CREATE USER 'aioa'@'%' IDENTIFIED BY 'AioaPassword123!';
GRANT ALL PRIVILEGES ON aioa.* TO 'aioa'@'%';
GRANT ALL PRIVILEGES ON aioa_chat.* TO 'aioa'@'%';
FLUSH PRIVILEGES;

# 5. 在 Slave (192.168.1.122) 配置
cat > /etc/my.cnf << 'EOF'
[mysqld]
server-id=2
relay-log=relay-bin
gtid-mode=on
enforce-gtid-consistency=on
read_only=on
innodb_buffer_pool_size=48G
EOF

systemctl start mysqld

# 6. 配置主从复制
mysql -u root -p
CHANGE MASTER TO
    MASTER_HOST='192.168.1.121',
    MASTER_USER='repl',
    MASTER_PASSWORD='ReplPassword123!',
    MASTER_AUTO_POSITION=1;
START SLAVE;
SHOW SLAVE STATUS\G
```

### 4.3 Redis Cluster 部署

```bash
# 在 Redis-01 (192.168.1.131) 上执行

# 1. 安装Redis
yum install -y redis

# 2. 配置Redis Cluster
cat > /etc/redis.conf << 'EOF'
bind 0.0.0.0
port 6379
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 15000
appendonly yes
requirepass RedisPassword123
masterauth RedisPassword123
EOF

# 3. 启动Redis
systemctl start redis
systemctl enable redis

# 4. 在所有Redis节点启动后，创建集群
redis-cli -a RedisPassword123 --cluster create \
    192.168.1.131:6379 \
    192.168.1.132:6379 \
    192.168.1.133:6379 \
    --cluster-replicas 0

# 验证
redis-cli -c -h 192.168.1.131 -p 6379 -a RedisPassword123 cluster nodes
```

```bash
# 在 Kafka-01 (192.168.1.141) 上执行

# 1. 下载并解压Kafka
wget https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz
tar -xzf kafka_2.13-3.6.0.tgz -C /opt/
ln -s /opt/kafka_2.13-3.6.0 /opt/kafka

# 2. 配置Kafka（每个节点）
cat > /opt/kafka/config/kafka.properties << 'EOF'
node.id=1
process.roles=controller,broker
listeners=PLAINTEXT://192.168.1.141:9092,CONTROLLER://192.168.1.141:9093
inter.broker.listener.name=PLAINTEXT
controller.listener.names=CONTROLLER
listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,SSL:SSL
controller.quorum.voters=1@192.168.1.141:9093,2@192.168.1.142:9093,3@192.168.1.143:9093
log.dirs=/var/lib/kafka/logs
num.partitions=6
offsets.topic.replication.factor=3
transaction.state.log.replication.factor=3
EOF

# 3. 启动Zookeeper（使用KRaft，无需独立Zookeeper）
/opt/kafka/bin/kafka-storage.sh random-uuid
# 使用上面命令生成的UUID
/opt/kafka/bin/kafka-storage.sh format -t <UUID> -c /opt/kafka/config/kafka.properties --ignore-formatted

# 4. 启动Kafka
nohup /opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/kafka.properties > /var/log/kafka.log 2>&1 &

# 5. 创建主题
/opt/kafka/bin/kafka-topics.sh --create --topic aioa-approval --bootstrap-server 192.168.1.141:9092,192.168.1.142:9092,192.168.1.143:9092 --partitions 6 --replication-factor 3
/opt/kafka/bin/kafka-topics.sh --create --topic aioa-chat --bootstrap-server 192.168.1.141:9092,192.168.1.142:9092,192.168.1.143:9092 --partitions 6 --replication-factor 3
```

### 4.5 RabbitMQ 集群部署

```bash
# 在所有RabbitMQ节点安装
yum install -y rabbitmq-server

# 1. 配置Erlang
cat >> /etc/rabbitmq/rabbitmq.conf << 'EOF'
loopback_users.guest = false
listeners.tcp.default = 5672
management.tcp.port = 15672
cluster_formation.peer_discovery_backend = rabbit_peer_discovery_classic_config
cluster_formation.classic_config.nodes.1 = rabbit@aioa-rabbit-01
cluster_formation.classic_config.nodes.2 = rabbit@aioa-rabbit-02
cluster_formation.classic_config.nodes.3 = rabbit@aioa-rabbit-03
EOF

# 2. 启动RabbitMQ
systemctl start rabbitmq-server
systemctl enable rabbitmq-server

# 3. 启用管理插件
rabbitmq-plugins enable rabbitmq_management

# 4. 创建用户和VHost
rabbitmqctl add_user aioa AioaRabbitPassword123
rabbitmqctl set_permissions -p / aioa ".*" ".*" ".*"
rabbitmqctl set_user_tags aioa administrator
```

### 4.6 MinIO 分布式部署

```bash
# 在所有MinIO节点执行

# 1. 下载并安装MinIO
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
mv minio /usr/local/bin/

# 2. 创建数据目录
mkdir -p /data/minio{1..4}

# 3. 配置MinIO（每个节点）
cat > /etc/default/minio << 'EOF'
MINIO_ROOT_USER=aioaadmin
MINIO_ROOT_PASSWORD=MinioPassword123!
MINIO_VOLUMES="http://192.168.1.151:9000/data/minio{1...4} http://192.168.1.152:9000/data/minio{1...4} http://192.168.1.153:9000/data/minio{1...4} http://192.168.1.154:9000/data/minio{1...4}"
MINIO_SERVER_OPTS="--console-address :9001"
EOF

# 4. 创建systemd服务
cat > /etc/systemd/system/minio.service << 'EOF'
[Unit]
Description=MinIO
After=network.target

[Service]
ExecStart=/usr/local/bin/minio server /data/minio{1...4} --console-address ":9001"
User=root
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# 5. 启动MinIO
systemctl start minio
systemctl enable minio

# 6. 创建Bucket
mc alias set aioa http://192.168.1.151:9000 aioaadmin MinioPassword123!
mc mb aioa/aioa-files
mc anonymous set public aioa/aioa-files
```

### 4.7 Nginx + Keepalived 部署

```bash
# 在两台Nginx服务器上执行

# 1. 安装Nginx和Keepalived
yum install -y nginx keepalived

# 2. 配置Keepalived（Nginx Master）
cat > /etc/keepalived/keepalived.conf << 'EOF'
! Configuration File for keepalived
global_defs {
    router_id NGINX_MASTER
}

vrrp_script check_nginx {
    script "/etc/keepalived/check_nginx.sh"
    interval 2
    weight -20
}

vrrp_instance VI_1 {
    state MASTER
    interface eth0
    virtual_router_id 51
    priority 100
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        192.168.1.100/24
    }
    track_script {
        check_nginx
    }
}
EOF

# 3. 健康检查脚本
cat > /etc/keepalived/check_nginx.sh << 'EOF'
#!/bin/bash
if [ `ps -C nginx --no-header | wc -l` -eq 0 ]; then
    systemctl stop keepalived
fi
EOF
chmod +x /etc/keepalived/check_nginx.sh

# 4. 配置Nginx
cat > /etc/nginx/conf.d/aioa.conf << 'EOF'
upstream aioa_gateway {
    least_conn;
    server 192.168.1.111:8080 weight=10;
    server 192.168.1.112:8080 weight=10;
    server 192.168.1.113:8080 weight=10;
    check interval=3000 rise=2 fall=3 timeout=1000 type=http;
    check_http_expect_alive http_2xx;
}

upstream aioa_chat {
    ip_hash;
    server 192.168.1.115:8085;
    server 192.168.1.116:8085;
}

server {
    listen 80;
    server_name aioa.example.com;

    location / {
        root /var/www/aioa;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://aioa_gateway/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /ws/ {
        proxy_pass http://aioa_chat/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 86400;
    }

    location /n8n/ {
        proxy_pass http://192.168.1.131:5678/;
        proxy_set_header Host $host;
    }
}
EOF

# 5. 启动服务
systemctl start nginx keepalived
systemctl enable nginx keepalived
```

### 4.8 微服务JAR部署

```bash
# 在所有应用服务器上为每个服务创建部署

# 创建服务目录
for svc in gateway user workflow report ai chat; do
    mkdir -p /opt/aioa/${svc}
    mkdir -p /var/log/aioa/${svc}
done

# 1. Gateway Service (192.168.1.111)
cat > /opt/aioa/gateway/application.yml << 'EOF'
server:
  port: 8080
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://192.168.1.111:8081
          predicates:
            - Path=/api/user/**
        - id: workflow-service
          uri: http://192.168.1.112:8082
          predicates:
            - Path=/api/workflow/**
        - id: report-service
          uri: http://192.168.1.113:8083
          predicates:
            - Path=/api/report/**
        - id: ai-service
          uri: http://192.168.1.115:8084
          predicates:
            - Path=/api/ai/**
  redis:
    host: 192.168.1.131
    port: 6379
    password: RedisPassword123
EOF

# 启动脚本
cat > /opt/aioa/gateway/start.sh << 'EOF'
#!/bin/bash
nohup java -Xms2g -Xmx4g -XX:+UseG1GC \
    -jar /opt/aioa/gateway/aioa-gateway.jar \
    --spring.config.location=/opt/aioa/gateway/application.yml \
    > /var/log/aioa/gateway/stdout.log 2>&1 &
echo $! > /var/run/aioa-gateway.pid
EOF

# 2. 其他服务类似配置...

# 3. 使用Supervisor管理进程（推荐）
yum install -y supervisor

cat > /etc/supervisord.d/aioa.ini << 'EOF'
[program:aioa-gateway]
command=java -Xms2g -Xmx4g -jar /opt/aioa/gateway/aioa-gateway.jar --spring.config.location=/opt/aioa/gateway/application.yml
directory=/opt/aioa/gateway
user=aioa
autostart=true
autorestart=true
stdout_logfile=/var/log/aioa/gateway/stdout.log
stderr_logfile=/var/log/aioa/gateway/stderr.log
EOF

supervisorctl reread
supervisorctl update
```

---

## 五、服务注册与发现

### 5.1 Consul 部署

```bash
# 下载Consul
wget https://releases.hashicorp.com/consul/1.17.0/consul_1.17.0_linux_amd64.zip
unzip consul_1.17.0_linux_amd64.zip
mv consul /usr/local/bin/

# 启动Consul Server
consul agent -server -bootstrap-expect=3 \
    -node=aioa-consul-01 \
    -bind=192.168.1.141 \
    -data-dir=/var/lib/consul \
    -ui

# 在其他节点启动
consul agent -server -join 192.168.1.141 ...
```

### 5.2 服务配置

```yaml
# application.yml 中添加
spring:
  cloud:
    consul:
      host: 192.168.1.141
      port: 8500
      discovery:
        register: true
        instance-id: ${spring.application.name}:${random.value}
        health-check-path: /actuator/health
```

---

## 六、部署检查清单

### 6.1 服务状态检查

```bash
# 检查所有服务端口
for port in 3306 3307 6379 9092 5672 9000 8500 8080 8081 8082 8083 8084 8085; do
    nc -zv localhost $port 2>/dev/null && echo "Port $port OK" || echo "Port $port FAILED"
done

# 检查进程
ps aux | grep java | grep -v grep
ps aux | grep redis | grep -v grep
ps aux | grep kafka | grep -v grep
```

### 6.2 功能验证

| 服务 | 验证地址 | 预期结果 |
|------|----------|----------|
| Nginx | http://VIP/ | 登录页面 |
| Gateway | http://VIP/api/actuator/health | UP |
| User Service | http://VIP/api/user/health | UP |
| Workflow | http://VIP/api/workflow/health | UP |
| AI Service | http://VIP/api/ai/health | UP |
| Chat Service | ws://VIP/ws/chat | 连接成功 |

---

## 七、性能调优参数

### 7.1 JVM参数

```bash
# Gateway/AI/Chat服务
java -Xms4g -Xmx8g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/var/log/aioa \
     -Djava.security.egd=file:/dev/./urandom

# 数据服务（Report/Workflow）
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200
```

### 7.2 内核参数

```bash
# /etc/sysctl.conf
net.core.somaxconn = 65535
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_fin_timeout = 30
fs.file-max = 1000000
vm.max_map_count = 262144

sysctl -p
```

---

## 八、备份策略

### 8.1 MySQL备份

```bash
# 每天凌晨2点全量备份
0 2 * * * mysqldump -h192.168.1.121 -uaioa -p'AioaPassword123!' \
    --single-transaction --master-data=2 aioa | gzip > /backup/aioa_$(date+\%Y\%m\%d).sql.gz

# 增量备份（每6小时）
0 */6 * * * mysqlbinlog -h192.168.1.121 -uaioa -p'AioaPassword123!' \
    --read-binlog-gtids=purge --stop-never aioa | gzip > /backup/binlog_$(date+\%Y\%m\%d\%H).gz
```

### 8.2 Redis备份

```bash
# 每天凌晨3点备份
0 3 * * * redis-cli -h 192.168.1.131 -a RedisPassword123 BGSAVE && sleep 30 && \
    cp /var/lib/redis/dump.rdb /backup/redis_$(date+\%Y\%m\%d).rdb
```

---

## 九、监控与告警

### 9.1 监控指标

| 类别 | 指标 | 告警阈值 |
|------|------|----------|
| CPU | 使用率 | >80% |
| 内存 | 使用率 | >85% |
| 磁盘 | 使用率 | >90% |
| MySQL | 连接数 | >80%max |
| Redis | 内存使用 | >80% |
| Kafka | 消费延迟 | >1000 |
| 应用 | JVM堆使用 | >80% |

### 9.2 告警配置

```bash
# 接入钉钉机器人
# 在application.yml中配置
dingtalk:
  webhook: https://oapi.dingtalk.com/robot/send?access_token=xxx
  secret: xxxx
```

---

## 十、扩展指南

### 10.1 水平扩展

```bash
# 新增应用服务器
# 1. 安装JDK和配置hosts
# 2. 上传JAR包
# 3. 配置supervisor
# 4. 更新Nginx upstream配置
# 5. 重载Nginx: nginx -s reload
```

### 10.2 垂直扩展

```bash
# 增加JVM内存
# 编辑start.sh，修改-Xms和-Xmx参数
# 重启服务: supervisorctl restart aioa-gateway
```

---

## 十一、故障排查

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| 服务注册失败 | Consul异常 | 检查Consul集群状态 |
| 前端无法访问 | Nginx异常 | 检查Nginx和Keepalived |
| 数据库连接失败 | 网络/密码错误 | 检查防火墙和密码 |
| 消息发送失败 | Kafka异常 | 检查Kafka（可选高吞吐场景）状态 |
| 文件上传失败 | MinIO异常 | 检查MinIO集群和数据盘 |

---

*文档版本：V1.0*
*创建时间：2026-04-05*
*适用版本：AI-OA V1.7+*
