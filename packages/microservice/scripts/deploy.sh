#!/bin/bash
#===============================================================================
# AI-OA 非容器化微服务部署脚本
# 
# 适用场景: 中大型企业，传统部署 (100-500用户)
# 服务器数量: 8-12台
# 
# 功能:
#   - MySQL主备集群部署
#   - Redis Cluster部署
#   - RabbitMQ集群部署
#   - MinIO分布式部署
#   - Nginx+Keepalived负载均衡
#   - 微服务JAR部署 (6个服务)
#===============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

#-------------------------------------------------------------------------------
# 配置变量 - 根据实际情况修改
#-------------------------------------------------------------------------------
# 服务器IP配置
MASTER_IP="${MASTER_IP:-192.168.1.101}"
MYSQL_MASTER_IP="${MYSQL_MASTER_IP:-192.168.1.111}"
MYSQL_SLAVE_IP="${MYSQL_SLAVE_IP:-192.168.1.112}"
REDIS_01_IP="${REDIS_01_IP:-192.168.1.121}"
REDIS_02_IP="${REDIS_02_IP:-192.168.1.122}"
REDIS_03_IP="${REDIS_03_IP:-192.168.1.123}"
RABBITMQ_01_IP="${RABBITMQ_01_IP:-192.168.1.131}"
RABBITMQ_02_IP="${RABBITMQ_02_IP:-192.168.1.132}"
RABBITMQ_03_IP="${RABBITMQ_03_IP:-192.168.1.133}"
MINIO_01_IP="${MINIO_01_IP:-192.168.1.141}"
MINIO_02_IP="${MINIO_02_IP:-192.168.1.142}"
MINIO_03_IP="${MINIO_03_IP:-192.168.1.143}"
MINIO_04_IP="${MINIO_04_IP:-192.168.1.144}"

# VIP配置
VIP_IP="${VIP_IP:-192.168.1.100}"

# 密码配置
MYSQL_ROOT_PASSWORD="ChangeMe123!"
MYSQL_REPL_PASSWORD="ReplPassword123!"
MYSQL_AIOA_PASSWORD="AioaPassword123!"
REDIS_PASSWORD="RedisPassword123"
RABBITMQ_PASSWORD="AioaPassword123"
MINIO_PASSWORD="MinioPassword123!"

#-------------------------------------------------------------------------------
# 基础环境配置 (所有服务器)
#-------------------------------------------------------------------------------
config_base_env() {
    info "配置基础环境..."
    
    # 关闭防火墙
    disable_firewall
    disable_selinux
    
    # 优化内核
    optimize_kernel
    configure_limits
    configure_timezone
    
    # 安装基础软件
    install_base_packages
    
    # 安装JDK
    install_jdk
    
    success "基础环境配置完成"
}

#-------------------------------------------------------------------------------
# 安装JDK
#-------------------------------------------------------------------------------
install_jdk() {
    info "安装JDK 17..."
    
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y java-17-openjdk java-17-openjdk-devel >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            apt-get install -y openjdk-17-jdk openjdk-17-jre >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java)))
    echo "export JAVA_HOME=$JAVA_HOME" >> /etc/profile.d/java.sh
    echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/profile.d/java.sh
    
    success "JDK安装完成"
}

#-------------------------------------------------------------------------------
# 部署MySQL主备集群
#-------------------------------------------------------------------------------
deploy_mysql_cluster() {
    info "部署MySQL主备集群..."
    
    local is_master=false
    local is_slave=false
    
    if [[ "$MYSQL_MASTER_IP" == "$(hostname -I | awk '{print $1}')" ]]; then
        is_master=true
    elif [[ "$MYSQL_SLAVE_IP" == "$(hostname -I | awk '{print $1}')" ]]; then
        is_slave=true
    fi
    
    if [[ "$is_master" == "false" ]] && [[ "$is_slave" == "false" ]]; then
        info "当前服务器不是MySQL节点，跳过..."
        return 0
    fi
    
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y https://dev.mysql.com/get/mysql80-community-release-el8-7.noarch.rpm >> "$INSTALL_LOG" 2>&1
            yum install -y mysql-community-server >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            wget -c https://dev.mysql.com/get/mysql-apt-config_0.8.22-1_all.deb -O /tmp/mysql.deb >> "$INSTALL_LOG" 2>&1
            dpkg -i /tmp/mysql.deb >> "$INSTALL_LOG" 2>&1
            apt-get update >> "$INSTALL_LOG" 2>&1
            apt-get install -y mysql-community-server >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    if [[ "$is_master" == "true" ]]; then
        cat > /etc/my.cnf << EOF
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
        
        systemctl enable mysqld
        systemctl start mysqld
        
        # 等待MySQL启动
        sleep 10
        
        # 配置MySQL
        mysql -u root -p"${MYSQL_ROOT_PASSWORD}" << EOF >> "$INSTALL_LOG" 2>&1 || true
CREATE USER 'repl'@'%' IDENTIFIED BY '${MYSQL_REPL_PASSWORD}';
GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';
CREATE DATABASE IF NOT EXISTS aioa DEFAULT CHARACTER SET utf8mb4;
CREATE USER 'aioa'@'%' IDENTIFIED BY '${MYSQL_AIOA_PASSWORD}';
GRANT ALL PRIVILEGES ON aioa.* TO 'aioa'@'%';
FLUSH PRIVILEGES;
EOF
        
        success "MySQL Master配置完成"
    fi
    
    if [[ "$is_slave" == "true" ]]; then
        cat > /etc/my.cnf << EOF
[mysqld]
server-id=2
relay-log=relay-bin
gtid-mode=on
enforce-gtid-consistency=on
read_only=on
innodb_buffer_pool_size=48G
EOF
        
        systemctl enable mysqld
        systemctl start mysqld
        
        # 等待MySQL启动
        sleep 10
        
        # 配置主从复制
        mysql -u root -p"${MYSQL_ROOT_PASSWORD}" << EOF >> "$INSTALL_LOG" 2>&1 || true
CHANGE MASTER TO
    MASTER_HOST='${MYSQL_MASTER_IP}',
    MASTER_USER='repl',
    MASTER_PASSWORD='${MYSQL_REPL_PASSWORD}',
    MASTER_AUTO_POSITION=1;
START SLAVE;
SHOW SLAVE STATUS\G
EOF
        
        success "MySQL Slave配置完成"
    fi
}

#-------------------------------------------------------------------------------
# 部署Redis集群
#-------------------------------------------------------------------------------
deploy_redis_cluster() {
    info "部署Redis集群..."
    
    local redis_ip=$(hostname -I | awk '{print $1}')
    local is_redis_node=false
    
    for ip in "$REDIS_01_IP" "$REDIS_02_IP" "$REDIS_03_IP"; do
        if [[ "$ip" == "$redis_ip" ]]; then
            is_redis_node=true
            break
        fi
    done
    
    if [[ "$is_redis_node" == "false" ]]; then
        info "当前服务器不是Redis节点，跳过..."
        return 0
    fi
    
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y redis >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            apt-get install -y redis-server >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    cat > /etc/redis/redis.conf << EOF
bind 0.0.0.0
port 6379
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 15000
appendonly yes
requirepass ${REDIS_PASSWORD}
masterauth ${REDIS_PASSWORD}
EOF
    
    systemctl enable redis
    systemctl restart redis
    
    success "Redis配置完成"
}

#-------------------------------------------------------------------------------
# 创建Redis集群 (在任意一个Redis节点执行)
#-------------------------------------------------------------------------------
create_redis_cluster() {
    info "创建Redis集群..."
    
    sleep 5
    
    redis-cli -a "${REDIS_PASSWORD}" --cluster create \
        ${REDIS_01_IP}:6379 \
        ${REDIS_02_IP}:6379 \
        ${REDIS_03_IP}:6379 \
        --cluster-replicas 0 >> "$INSTALL_LOG" 2>&1
    
    success "Redis集群创建完成"
}

#-------------------------------------------------------------------------------
# 部署RabbitMQ集群
#-------------------------------------------------------------------------------
deploy_rabbitmq_cluster() {
    info "部署RabbitMQ集群..."
    
    local rabbitmq_ip=$(hostname -I | awk '{print $1}')
    local is_rabbitmq_node=false
    
    for ip in "$RABBITMQ_01_IP" "$RABBITMQ_02_IP" "$RABBITMQ_03_IP"; do
        if [[ "$ip" == "$rabbitmq_ip" ]]; then
            is_rabbitmq_node=true
            break
        fi
    done
    
    if [[ "$is_rabbitmq_node" == "false" ]]; then
        info "当前服务器不是RabbitMQ节点，跳过..."
        return 0
    fi
    
    # 安装Erlang
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y erlang >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            apt-get install -y erlang >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    # 安装RabbitMQ
    case "$TARGET_OS" in
        centos|rhel)
            rpm --import https://github.com/rabbitmq/signing-keys/releases/download/3.0/rabbitmq-release-signing-key.asc
            yum install -y rabbitmq-server >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            wget -O /tmp/rabbitmq-server.deb https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.12.6/rabbitmq-server_3.12.6-1_all.deb >> "$INSTALL_LOG" 2>&1
            dpkg -i /tmp/rabbitmq-server.deb >> "$INSTALL_LOG" 2>&1
            apt-get install -f -y >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    # 配置集群
    cat > /etc/rabbitmq/rabbitmq.conf << EOF
loopback_users.guest = false
listeners.tcp.default = 5672
management.tcp.port = 15672
cluster_formation.peer_discovery_backend = rabbit_peer_discovery_classic_config
cluster_formation.classic_config.nodes.1 = rabbit@${RABBITMQ_01_IP}
cluster_formation.classic_config.nodes.2 = rabbit@${RABBITMQ_02_IP}
cluster_formation.classic_config.nodes.3 = rabbit@${RABBITMQ_03_IP}
EOF
    
    # 配置Erlang Cookie
    echo "aioa_rabbitmq_cluster_cookie" > /var/lib/rabbitmq/.erlang.cookie
    chown rabbitmq:rabbitmq /var/lib/rabbitmq/.erlang.cookie
    chmod 400 /var/lib/rabbitmq/.erlang.cookie
    
    systemctl enable rabbitmq-server
    systemctl restart rabbitmq-server
    
    success "RabbitMQ配置完成"
}

#-------------------------------------------------------------------------------
# 配置RabbitMQ集群
#-------------------------------------------------------------------------------
setup_rabbitmq_cluster() {
    info "配置RabbitMQ集群..."
    
    # 等待所有节点启动
    sleep 15
    
    # 创建用户
    rabbitmqctl add_user aioa "${RABBITMQ_PASSWORD}"
    rabbitmqctl set_permissions -p / aioa ".*" ".*" ".*"
    rabbitmqctl set_user_tags aioa administrator
    
    # 启用管理插件
    rabbitmq-plugins enable rabbitmq_management >> "$INSTALL_LOG" 2>&1
    
    success "RabbitMQ集群配置完成"
}

#-------------------------------------------------------------------------------
# 部署MinIO分布式
#-------------------------------------------------------------------------------
deploy_minio_distributed() {
    info "部署MinIO分布式..."
    
    local minio_ip=$(hostname -I | awk '{print $1}')
    local is_minio_node=false
    
    for ip in "$MINIO_01_IP" "$MINIO_02_IP" "$MINIO_03_IP" "$MINIO_04_IP"; do
        if [[ "$ip" == "$minio_ip" ]]; then
            is_minio_node=true
            break
        fi
    done
    
    if [[ "$is_minio_node" == "false" ]]; then
        info "当前服务器不是MinIO节点，跳过..."
        return 0
    fi
    
    # 下载并安装MinIO
    wget -c https://dl.min.io/server/minio/release/linux-amd64/minio -O /usr/local/bin/minio >> "$INSTALL_LOG" 2>&1
    chmod +x /usr/local/bin/minio
    
    mkdir -p /data/minio{1..4}
    
    # 配置MinIO
    cat > /etc/default/minio << EOF
MINIO_ROOT_USER=aioaadmin
MINIO_ROOT_PASSWORD=${MINIO_PASSWORD}
MINIO_VOLUMES="http://${MINIO_01_IP}:9000/data/minio{1...4} http://${MINIO_02_IP}:9000/data/minio{1...4} http://${MINIO_03_IP}:9000/data/minio{1...4} http://${MINIO_04_IP}:9000/data/minio{1...4}"
EOF
    
    # 创建服务
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
    
    systemctl enable minio
    systemctl start minio
    
    success "MinIO节点配置完成"
}

#-------------------------------------------------------------------------------
# 初始化MinIO分布式
#-------------------------------------------------------------------------------
init_minio_distributed() {
    info "初始化MinIO分布式集群..."
    
    # 等待所有节点启动
    sleep 10
    
    # 安装mc客户端
    wget -c https://dl.min.io/client/mc/release/linux-amd64/mc -O /usr/local/bin/mc >> "$INSTALL_LOG" 2>&1
    chmod +x /usr/local/bin/mc
    
    # 配置别名
    /usr/local/bin/mc alias set aioa http://${MINIO_01_IP}:9000 aioaadmin "${MINIO_PASSWORD}"
    
    # 创建bucket
    /usr/local/bin/mc mb aioa/aioa-files --ignore-existing
    /usr/local/bin/mc anonymous set download aioa/aioa-files
    
    success "MinIO分布式集群初始化完成"
}

#-------------------------------------------------------------------------------
# 部署Nginx+Keepalived
#-------------------------------------------------------------------------------
deploy_lb() {
    info "部署Nginx+Keepalived负载均衡..."
    
    local lb_ip=$(hostname -I | awk '{print $1}')
    local is_master=false
    
    if [[ "$lb_ip" == "$VIP_IP" ]] || [[ "$lb_ip" == "${VIP_IP%.*}.101" ]]; then
        is_master=true
    fi
    
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y nginx keepalived >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            apt-get install -y nginx keepalived >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    # 配置Keepalived
    cat > /etc/keepalived/keepalived.conf << EOF
! Configuration File for keepalived
global_defs {
    router_id NGINX_${lb_ip//./_}
}

vrrp_script check_nginx {
    script "/etc/keepalived/check_nginx.sh"
    interval 2
    weight -20
}

vrrp_instance VI_1 {
    state ${is_master:+$MASTER:+$BACKUP}
    interface eth0
    virtual_router_id 51
    priority ${is_master:+100:90}
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        ${VIP_IP}/24
    }
    track_script {
        check_nginx
    }
}
EOF
    
    # 创建健康检查脚本
    cat > /etc/keepalived/check_nginx.sh << 'EOF'
#!/bin/bash
if [ `ps -C nginx --no-header | wc -l` -eq 0 ]; then
    systemctl stop keepalived
fi
EOF
    chmod +x /etc/keepalived/check_nginx.sh
    
    # 配置Nginx
    cat > /etc/nginx/conf.d/aioa.conf << EOF
upstream aioa_backend {
    least_conn;
    server ${MASTER_IP}:8080 weight=10;
}

server {
    listen 80;
    server_name _;
    
    client_max_body_size 100M;
    
    location / {
        root /var/www/aioa;
        index index.html;
        try_files \$uri \$uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://aioa_backend/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }
    
    location /ws/ {
        proxy_pass http://aioa_backend/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF
    
    mkdir -p /var/www/aioa
    systemctl enable nginx keepalived
    systemctl start nginx
    systemctl start keepalived
    
    success "Nginx+Keepalived配置完成"
}

#-------------------------------------------------------------------------------
# 部署微服务JAR
#-------------------------------------------------------------------------------
deploy_microservices() {
    info "部署微服务JAR..."
    
    local app_ip=$(hostname -I | awk '{print $1}')
    local is_app_server=false
    
    # 判断是否为应用服务器 (192.168.1.111-116)
    local ip_prefix="${app_ip%.*}"
    if [[ "$ip_prefix" == "192.168.1.11" ]] || [[ "$app_ip" == "$MASTER_IP" ]]; then
        is_app_server=true
    fi
    
    if [[ "$is_app_server" == "false" ]]; then
        info "当前服务器不是应用服务器，跳过..."
        return 0
    fi
    
    # 创建目录
    mkdir -p /opt/aioa/{gateway,user,workflow,report,ai,chat}
    mkdir -p /var/log/aioa
    mkdir -p /var/www/aioa
    
    # 基础配置
    cat > /opt/aioa/base.yml << EOF
spring:
  datasource:
    url: jdbc:mysql://${MYSQL_MASTER_IP}:3306/aioa?useUnicode=true&characterEncoding=utf8
    username: aioa
    password: ${MYSQL_AIOA_PASSWORD}
  redis:
    host: ${REDIS_01_IP}
    port: 6379
    password: ${REDIS_PASSWORD}
  rabbitmq:
    host: ${RABBITMQ_01_IP}
    port: 5672
    username: aioa
    password: ${RABBITMQ_PASSWORD}
minio:
  endpoint: http://${MINIO_01_IP}:9000
  access-key: aioaadmin
  secret-key: ${MINIO_PASSWORD}
EOF
    
    success "微服务基础配置创建完成"
    info "请上传JAR包到对应目录并启动服务"
}

#-------------------------------------------------------------------------------
# 主函数
#-------------------------------------------------------------------------------
microservice_main() {
    info "========== AI-OA 非容器化微服务部署 =========="
    
    config_base_env
    deploy_mysql_cluster
    deploy_redis_cluster
    deploy_rabbitmq_cluster
    deploy_minio_distributed
    deploy_lb
    deploy_microservices
    
    show_microservice_info
}

show_microservice_info() {
    echo ""
    echo "========================================"
    echo "  AI-OA 非容器化微服务部署完成"
    echo "========================================"
    echo ""
    echo "  服务器规划:"
    echo "    VIP:           ${VIP_IP}"
    echo "    MySQL Master:   ${MYSQL_MASTER_IP}"
    echo "    MySQL Slave:    ${MYSQL_SLAVE_IP}"
    echo "    Redis:          ${REDIS_01_IP}, ${REDIS_02_IP}, ${REDIS_03_IP}"
    echo "    RabbitMQ:       ${RABBITMQ_01_IP}, ${RABBITMQ_02_IP}, ${RABBITMQ_03_IP}"
    echo "    MinIO:          ${MINIO_01_IP}-${MINIO_04_IP}"
    echo ""
    echo "  集群创建命令:"
    echo "    Redis: redis-cli -a '${REDIS_PASSWORD}' --cluster create \\"
    echo "      ${REDIS_01_IP}:6379 ${REDIS_02_IP}:6379 ${REDIS_03_IP}:6379"
    echo ""
    echo "  服务启动后需执行:"
    echo "    RabbitMQ: rabbitmqctl join_cluster rabbit@${RABBITMQ_01_IP}"
    echo "    MinIO: mc alias set aioa http://${MINIO_01_IP}:9000 aioaadmin '${MINIO_PASSWORD}'"
    echo ""
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    microservice_main "$@"
fi
