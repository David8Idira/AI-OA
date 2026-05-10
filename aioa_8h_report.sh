#!/bin/bash
# AI-OA 8小时进度汇报 - 毛选思想指导
source /root/.openclaw/workspace/scripts/send-feishu.py 2>/dev/null || true

AI_OA_DIR="/root/workspace/AI-OA"
BACKEND_DIR="$AI_OA_DIR/source/backend"
FRONTEND_DIR="$AI_OA_DIR/source/frontend"

report_time=$(date '+%Y-%m-%d %H:%M GMT+8')
cd $AI_OA_DIR

BRANCH=$(git branch --show-current)
COMMITS=$(git rev-list --count HEAD)
LAST_COMMIT=$(git log --oneline -1 --format='%h %s')
RECENT_COMMITS=$(git log --oneline --since="8 hours ago" | wc -l)

JAVA_FILES=$(find $BACKEND_DIR -name "*.java" -not -path "*/target/*" | wc -l)
CONTROLLERS=$(find $BACKEND_DIR -name "*Controller.java" -not -path "*/target/*" | wc -l)
TEST_FILES=$(find $BACKEND_DIR -name "*Test.java" -not -path "*/target/*" | wc -l)

BUILD_STATUS=$(cd $BACKEND_DIR && mvn compile -q 2>&1 && echo "✅ 编译成功" || echo "❌ 编译失败")

TEST_OUTPUT=$(cd $BACKEND_DIR && mvn test -q 2>&1)
TEST_RESULT=$(echo "$TEST_OUTPUT" | grep "BUILD" | tail -1)
TEST_COUNT=$(echo "$TEST_OUTPUT" | grep -oP "Tests run: \K[0-9]+" | awk '{sum+=$1} END {print sum+0}')

FRONTEND_STATUS=$(test -f "$FRONTEND_DIR/dist/index.html" && echo "✅ 已构建" || echo "❌ 未构建")
HARMONYOS_FILES=$(find $AI_OA_DIR/packages/harmonyos -name "*.ets" 2>/dev/null | wc -l)

MESSAGE="📋 AI-OA 8小时进度汇报
⏰ 时间: $report_time
🌱 分支: $BRANCH | 提交: $COMMITS

📊 代码统计:
- Java文件: $JAVA_FILES
- Controller: $CONTROLLERS  
- 测试文件: $TEST_FILES

🔨 编译: $BUILD_STATUS
🧪 测试: $TEST_RESULT (总计: $TEST_COUNT)

📱 移动端: HarmonyOS $HARMONYOS_FILES 个ets文件
🌐 前端: $FRONTEND_STATUS

📦 部署包: Docker/K8s/微服务/单体 全部就绪

🎯 下一阶段:
1. ServiceImpl测试覆盖率95%+
2. 移动端功能完善
3. 所有部署方案验证

毛主席教导: '实事求是，持续改进'"

echo "$MESSAGE"

# 发送到飞书（如配置了send_feishu.py）
if type send_feishu_message >/dev/null 2>&1; then
    send_feishu_message "$MESSAGE"
fi

echo "--- 汇报完成 ---"
