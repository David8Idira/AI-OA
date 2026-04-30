#!/bin/bash

# AI-OA HarmonyOS 构建脚本
# 用法: ./build.sh [dev|test|prod]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 默认环境
BUILD_ENV=${1:-dev}

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   AI-OA HarmonyOS 构建脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "构建环境: ${YELLOW}$BUILD_ENV${NC}"
echo -e "项目路径: ${YELLOW}$PROJECT_ROOT${NC}"
echo ""

# 检查环境
check_environment() {
    echo -e "${GREEN}[1/5] 检查构建环境...${NC}"
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        echo -e "${RED}错误: Node.js 未安装${NC}"
        exit 1
    fi
    echo -e "  Node.js: $(node -v)"
    
    # 检查npm
    if ! command -v npm &> /dev/null; then
        echo -e "${RED}错误: npm 未安装${NC}"
        exit 1
    fi
    echo -e "  npm: $(npm -v)"
    
    # 检查HarmonyOS SDK (可选)
    if [ -d "$HOME/Huawei/Sdk" ]; then
        echo -e "  HarmonyOS SDK: 已配置"
    else
        echo -e "${YELLOW}  HarmonyOS SDK: 未配置(可选)${NC}"
    fi
    
    echo -e "${GREEN}环境检查完成${NC}"
    echo ""
}

# 安装依赖
install_dependencies() {
    echo -e "${GREEN}[2/5] 安装依赖...${NC}"
    
    # 清理旧依赖
    if [ -d "node_modules" ]; then
        echo -e "  清理旧依赖..."
        rm -rf node_modules
    fi
    
    # 安装依赖
    npm install
    
    echo -e "${GREEN}依赖安装完成${NC}"
    echo ""
}

# 代码检查
lint_code() {
    echo -e "${GREEN}[3/5] 代码检查...${NC}"
    
    if [ -f "eslint.config.js" ] || [ -f ".eslintrc.js" ]; then
        echo -e "  运行 ESLint..."
        npm run lint || {
            echo -e "${YELLOW}警告: ESLint 检查发现问题${NC}"
        }
    else
        echo -e "  ESLint 配置不存在，跳过"
    fi
    
    echo -e "${GREEN}代码检查完成${NC}"
    echo ""
}

# 编译项目
build_project() {
    echo -e "${GREEN}[4/5] 编译项目 (环境: $BUILD_ENV)...${NC}"
    
    # 设置环境变量
    export BUILD_ENV=$BUILD_ENV
    
    # 执行编译
    case $BUILD_ENV in
        dev)
            echo -e "  构建开发环境..."
            npm run build:dev
            ;;
        test)
            echo -e "  构建测试环境..."
            npm run build:test
            ;;
        prod)
            echo -e "  构建生产环境..."
            npm run build:prod
            ;;
        *)
            echo -e "${RED}错误: 不支持的环境: $BUILD_ENV${NC}"
            echo "支持的环境: dev, test, prod"
            exit 1
            ;;
    esac
    
    echo -e "${GREEN}编译完成${NC}"
    echo ""
}

# 生成产物
generate_output() {
    echo -e "${GREEN}[5/5] 生成构建产物...${NC}"
    
    OUTPUT_DIR="$PROJECT_ROOT/dist/$BUILD_ENV"
    
    if [ -d "$OUTPUT_DIR" ]; then
        echo -e "  产物目录: $OUTPUT_DIR"
        
        # 列出产物
        echo -e ""
        echo -e "  构建产物:"
        find "$OUTPUT_DIR" -type f -name "*.hap" 2>/dev/null | while read f; do
            size=$(du -h "$f" | cut -f1)
            echo -e "    - $(basename $f) ($size)"
        done
        
        if [ ! -f "$OUTPUT_DIR"/*.hap 2>/dev/null ]; then
            echo -e "    (HAP文件将在后续步骤生成)"
        fi
    else
        echo -e "${YELLOW}警告: 产物目录不存在${NC}"
    fi
    
    echo -e "${GREEN}构建产物生成完成${NC}"
    echo ""
}

# 显示完成信息
show_summary() {
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}   构建完成!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "构建环境: ${YELLOW}$BUILD_ENV${NC}"
    echo -e "产物目录: ${YELLOW}$PROJECT_ROOT/dist/$BUILD_ENV${NC}"
    echo ""
    echo -e "下一步:"
    echo -e "  1. 使用 DevEco Studio 打开项目"
    echo -e "  2. 连接设备或启动模拟器"
    echo -e "  3. 运行或调试应用"
    echo ""
}

# 主流程
main() {
    check_environment
    install_dependencies
    lint_code
    build_project
    generate_output
    show_summary
}

# 捕获错误
trap 'echo -e "${RED}构建失败!${NC}" >&2; exit 1' ERR

# 执行主流程
main
