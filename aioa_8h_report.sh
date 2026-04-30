#!/bin/bash
# AI-OA 8小时进度汇报脚本
# 执行时间: 12:30, 20:30, 04:30 (每8小时)

echo "=== AI-OA项目进度汇报 ==="
echo "时间: $(date '+%Y-%m-%d %H:%M GMT+8')"
echo ""

# 1. Git状态
cd /root/workspace/AI-OA
COMMITS=$(git rev-list --count HEAD)
echo "Git提交: $COMMITS commits"

# 2. 测试状态
cd /root/workspace/AI-OA/source/backend
mvn test -q 2>/dev/null | grep "BUILD" | tail -1
echo ""

# 3. 模块测试数
echo "--- 模块测试数 ---"
mvn test -q 2>/dev/null | grep "Tests run:" | grep -v "Time elapsed" | awk -F'Tests run: ' '{split($2,a,","); s+=a[1]} END {print "总计:", s+0, "tests"}'

# 4. 部署包状态
echo ""
echo "--- 部署包 ---"
echo "Docker: $(ls /root/workspace/AI-OA/packages/docker/docker-compose.yml 2>/dev/null && echo '✅' || echo '❌')"
echo "K8s: $(find /root/workspace/AI-OA/packages/k8s/services -name "*.yaml" 2>/dev/null | wc -l) service files"
echo "前端: $(ls /root/workspace/AI-OA/source/frontend/dist/index.html 2>/dev/null && echo '✅' || echo '❌')"
echo "HarmonyOS: $(find /root/workspace/AI-OA/packages/harmonyos -name "*.ets" 2>/dev/null | wc -l) pages"

echo ""
echo "=== 汇报完成 ==="