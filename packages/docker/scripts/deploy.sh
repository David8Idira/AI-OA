#!/bin/bash
#===============================================================================
# AI-OA Docker Compose 部署脚本
# 支持新旧版Docker (docker-compose 和 docker compose)
# 
# 适用场景: 开发/测试环境 (<200用户)
# 服务器数量: 2-4台
# 
# 功能:
#   - Docker + Docker Compose 安装 (兼容新旧版本)
#   - Kingbase/Redis/RabbitMQ 容器化部署
#   - AI-OA 应用容器化部署
#===============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

#-------------------------------------------------------------------------------
# Docker Compose 兼容函数 (支持新旧版本)
#-------------------------------------------------------------------------------
DOCKER_COMPOSE_CMD=""

check_docker_compose() {
    info "检测Docker Compose环境..."
    
    # 优先使用docker compose (新版, v2+)
    if docker compose version >/dev/null 2>&1; then
        DOCKER_COMPOSE_CMD="docker compose"
        success "检测到 Docker Compose (V2+): $(docker compose version --short)"
        return 0
    fi
    
    # 备选docker-compose (旧版, v1)
    if command -v docker-compose >/dev/null 2>&1; then
        DOCKER_COMPOSE_CMD="docker-compose"
        success "检测到 Docker Compose (V1): $(docker-compose --version)"
        return 0
    fi
    
    # 两者都不存在
    return 1
}

install_docker_compose_plugin() {
    info "安装Docker Compose插件..."
    
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y docker-compose-plugin >> "$INSTALL_LOG" 2>&1 || true
            ;;
        ubuntu|debian)
            apt-get update >> "$INSTALL_LOG" 2>&1
            apt-get install -y docker-compose-plugin >> "$INSTALL_LOG" 2>&1 || true
            ;;
    esac
    
    if docker compose version >/dev/null 2>&1; then
        DOCKER_COMPOSE_CMD="docker compose"
        success "Docker Compose插件安装成功: $(docker compose version --short)"
        return 0
    fi
    
    return 1
}

install_docker_compose_standalone() {
    info "安装独立docker-compose..."
    
    local arch=""
    case "$(uname -m)" in
        x86_64) arch="x86_64" ;;
        aarch64|arm64) arch="aarch64" ;;
        *) arch="x86_64" ;;
    esac
    
    curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-${arch}" -o /usr/local/bin/docker-compose >> "$INSTALL_LOG" 2>&1
    chmod +x /usr/local/bin/docker-compose >> "$INSTALL_LOG" 2>&1
    
    if command -v docker-compose >/dev/null 2>&1; then
        DOCKER_COMPOSE_CMD="docker-compose"
        success "docker-compose安装成功: $(docker-compose --version)"
        return 0
    fi
    
    return 1
}

ensure_docker_compose() {
    if ! check_docker_compose; then
        info "Docker Compose未检测到，开始安装..."
        
        # 方案1: 安装docker-compose-plugin (推荐)
        if install_docker_compose_plugin; then
            return 0
        fi
        
        # 方案2: 安装独立docker-compose
        if install_docker_compose_standalone; then
            return 0
        fi
        
        error "Docker Compose安装失败"
        exit 1
    fi
}

# Docker Compose 封装命令 (自动适配新旧版本)
dc_pull() {
    ensure_docker_compose
    $DOCKER_COMPOSE_CMD pull
}

dc_up() {
    ensure_docker_compose
    $DOCKER_COMPOSE_CMD up -d
}

dc_ps() {
    ensure_docker_compose
    $DOCKER_COMPOSE_CMD ps
}

dc_down() {
    ensure_docker_compose
    $DOCKER_COMPOSE_CMD down
}

dc_logs() {
    ensure_docker_compose
    $DOCKER_COMPOSE_CMD logs -f
}

#-------------------------------------------------------------------------------
# 安装Docker和Docker Compose
#-------------------------------------------------------------------------------
install_docker() {
    info "安装Docker和Docker Compose..."
    
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y yum-utils >> "$INSTALL_LOG" 2>&1
            yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo >> "$INSTALL_LOG" 2>&1
            yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            apt-get update >> "$INSTALL_LOG" 2>&1
            apt-get install -y ca-certificates curl gnupg lsb-release >> "$INSTALL_LOG" 2>&1
            mkdir -p /etc/apt/keyrings
            curl -fsSL https://download.docker.com/linux/${TARGET_OS}/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg >> "$INSTALL_LOG" 2>&1
            echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/${TARGET_OS} $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
            apt-get update >> "$INSTALL_LOG" 2>&1
            apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    systemctl enable docker
    systemctl start docker
    
    # 验证Docker Compose并确保可用
    ensure_docker_compose
    
    # 添加当前用户到docker组
    if [ -n "$SUDO_USER" ]; then
        usermod -aG docker "$SUDO_USER"
    fi
    
    success "Docker安装完成: $(docker --version), $(docker compose version --short 2>/dev/null || docker-compose --version 2>/dev/null)"
}

#-------------------------------------------------------------------------------
# 创建Docker Compose配置
#-------------------------------------------------------------------------------
create_docker_compose() {
    info "创建Docker Compose配置..."
    
    mkdir -p /opt/aioa/docker
    cd /opt/aioa/docker
    
    cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: aioa-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-ChangeMe123!}
      MYSQL_DATABASE: aioa
      MYSQL_USER: aioa
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-AioaPassword123!}
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"
    networks:
      - aioa-network
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  # Redis缓存
  redis:
    image: redis:7-alpine
    container_name: aioa-redis
    restart: unless-stopped
    command: redis-server --requirepass ${REDIS_PASSWORD:-RedisPassword123}
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    networks:
      - aioa-network

  # MinIO对象存储
  minio:
    image: minio/minio:latest
    container_name: aioa-minio
    restart: unless-stopped
    environment:
      MINIO_ROOT_USER: ${MINIO_USER:-aioaadmin}
      MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD:-MinioPassword123!}
    volumes:
      - minio_data:/data
    ports:
      - "9000:9000"
      - "9001:9001"
    networks:
      - aioa-network
    command: server /data --console-address ":9001"

  # RabbitMQ消息队列
  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    container_name: aioa-rabbitmq
    restart: unless-stopped
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USER:-aioa}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD:-AioaPassword123}
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    networks:
      - aioa-network

  # n8n工作流引擎
  n8n:
    image: n8nio/n8n:latest
    container_name: aioa-n8n
    restart: unless-stopped
    environment:
      - N8N_BASIC_AUTH_ACTIVE=true
      - N8N_BASIC_AUTH_USER=${N8N_USER:-admin}
      - N8N_BASIC_AUTH_PASSWORD=${N8N_PASSWORD:-N8nPassword123}
      - N8N_HOST=n8n.local
      - N8N_PROTOCOL=http
      - WEBHOOK_URL=http://n8n.local:5672
      - DB_TYPE=sqlite
      - EXECUTIONS_DATA_PRUNE=true
    volumes:
      - n8n_data:/home/node/.n8n
    ports:
      - "5678:5678"
    networks:
      - aioa-network

  # AI-OA应用
  aioa:
    image: aioa/app:latest
    container_name: aioa-app
    restart: unless-stopped
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/aioa?useUnicode=true&characterEncoding=utf8
      SPRING_DATASOURCE_USERNAME: aioa
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD:-AioaPassword123!}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      SPRING_REDIS_PASSWORD: ${REDIS_PASSWORD:-RedisPassword123}
      MINIO_ENDPOINT: http://minio:9000
      MINIO_ACCESS_KEY: ${MINIO_USER:-aioaadmin}
      MINIO_SECRET_KEY: ${MINIO_PASSWORD:-MinioPassword123!}
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: ${RABBITMQ_USER:-aioa}
      RABBITMQ_PASSWORD: ${RABBITMQ_PASSWORD:-AioaPassword123}
      N8N_WEBHOOK_URL: http://n8n:5672
    volumes:
      - app_logs:/var/log/aioa
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
      - minio
      - rabbitmq
    networks:
      - aioa-network

  # Nginx反向代理
  nginx:
    image: nginx:alpine
    container_name: aioa-nginx
    restart: unless-stopped
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./html:/usr/share/nginx/html:ro
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - aioa
      - n8n
    networks:
      - aioa-network

volumes:
  mysql_data:
  redis_data:
  minio_data:
  rabbitmq_data:
  n8n_data:
  app_logs:

networks:
  aioa-network:
    driver: bridge
EOF
    
    # 创建Nginx配置
    mkdir -p ./nginx ./html
    cat > nginx/nginx.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    upstream aioa_backend {
        server aioa:8080;
    }
    
    server {
        listen 80;
        server_name _;
        
        client_max_body_size 100M;
        
        location / {
            root /usr/share/nginx/html;
            index index.html;
            try_files $uri $uri/ /index.html;
        }
        
        location /api/ {
            proxy_pass http://aioa_backend/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }
        
        location /ws/ {
            proxy_pass http://aioa_backend/ws/;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
        }
        
        location /n8n/ {
            proxy_pass http://aioa:5678/;
            proxy_set_header Host $host;
        }
    }
}
EOF
    
    # 创建前端静态文件
    cat > html/index.html << 'EOF'
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI-OA 智能化OA系统</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .container { text-align: center; }
        h1 { color: #667eea; font-size: 48px; margin-bottom: 20px; }
        p { color: #666; font-size: 18px; }
        .status { margin-top: 30px; padding: 20px; background: white; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .status-item { margin: 10px 0; color: #52c41a; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🤖 AI-OA</h1>
        <p>智能化OA系统 - Docker Compose部署</p>
        <div class="status">
            <div class="status-item">✅ 系统运行正常</div>
            <div>版本: 1.0.0</div>
        </div>
    </div>
</body>
</html>
EOF
    
    success "Docker Compose配置创建完成"
}

#-------------------------------------------------------------------------------
# 环境变量配置
#-------------------------------------------------------------------------------
create_env_file() {
    info "创建环境变量文件..."
    
    cat > .env << 'EOF'
# AI-OA 环境变量配置

# MySQL配置
MYSQL_ROOT_PASSWORD=ChangeMe123!
MYSQL_PASSWORD=AioaPassword123!

# Redis配置
REDIS_PASSWORD=RedisPassword123

# MinIO配置
MINIO_USER=aioaadmin
MINIO_PASSWORD=MinioPassword123!

# RabbitMQ配置
RABBITMQ_USER=aioa
RABBITMQ_PASSWORD=AioaPassword123

# n8n配置
N8N_USER=admin
N8N_PASSWORD=N8nPassword123

# AI API配置（可选）
# OPENAI_API_KEY=your-api-key
EOF
    
    success "环境变量文件创建完成"
}

#-------------------------------------------------------------------------------
# 启动服务
#-------------------------------------------------------------------------------
start_services() {
    info "启动Docker服务..."
    
    # 创建网络
    docker network create aioa-network 2>/dev/null || true
    
    # 拉取镜像
    info "拉取基础镜像..."
    dc_pull
    
    # 启动服务
    dc_up
    
    # 等待服务启动
    info "等待服务启动..."
    sleep 15
    
    # 显示状态
    info "服务状态:"
    dc_ps
    
    success "Docker服务启动完成"
}

#-------------------------------------------------------------------------------
# 验证部署
#-------------------------------------------------------------------------------
verify_deployment() {
    info "验证Docker部署..."
    
    # 检查容器状态
    local running=$(dc_ps 2>/dev/null | grep -c "Up" || echo "0")
    local total=$(dc_ps 2>/dev/null | grep -c "aioa-" || echo "0")
    
    if [ "$running" -ge "$total" ] && [ "$total" -gt 0 ]; then
        success "所有容器运行正常 ($running/$total)"
    else
        warn "部分容器未正常运行"
    fi
    
    # 健康检查
    health_check "Nginx" 80
    health_check "MySQL" 3306
    health_check "Redis" 6379
    health_check "MinIO" 9000
    health_check "RabbitMQ" 5672
    health_check "n8n" 5678
    health_check "AI-OA" 8080
}

#-------------------------------------------------------------------------------
# 停止服务
#-------------------------------------------------------------------------------
stop_services() {
    info "停止Docker服务..."
    cd /opt/aioa/docker
    dc_down
    success "服务已停止"
}

#-------------------------------------------------------------------------------
# 卸载
#-------------------------------------------------------------------------------
uninstall() {
    warn "卸载Docker部署..."
    cd /opt/aioa/docker
    dc_down
    rm -rf /opt/aioa/docker
    success "卸载完成"
}

#-------------------------------------------------------------------------------
# 主函数
#-------------------------------------------------------------------------------
docker_main() {
    info "========== AI-OA Docker Compose 部署 =========="
    
    case "$1" in
        stop)
            stop_services
            ;;
        uninstall)
            uninstall
            ;;
        *)
            install_docker
            create_env_file
            create_docker_compose
            start_services
            verify_deployment
            show_docker_info
            ;;
    esac
}

show_docker_info() {
    echo ""
    echo "========================================"
    echo "  AI-OA Docker Compose 部署完成"
    echo "========================================"
    echo ""
    echo "  管理命令:"
    echo "    cd /opt/aioa/docker"
    echo "    dc_ps        # 查看状态 (兼容新旧版本)"
    echo "    dc_logs      # 查看日志"
    echo "    dc_down      # 停止服务"
    echo "    dc_up        # 启动服务"
    echo ""
    echo "  访问地址:"
    echo "    前端:     http://<服务器IP>"
    echo "    API:     http://<服务器IP>:8080"
    echo "    MinIO:   http://<服务器IP>:9001"
    echo "    RabbitMQ: http://<服务器IP>:15672"
    echo "    n8n:     http://<服务器IP>:5678"
    echo ""
    echo "  默认账号:"
    echo "    MinIO:   aioaadmin / MinioPassword123!"
    echo "    RabbitMQ: aioa / AioaPassword123"
    echo "    n8n:     admin / N8nPassword123"
    echo ""
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    docker_main "$@"
fi
