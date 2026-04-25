#!/bin/bash

# Docker镜像加速器设置脚本
# 适用于国内环境，加速Docker镜像拉取

set -e

echo "========== Docker镜像加速配置脚本 =========="
echo "适用于中国大陆地区，解决Docker镜像拉取慢的问题"
echo ""

# 备份原有配置
DOCKER_DAEMON_JSON="/etc/docker/daemon.json"
if [ -f "$DOCKER_DAEMON_JSON" ]; then
    echo "备份原有配置: $DOCKER_DAEMON_JSON -> ${DOCKER_DAEMON_JSON}.bak"
    cp "$DOCKER_DAEMON_JSON" "${DOCKER_DAEMON_JSON}.bak"
fi

# 检测操作系统
OS_TYPE=""
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS_TYPE=$ID
fi

echo "检测到操作系统: $OS_TYPE"
echo ""

# 选择镜像源
echo "请选择Docker镜像加速源:"
echo "1. 阿里云镜像加速器（推荐）"
echo "2. 腾讯云镜像加速器"
echo "3. 网易云镜像加速器"
echo "4. DaoCloud镜像加速器"
echo "5. 华为云镜像加速器"
echo "6. 自定义镜像源"
read -p "请输入选项 (1-6): " MIRROR_CHOICE

case $MIRROR_CHOICE in
    1)
        MIRRORS='"https://registry.cn-hangzhou.aliyuncs.com"'
        echo "已选择阿里云镜像加速器"
        ;;
    2)
        MIRRORS='"https://mirror.ccs.tencentyun.com"'
        echo "已选择腾讯云镜像加速器"
        ;;
    3)
        MIRRORS='"https://hub-mirror.c.163.com"'
        echo "已选择网易云镜像加速器"
        ;;
    4)
        MIRRORS='"https://docker.m.daocloud.io"'
        echo "已选择DaoCloud镜像加速器"
        ;;
    5)
        MIRRORS='"https://05f073ad3c0010ea0f4bc00b7105ec20.mirror.swr.myhuaweicloud.com"'
        echo "已选择华为云镜像加速器"
        ;;
    6)
        read -p "请输入自定义镜像源URL: " CUSTOM_MIRROR
        MIRRORS="\"$CUSTOM_MIRROR\""
        echo "已设置自定义镜像源"
        ;;
    *)
        echo "无效选项，使用默认阿里云镜像加速器"
        MIRRORS='"https://registry.cn-hangzhou.aliyuncs.com"'
        ;;
esac

echo ""

# 创建新的daemon.json配置
echo "创建Docker daemon.json配置..."
cat > /tmp/daemon.json << EOF
{
  "registry-mirrors": [
    $MIRRORS
  ],
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m"
  },
  "storage-driver": "overlay2"
}
EOF

# 移动配置文件到正确位置
sudo mv /tmp/daemon.json "$DOCKER_DAEMON_JSON"
echo "配置文件已生成: $DOCKER_DAEMON_JSON"

# 显示配置内容
echo ""
echo "配置内容:"
cat "$DOCKER_DAEMON_JSON"
echo ""

# 重启Docker服务
echo "重启Docker服务..."
if [ "$OS_TYPE" = "ubuntu" ] || [ "$OS_TYPE" = "debian" ]; then
    sudo systemctl restart docker
elif [ "$OS_TYPE" = "centos" ] || [ "$OS_TYPE" = "rhel" ] || [ "$OS_TYPE" = "fedora" ]; then
    sudo systemctl restart docker
elif [ "$OS_TYPE" = "alpine" ]; then
    sudo rc-service docker restart
else
    echo "未知操作系统，请手动重启Docker服务"
    exit 1
fi

# 等待Docker服务启动
sleep 3

# 验证配置
echo "验证配置..."
if sudo docker info | grep -q "Registry Mirrors"; then
    echo "✅ Docker镜像加速配置成功！"
    sudo docker info | grep -A 2 "Registry Mirrors"
else
    echo "❌ 配置可能未生效，请检查Docker服务状态"
    exit 1
fi

echo ""
echo "========== 常用镜像加速测试 =========="

# 测试镜像拉取速度
TEST_IMAGES=("nginx:alpine" "redis:alpine" "mysql:8.0")

for IMAGE in "${TEST_IMAGES[@]}"; do
    echo ""
    echo "测试拉取镜像: $IMAGE"
    
    # 删除本地已有的镜像
    sudo docker rmi -f $IMAGE 2>/dev/null || true
    
    # 计时拉取镜像
    START_TIME=$(date +%s)
    sudo docker pull $IMAGE 2>&1 | tail -5
    END_TIME=$(date +%s)
    
    DURATION=$((END_TIME - START_TIME))
    echo "拉取耗时: ${DURATION}秒"
done

echo ""
echo "========== 其他有用的配置 =========="

# 创建Docker镜像仓库配置文件
if [ ! -f ~/.docker/config.json ]; then
    cat > ~/.docker/config.json << EOF
{
  "auths": {},
  "HttpHeaders": {
    "User-Agent": "Docker-Client/19.03.9 (linux)"
  },
  "credSstore": "desktop"
}
EOF
    echo "✅ 已创建Docker客户端配置文件"
fi

# 创建Maven镜像加速配置
if [ ! -f ~/.m2/settings.xml ]; then
    mkdir -p ~/.m2
    cat > ~/.m2/settings.xml << EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF
    echo "✅ 已创建Maven镜像加速配置"
fi

echo ""
echo "========== 使用说明 =========="
echo "1. 使用国内版docker-compose文件:"
echo "   docker-compose -f docker-compose-cn.yml up -d"
echo ""
echo "2. 手动拉取镜像时使用国内源:"
echo "   docker pull registry.cn-hangzhou.aliyuncs.com/library/nginx:alpine"
echo ""
echo "3. 查看当前镜像源配置:"
echo "   docker info | grep -i 'registry'"
echo ""
echo "4. 测试镜像拉取速度:"
echo "   time docker pull nginx:alpine"
echo ""
echo "5. 如果仍有问题，可以尝试:"
echo "   a. 更换其他镜像源"
echo "   b. 使用代理服务器"
echo "   c. 手动下载镜像后导入"
echo ""
echo "========== 配置完成！ =========="
echo "现在Docker镜像拉取速度应该已经大大提升了。"

# 创建快捷命令别名
echo ""
echo "建议将以下别名添加到 ~/.bashrc 或 ~/.zshrc:"
echo ""
echo "alias docker-cn='docker-compose -f docker-compose-cn.yml'"
echo "alias docker-pull-cn='function _docker_pull_cn() { docker pull registry.cn-hangzhou.aliyuncs.com/library/\$1; };_docker_pull_cn'"
echo "alias docker-images-cn='docker images | grep -i registry.cn-hangzhou'"
echo "alias docker-speed-test='time docker pull nginx:alpine'"

# 创建示例命令文件
cat > /tmp/docker-cn-commands.sh << 'EOF'
#!/bin/bash
# Docker国内源快捷命令

# 使用国内版docker-compose
alias docker-cn='docker-compose -f docker-compose-cn.yml'

# 拉取国内源镜像
docker-pull-cn() {
    if [ -z "$1" ]; then
        echo "用法: docker-pull-cn <镜像名:标签>"
        echo "示例: docker-pull-cn nginx:alpine"
        return 1
    fi
    docker pull "registry.cn-hangzhou.aliyuncs.com/library/$1"
}

# 查看国内源镜像
docker-images-cn() {
    docker images | grep -i 'registry.cn-hangzhou'
}

# 测试镜像拉取速度
docker-speed-test() {
    echo "测试镜像拉取速度..."
    time docker pull nginx:alpine
}

# 清理未使用的镜像
docker-cleanup() {
    echo "清理未使用的Docker资源..."
    docker system prune -f
}

# 查看镜像源状态
docker-mirror-status() {
    echo "当前Docker镜像源配置:"
    docker info | grep -A 2 "Registry Mirrors"
}

EOF

echo ""
echo "快捷命令文件已保存到: /tmp/docker-cn-commands.sh"
echo "可以将其添加到您的shell配置中"

# 记录操作日志
LOG_FILE="/tmp/docker-mirror-setup-$(date +%Y%m%d-%H%M%S).log"
echo "操作日志已保存到: $LOG_FILE"
{
    echo "Docker镜像加速配置脚本执行记录"
    echo "执行时间: $(date)"
    echo "操作系统: $OS_TYPE"
    echo "选择的镜像源: $MIRRORS"
    echo "配置文件: $DOCKER_DAEMON_JSON"
    echo ""
    echo "测试镜像拉取结果:"
    for IMAGE in "${TEST_IMAGES[@]}"; do
        echo "- $IMAGE"
    done
} > "$LOG_FILE"

echo ""
echo "✅ 所有配置已完成！"
exit 0