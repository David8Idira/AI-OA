#!/bin/bash
#===============================================================================
# AI-OA 自动化部署脚本
# 
# 支持平台: CentOS/RHEL, Ubuntu/Debian, macOS
# 支持部署: standalone, microservice, docker, k8s
# 
# 用法:
#   ./deploy.sh [选项]
#   
#   选项:
#     --plan <方案>     部署方案: standalone, microservice, docker, k8s
#     --os <系统>        操作系统: auto, centos, ubuntu, debian, macos
#     --env <环境>       环境: dev, test, prod
#     --help            显示帮助
#
# 示例:
#   ./deploy.sh --plan standalone --os auto --env prod
#   ./deploy.sh --plan microservice --os ubuntu
#   ./deploy.sh --plan docker --env dev
#
#===============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 全局变量
DEPLOY_PLAN=""
TARGET_OS=""
DEPLOY_ENV=""
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
LOG_DIR="$SCRIPT_DIR/logs"
INSTALL_LOG="$LOG_DIR/install_$(date +%Y%m%d_%H%M%S).log"

#-------------------------------------------------------------------------------
# 通用函数库
#-------------------------------------------------------------------------------

log() {
    local level="$1"
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo -e "${timestamp} [${level}] ${message}" | tee -a "$INSTALL_LOG"
}

info() { log "${BLUE}INFO" "$*"; }
success() { log "${GREEN}SUCCESS" "$*"; }
warn() { log "${YELLOW}WARN" "$*"; }
error() { log "${RED}ERROR" "$*"; }

# 检测操作系统
detect_os() {
    if [[ "$TARGET_OS" != "auto" ]] && [[ -n "$TARGET_OS" ]]; then
        return 0
    fi
    
    case "$(uname -s)" in
        Linux*)
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
            ;;
        Darwin*)
            TARGET_OS="macos"
            ;;
        *)
            error "不支持的操作系统: $(uname -s)"
            exit 1
            ;;
    esac
    
    info "检测到操作系统: $TARGET_OS"
}

# 检查是否为root用户
check_root() {
    if [ "$EUID" -ne 0 ] && [ "$TARGET_OS" != "macos" ]; then
        error "请使用root用户运行此脚本"
        exit 1
    fi
}

# 创建日志目录
init_log_dir() {
    mkdir -p "$LOG_DIR"
    info "日志目录: $LOG_DIR"
}

# 检查命令是否存在
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 安装基础软件包
install_base_packages() {
    info "安装基础软件包..."
    
    case "$TARGET_OS" in
        centos|rhel)
            yum update -y >> "$INSTALL_LOG" 2>&1
            yum install -y curl wget vim git unzip tar gzip jq \
                chrony net-tools telnet nc iftop iotop \
                sysstat htop ncdu rsync 2>> "$INSTALL_LOG"
            ;;
        ubuntu|debian)
            export DEBIAN_FRONTEND=noninteractive
            apt-get update -y >> "$INSTALL_LOG" 2>&1
            apt-get install -y curl wget vim git unzip tar gzip jq \
                chrony net-tools telnet netcat-openbsd iftop iotop \
                sysstat htop rsync 2>> "$INSTALL_LOG"
            ;;
        macos)
            if command_exists brew; then
                brew install coreutils wget vim git jq
            else
                warn "请先安装Homebrew: https://brew.sh"
            fi
            ;;
    esac
    
    success "基础软件包安装完成"
}

# 配置防火墙
configure_firewall() {
    info "配置防火墙..."
    
    case "$TARGET_OS" in
        centos|rhel)
            if command_exists firewalld; then
                systemctl enable firewalld
                systemctl start firewalld
                # 开放端口
                for port in 80 443 3306 5672 6379 8080 8081 8082 8083 8084 8085 9000 15672; do
                    firewall-cmd --permanent --add-port=$port/tcp 2>/dev/null || true
                done
                firewall-cmd --reload
            fi
            ;;
        ubuntu|debian)
            if command_exists ufw; then
                ufw allow 22/tcp
                ufw allow 80/tcp
                ufw allow 443/tcp
                ufw allow 8080:8085/tcp
                ufw allow 3306/tcp
                ufw allow 5672/tcp
                ufw allow 6379/tcp
                ufw allow 9000/tcp
                ufw allow 15672/tcp
                ufw --force enable
            fi
            ;;
    esac
    
    success "防火墙配置完成"
}

# 安装JDK
install_jdk() {
    info "安装JDK 17..."
    
    local java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1 || echo "0")
    
    if [ "$java_version" = "17" ]; then
        info "JDK 17已安装"
        return 0
    fi
    
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y java-17-openjdk java-17-openjdk-devel >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            apt-get install -y openjdk-17-jdk openjdk-17-jre >> "$INSTALL_LOG" 2>&1
            ;;
        macos)
            brew install openjdk@17 >> "$INSTALL_LOG" 2>&1 || true
            ;;
    esac
    
    success "JDK安装完成"
    java -version 2>&1 | head -3
}

# 安装Docker
install_docker() {
    if command_exists docker; then
        info "Docker已安装: $(docker --version)"
        return 0
    fi
    
    info "安装Docker..."
    
    case "$TARGET_OS" in
        centos|rhel)
            yum install -y yum-utils >> "$INSTALL_LOG" 2>&1
            yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo >> "$INSTALL_LOG" 2>&1
            yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin >> "$INSTALL_LOG" 2>&1
            ;;
        ubuntu|debian)
            apt-get install -y ca-certificates curl gnupg lsb-release >> "$INSTALL_LOG" 2>&1
            mkdir -p /etc/apt/keyrings
            curl -fsSL https://download.docker.com/linux/${TARGET_OS}/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg >> "$INSTALL_LOG" 2>&1
            echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/${TARGET_OS} $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
            apt-get update -y >> "$INSTALL_LOG" 2>&1
            apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin >> "$INSTALL_LOG" 2>&1
            ;;
    esac
    
    systemctl enable docker >> "$INSTALL_LOG" 2>&1 || true
    systemctl start docker >> "$INSTALL_LOG" 2>&1 || true
    success "Docker安装完成: $(docker --version)"
}

# 安装kubectl
install_kubectl() {
    if command_exists kubectl; then
        info "kubectl已安装: $(kubectl version --client --short 2>/dev/null || echo 'installed')"
        return 0
    fi
    
    info "安装kubectl..."
    
    case "$TARGET_OS" in
        centos|rhel|ubuntu|debian)
            curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" >> "$INSTALL_LOG" 2>&1
            chmod +x kubectl
            mv kubectl /usr/local/bin/
            ;;
        macos)
            brew install kubectl >> "$INSTALL_LOG" 2>&1 || true
            ;;
    esac
    
    success "kubectl安装完成"
}

# 安装Helm
install_helm() {
    if command_exists helm; then
        info "Helm已安装: $(helm version --short 2>/dev/null || echo 'installed')"
        return 0
    fi
    
    info "安装Helm..."
    
    curl -fsSL https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash >> "$INSTALL_LOG" 2>&1
    
    success "Helm安装完成"
}

# 验证环境
verify_environment() {
    info "验证部署环境..."
    
    local errors=0
    
    # 检查内存
    local total_mem=$(free -m | awk '/^Mem:/{print $2}')
    info "总内存: ${total_mem}MB"
    
    # 检查磁盘空间
    local available_disk=$(df -m / | awk 'NR==2 {print $4}')
    info "可用磁盘: ${available_disk}MB"
    
    if [ "$total_mem" -lt 2048 ]; then
        warn "内存小于2GB，可能影响性能"
    fi
    
    if [ "$available_disk" -lt 10240 ]; then
        warn "可用磁盘小于10GB，可能空间不足"
    fi
    
    success "环境验证完成"
}

# 显示帮助
show_help() {
    cat << EOF
AI-OA 自动化部署脚本

用法:
    $0 [选项]

选项:
    --plan <方案>     部署方案: standalone, microservice, docker, k8s
                       - standalone: 单体部署 (1-2台服务器)
                       - microservice: 非容器化微服务 (8-12台服务器)
                       - docker: Docker Compose部署 (2-4台服务器)
                       - k8s: Kubernetes部署 (15-20+台服务器)
    --os <系统>        操作系统: auto, centos, ubuntu, debian, macos
                       (默认: auto 自动检测)
    --env <环境>       部署环境: dev, test, prod
                       (默认: prod)
    --help            显示此帮助信息

示例:
    $0 --plan standalone --os auto --env prod
    $0 --plan microservice --os ubuntu
    $0 --plan docker --env dev
    $0 --plan k8s --env prod

EOF
}

#-------------------------------------------------------------------------------
# 部署方案
#-------------------------------------------------------------------------------

# 单体部署
deploy_standalone() {
    info "开始单体部署..."
    
    source "$SCRIPT_DIR/deploy-standalone.sh"
    
    standalone_main
    success "单体部署完成!"
}

# 非容器化微服务部署
deploy_microservice() {
    info "开始非容器化微服务部署..."
    
    source "$SCRIPT_DIR/deploy-microservice.sh"
    
    microservice_main
    success "非容器化微服务部署完成!"
}

# Docker Compose部署
deploy_docker() {
    info "开始Docker Compose部署..."
    
    install_docker
    
    source "$SCRIPT_DIR/deploy-docker.sh"
    
    docker_main
    success "Docker Compose部署完成!"
}

# Kubernetes部署
deploy_k8s() {
    info "开始Kubernetes部署..."
    
    install_docker
    install_kubectl
    install_helm
    
    source "$SCRIPT_DIR/deploy-k8s.sh"
    
    k8s_main
    success "Kubernetes部署完成!"
}

#-------------------------------------------------------------------------------
# 主函数
#-------------------------------------------------------------------------------

main() {
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            --plan)
                DEPLOY_PLAN="$2"
                shift 2
                ;;
            --os)
                TARGET_OS="$2"
                shift 2
                ;;
            --env)
                DEPLOY_ENV="$2"
                shift 2
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 默认值
    TARGET_OS="${TARGET_OS:-auto}"
    DEPLOY_ENV="${DEPLOY_ENV:-prod}"
    
    # 显示欢迎信息
    echo ""
    echo "========================================"
    echo "  AI-OA 自动化部署脚本"
    echo "========================================"
    echo ""
    echo "  部署方案: ${DEPLOY_PLAN:-未指定}"
    echo "  操作系统: ${TARGET_OS}"
    echo "  部署环境: ${DEPLOY_ENV}"
    echo "========================================"
    echo ""
    
    # 检查部署方案
    if [ -z "$DEPLOY_PLAN" ]; then
        error "请指定部署方案 --plan"
        show_help
        exit 1
    fi
    
    # 初始化
    init_log_dir
    detect_os
    check_root
    
    # 执行预检查
    info "执行环境预检查..."
    install_base_packages
    verify_environment
    
    # 根据部署方案执行
    case "$DEPLOY_PLAN" in
        standalone)
            deploy_standalone
            ;;
        microservice)
            deploy_microservice
            ;;
        docker)
            deploy_docker
            ;;
        k8s)
            deploy_k8s
            ;;
        *)
            error "未知部署方案: $DEPLOY_PLAN"
            echo "支持的方案: standalone, microservice, docker, k8s"
            exit 1
            ;;
    esac
    
    echo ""
    echo "========================================"
    echo "  部署完成!"
    echo "  日志文件: $INSTALL_LOG"
    echo "========================================"
    echo ""
}

# 运行主函数
main "$@"
