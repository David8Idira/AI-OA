#!/bin/bash
#===============================================================================
# AI-OA 单体部署脚本
# 
# 适用场景: 中小企业，低并发 (<100用户)
# 服务器数量: 1-2台
# 
# 功能:
#   - MySQL 8.0 安装配置
#   - Redis 7.0 安装配置
#   - MinIO 安装配置
#   - Kafka 单节点安装配置 (可选)
#   - n8n 安装配置
#   - Nginx 安装配置
#   - AI-OA 应用部署
#===============================================================================

set -e

# 加载通用函数库
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

# 配置变量
MYSQL_VERSION="8.0.35"
MYSQL_ROOT_PASSWORD="ChangeMe123!"
MYSQL_DATABASE="aioa"
MYSQL_USER="aioa"
MYSQL_PASSWORD="AioaPassword123!"

REDIS_PASSWORD="RedisPassword123"

MINIO_ROOT_USER="aioaadmin"
MINIO_ROOT_PASSWORD="MinioPassword123!"

N8N_ADMIN_EMAIL="admin@aioa.com"
N8N_ADMIN_PASSWORD="N8nPassword123!"

#-------------------------------------------------------------------------------
# 安装MySQL
#-------------------------------------------------------------------------------
install_mysql() {
    info "安装MySQL ${MYSQL_VERSION}..."
    
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y https://dev.mysql.com/get/mysql80-community-release-el8-7.noarch.rpm >> "$INSTALL_LOG" 2>&1
            yum install -y mysql-community-server >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            wget -c https://dev.mysql.com/get/mysql-apt-config_0.8.22-1_all.deb -O /tmp/mysql.deb >> "$INSTALL_LOG" 2>&1
            dpkg -i /tmp/mysql.deb >> "$INSTALL_LOG" 2>&1
            apt-get update -y >> "$INSTALL_LOG" 2>&1
            apt-get install -y mysql-community-server >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    systemctl enable mysqld
    systemctl start mysqld
    
    # 等待MySQL启动
    sleep 10
    
    # 获取临时密码
    local temp_password=$(grep 'temporary password' /var/log/mysqld.log 2>/dev/null | awk '{print $NF}' || echo "")
    
    # 修改root密码并创建数据库
    mysql -u root -p"${temp_password}" --connect-expired-password << EOF >> "$INSTALL_LOG" 2>&1 || true
ALTER USER 'root'@'localhost' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}';
CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER '${MYSQL_USER}'@'localhost' IDENTIFIED BY '${MYSQL_PASSWORD}';
GRANT ALL PRIVILEGES ON ${MYSQL_DATABASE}.* TO '${MYSQL_USER}'@'localhost';
FLUSH PRIVILEGES;
EOF
    
    success "MySQL安装完成"
}

#-------------------------------------------------------------------------------
# 安装Redis
#-------------------------------------------------------------------------------
install_redis() {
    info "安装Redis 7.0..."
    
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y redis >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            apt-get install -y redis-server >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    # 配置Redis
    cat > /etc/redis.conf << EOF
bind 127.0.0.1
port 6379
requirepass ${REDIS_PASSWORD}
maxmemory 4gb
maxmemory-policy allkeys-lru
appendonly yes
save ""
EOF
    
    systemctl enable redis
    systemctl restart redis
    
    success "Redis安装完成"
}

#-------------------------------------------------------------------------------
# 安装MinIO
#-------------------------------------------------------------------------------
install_minio() {
    info "安装MinIO..."
    
    wget -c https://dl.min.io/server/minio/release/linux-amd64/minio -O /usr/local/bin/minio >> "$INSTALL_LOG" 2>&1
    chmod +x /usr/local/bin/minio
    
    mkdir -p /data/minio
    
    cat > /etc/default/minio << EOF
MINIO_ROOT_USER=${MINIO_ROOT_USER}
MINIO_ROOT_PASSWORD=${MINIO_ROOT_PASSWORD}
MINIO_VOLUMES="/data/minio"
EOF
    
    cat > /etc/systemd/system/minio.service << 'MINIO_EOF'
[Unit]
Description=MinIO
After=network.target

[Service]
ExecStart=/usr/local/bin/minio server /data/minio --console-address ":9001"
User=root
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
MINIO_EOF
    
    systemctl enable minio
    systemctl start minio
    
    success "MinIO安装完成"
}

#-------------------------------------------------------------------------------
# 安装Kafka (可选)
#-------------------------------------------------------------------------------
install_kafka() {
    info "安装Kafka..."
    
    KAFKA_VERSION="3.6.0"
    wget -c https://downloads.apache.org/kafka/${KAFKA_VERSION}/kafka_2.13-${KAFKA_VERSION}.tgz -O /tmp/kafka.tgz >> "$INSTALL_LOG" 2>&1
    tar -xzf /tmp/kafka.tgz -C /opt/ >> "$INSTALL_LOG" 2>&1
    ln -sf /opt/kafka_2.13-${KAFKA_VERSION} /opt/kafka
    
    mkdir -p /var/lib/kafka/logs
    
    cat > /opt/kafka/config/server.properties << 'EOF'
broker.id=0
listeners=PLAINTEXT://0.0.0.0:9092
advertised.listeners=PLAINTEXT://localhost:9092
log.dirs=/var/lib/kafka/logs
num.partitions=3
zookeeper.connect=localhost:2181
EOF
    
    # 启动Zookeeper
    nohup /opt/kafka/bin/zookeeper-server-start.sh -daemon /opt/kafka/config/zookeeper.properties >> "$INSTALL_LOG" 2>&1
    sleep 5
    
    # 启动Kafka
    nohup /opt/kafka/bin/kafka-server-start.sh -daemon /opt/kafka/config/server.properties >> "$INSTALL_LOG" 2>&1
    
    success "Kafka安装完成"
}

#-------------------------------------------------------------------------------
# 安装n8n
#-------------------------------------------------------------------------------
install_n8n() {
    info "安装n8n..."
    
    # 安装Node.js
    curl -fsSL https://rpm.nodesource.com/setup_20.x | bash - >> "$INSTALL_LOG" 2>&1 || true
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y nodejs >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            curl -fsSL https://deb.nodesource.com/setup_20.x | bash - >> "$INSTALL_LOG" 2>&1
            apt-get install -y nodejs >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    npm install -g n8n >> "$INSTALL_LOG" 2>&1
    
    mkdir -p /data/n8n
    
    cat > /etc/systemd/system/n8n.service << 'N8N_EOF'
[Unit]
Description=n8n
After=network.target

[Service]
ExecStart=/usr/bin/n8n start
WorkingDirectory=/data/n8n
User=root
Restart=always
RestartSec=10
Environment=DB_TYPE=sqlite
N8N_PORT=5678
N8N_PROTOCOL=http
N8N_HOST=localhost

[Install]
WantedBy=multi-user.target
N8N_EOF
    
    systemctl enable n8n
    systemctl start n8n
    
    success "n8n安装完成"
}

#-------------------------------------------------------------------------------
# 安装Nginx
#-------------------------------------------------------------------------------
install_nginx() {
    info "安装Nginx..."
    
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y nginx >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            apt-get install -y nginx >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    mkdir -p /var/www/aioa
    
    cat > /etc/nginx/conf.d/aioa.conf << 'EOF'
server {
    listen 80;
    server_name _;
    
    client_max_body_size 100M;
    
    location / {
        root /var/www/aioa;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
    location /ws/ {
        proxy_pass http://127.0.0.1:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
    
    location /n8n/ {
        proxy_pass http://127.0.0.1:5678/;
        proxy_set_header Host $host;
    }
}
EOF
    
    systemctl enable nginx
    systemctl restart nginx
    
    success "Nginx安装完成"
}

#-------------------------------------------------------------------------------
# 部署AI-OA应用
#-------------------------------------------------------------------------------
deploy_application() {
    info "部署AI-OA应用..."
    
    mkdir -p /opt/aioa
    mkdir -p /var/log/aioa
    
    # 创建配置文件
    cat > /opt/aioa/application.yml << 'EOF'
server:
  port: 8080
  tomcat:
    max-threads: 200
    connection-timeout: 20000

spring:
  application:
    name: aioa
  
  datasource:
    url: jdbc:mysql://localhost:3306/aioa?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: aioa
    password: AioaPassword123!
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  
  redis:
    host: localhost
    port: 6379
    password: RedisPassword123
    database: 0
  
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 100MB

minio:
  endpoint: http://localhost:9000
  accessKey: aioaadmin
  secretKey: MinioPassword123!
  bucket: aioa

ai:
  openai:
    api-key: ${OPENAI_API_KEY:your-api-key}
    base-url: https://api.openai.com

kafka:
  bootstrap-servers: localhost:9092

n8n:
  url: http://localhost:5678
  api-key: ${N8N_API_KEY:}

logging:
  file:
    name: /var/log/aioa/application.log
EOF
    
    success "AI-OA配置文件已创建"
    info "请上传AI-OA JAR包到 /opt/aioa/ 并执行启动"
}

#-------------------------------------------------------------------------------
# 验证部署
#-------------------------------------------------------------------------------
verify_installation() {
    info "验证部署..."
    
    local services=("mysqld" "redis" "nginx" "minio")
    local all_ok=true
    
    for svc in "${services[@]}"; do
        if systemctl is-active --quiet "$svc" 2>/dev/null; then
            success "$svc: 运行中"
        else
            warn "$svc: 未运行或未安装"
            all_ok=false
        fi
    done
    
    # 检查端口
    info "检查端口监听..."
    netstat -tlnp 2>/dev/null | grep -E ':(3306|6379|9000|8080|80)\s' || ss -tlnp | grep -E ':(3306|6379|9000|8080|80)\s'
    
    if [ "$all_ok" = true ]; then
        success "所有服务验证通过"
    else
        warn "部分服务验证失败，请检查日志"
    fi
}

#-------------------------------------------------------------------------------
# 卸载
#-------------------------------------------------------------------------------
uninstall() {
    warn "开始卸载AI-OA..."
    
    systemctl stop mysqld redis nginx minio n8n 2>/dev/null || true
    systemctl disable mysqld redis nginx minio n8n 2>/dev/null || true
    
    rm -rf /opt/aioa /var/www/aioa /data/minio /data/n8n /var/log/aioa
    
    success "卸载完成"
}

#-------------------------------------------------------------------------------
# 主函数
#-------------------------------------------------------------------------------
standalone_main() {
    info "========== AI-OA 单体部署 =========="
    
    case "$1" in
        uninstall)
            uninstall
            ;;
        *)
            install_mysql
            install_redis
            install_minio
            install_kafka
            install_n8n
            install_nginx
            deploy_application
            verify_installation
            ;;
    esac
}

# 如果直接运行此脚本
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    standalone_main "$@"
fi
