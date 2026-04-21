#!/bin/bash
#===============================================================================
# AI-OA 部署脚本通用函数库
# 
# 提供各部署脚本共用的函数和变量
#===============================================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 日志文件
INSTALL_LOG="${INSTALL_LOG:-/tmp/aioa_install_$(date +%Y%m%d_%H%M%S).log}"

# 全局变量
TARGET_OS="${TARGET_OS:-auto}"
DEPLOY_ENV="${DEPLOY_ENV:-prod}"

#-------------------------------------------------------------------------------
# 日志函数
#-------------------------------------------------------------------------------
log() {
    local level="$1"
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo -e "${timestamp} [${level}] ${message}"
    echo -e "${timestamp} [${level}] ${message}" >> "$INSTALL_LOG" 2>/dev/null || true
}

info() { log "${BLUE}INFO" "$*"; }
success() { log "${GREEN}SUCCESS" "$*"; }
warn() { log "${YELLOW}WARN" "$*"; }
error() { log "${RED}ERROR" "$*"; }

#-------------------------------------------------------------------------------
# 系统检测
#-------------------------------------------------------------------------------
detect_os() {
    if [[ "$TARGET_OS" != "auto" ]] && [[ -n "$TARGET_OS" ]]; then
        return 0
    fi
    
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        case "$ID" in
            centos|rhel|rocky|alma)
                TARGET_OS="centos"
                ;;
            ubuntu)
                TARGET_OS="ubuntu"
                ;;
            debian)
                TARGET_OS="debian"
                ;;
            *)
                error "不支持的Linux发行版: $ID"
                exit 1
                ;;
        esac
    elif [ -f /etc/redhat-release ]; then
        TARGET_OS="centos"
    else
        error "无法检测操作系统类型"
        exit 1
    fi
    
    info "检测到操作系统: $TARGET_OS"
}

#-------------------------------------------------------------------------------
# 命令存在检查
#-------------------------------------------------------------------------------
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

#-------------------------------------------------------------------------------
# 包管理器安装函数
#-------------------------------------------------------------------------------
yum_install() {
    local package="$1"
    if ! command_exists "$package"; then
        yum install -y "$package" >> "$INSTALL_LOG" 2>&1
    fi
}

apt_install() {
    local package="$1"
    export DEBIAN_FRONTEND=noninteractive
    if ! command_exists "$package"; then
        apt-get install -y "$package" >> "$INSTALL_LOG" 2>&1
    fi
}

# 根据操作系统安装包
install_package() {
    local package="$1"
    case "$TARGET_OS" in
        centos|rhel)
            yum_install "$package"
            ;;
        ubuntu|debian)
            apt_install "$package"
            ;;
        macos)
            if command_exists brew; then
                brew install "$package" >> "$INSTALL_LOG" 2>&1 || true
            fi
            ;;
    esac
}

#-------------------------------------------------------------------------------
# 系统配置
#-------------------------------------------------------------------------------
disable_selinux() {
    if [ -f /etc/selinux/config ]; then
        sed -i 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/selinux/config
        setenforce 0 2>/dev/null || true
    fi
}

disable_firewall() {
    case "$TARGET_OS" in
        centos|rhel)
            systemctl stop firewalld 2>/dev/null || true
            systemctl disable firewalld 2>/dev/null || true
            ;;
        ubuntu|debian)
            systemctl stop ufw 2>/dev/null || true
            systemctl disable ufw 2>/dev/null || true
            ;;
    esac
}

#-------------------------------------------------------------------------------
# 内核参数优化
#-------------------------------------------------------------------------------
optimize_kernel() {
    info "优化内核参数..."
    
    cat >> /etc/sysctl.conf << 'EOF'
# AI-OA 内核参数优化
net.core.somaxconn = 65535
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_fin_timeout = 30
net.ipv4.ip_local_port_range = 1024 65535
net.ipv4.tcp_max_syn_backlog = 65535
fs.file-max = 1000000
vm.max_map_count = 262144
vm.swappiness = 10
EOF
    
    sysctl -p >> "$INSTALL_LOG" 2>&1 || true
}

#-------------------------------------------------------------------------------
# 用户限制配置
#-------------------------------------------------------------------------------
configure_limits() {
    info "配置用户限制..."
    
    cat >> /etc/security/limits.conf << 'EOF'
# AI-OA 用户限制
* soft nofile 1000000
* hard nofile 1000000
* soft nproc 65535
* hard nproc 65535
* soft memlock unlimited
* hard memlock unlimited
EOF
}

#-------------------------------------------------------------------------------
# 时区配置
#-------------------------------------------------------------------------------
configure_timezone() {
    info "配置时区..."
    
    case "$TARGET_OS" in
        centos|rhel)
            timedatectl set-timezone Asia/Shanghai >> "$INSTALL_LOG" 2>&1 || true
            ;;
        ubuntu|debian)
            timedatectl set-timezone Asia/Shanghai >> "$INSTALL_LOG" 2>&1 || true
            ;;
    esac
    
    # 同步时间
    chrony_install_and_start
}

chrony_install_and_start() {
    install_package "chrony"
    
    case "$TARGET_OS" in
        centos|rhel)
            systemctl enable chronyd >> "$INSTALL_LOG" 2>&1 || true
            systemctl restart chronyd >> "$INSTALL_LOG" 2>&1 || true
            ;;
        ubuntu|debian)
            systemctl enable chrony >> "$INSTALL_LOG" 2>&1 || true
            systemctl restart chrony >> "$INSTALL_LOG" 2>&1 || true
            ;;
    esac
}

#-------------------------------------------------------------------------------
# 服务管理
#-------------------------------------------------------------------------------
create_service() {
    local service_name="$1"
    local service_file="$2"
    
    cat > "/etc/systemd/system/${service_name}.service" << EOF
$service_file
EOF
    
    systemctl daemon-reload
    systemctl enable "$service_name"
}

#-------------------------------------------------------------------------------
# 备份配置
#-------------------------------------------------------------------------------
setup_backup() {
    info "配置备份任务..."
    
    mkdir -p /backup/aioa
    
    # 数据库备份脚本
    cat > /backup/aioa/backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR=/backup/aioa
DATE=$(date +%Y%m%d_%H%M%S)

# MySQL备份
mysqldump -u root -p'${MYSQL_ROOT_PASSWORD}' aioa | gzip > $BACKUP_DIR/aioa_$DATE.sql.gz

# 保留30天备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

echo "Backup completed: aioa_$DATE.sql.gz"
EOF
    
    chmod +x /backup/aioa/backup.sh
    
    # 添加定时任务
    echo "0 2 * * * /backup/aioa/backup.sh" >> /var/spool/cron/root 2>/dev/null || \
    echo "0 2 * * * /backup/aioa/backup.sh" >> /etc/cron.d/aioa-backup 2>/dev/null || true
}

#-------------------------------------------------------------------------------
# 健康检查
#-------------------------------------------------------------------------------
health_check() {
    local service="$1"
    local port="$2"
    local host="${3:-localhost}"
    
    info "检查 $service 健康状态..."
    
    if nc -z "$host" "$port" 2>/dev/null; then
        success "$service (端口 $port): 正常"
        return 0
    else
        error "$service (端口 $port): 无法连接"
        return 1
    fi
}

#-------------------------------------------------------------------------------
# 显示部署信息
#-------------------------------------------------------------------------------
show_deployment_info() {
    echo ""
    echo "========================================"
    echo "  AI-OA 部署完成"
    echo "========================================"
    echo ""
    echo "  操作系统: $TARGET_OS"
    echo "  部署环境: $DEPLOY_ENV"
    echo "  安装日志: $INSTALL_LOG"
    echo ""
    echo "  服务端口:"
    echo "    - Nginx:      80"
    echo "    - 应用:       8080"
    echo "    - MySQL:      3306"
    echo "    - Redis:      6379"
    echo "    - MinIO:      9000/9001"
    echo "    - n8n:       5678"
    echo ""
    echo "========================================"
    echo ""
}
