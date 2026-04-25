#!/bin/bash
# AI-OA项目8小时定时进度汇报脚本
# 执行时间: 每8小时 (00:00, 08:00, 16:00)

DATE=$(date '+%Y-%m-%d %H:%M GMT+8')
LOG_FILE="/root/.openclaw/workspace/AI-OA_PROJECT_HOUR_REPORT.md"
cd /root/.openclaw/workspace/AI-OA-Project

# 获取模块信息
BACKEND_MODULES=$(ls -d source/backend/aioa-*/ 2>/dev/null | wc -l)
FRONTEND_VIEWS=$(ls source/frontend/src/views/ 2>/dev/null | wc -l)
JAVA_FILES=$(find source/backend -name "*.java" 2>/dev/null | wc -l)
TEST_FILES=$(find source/backend -name "*Test*.java" 2>/dev/null | wc -l)

# 获取git状态
LAST_COMMIT=$(git log -1 --format="%h %s" 2>/dev/null)
BRANCH=$(git branch --show-current 2>/dev/null)

# 生成进度报告
cat > "$LOG_FILE" << EOF
# AI-OA项目进度报告

**汇报时间**: $DATE  
**分支**: $BRANCH  
**最后提交**: $LAST_COMMIT

---

## 项目总体进度

| 指标 | 数值 |
|------|------|
| 后端模块数 | $BACKEND_MODULES |
| 前端页面数 | $FRONTEND_VIEWS |
| Java文件数 | $JAVA_FILES |
| 测试文件数 | $TEST_FILES |

---

## 模块完成情况

### 后端模块
$(for dir in source/backend/aioa-*/; do
  name=$(basename "$dir")
  java_count=$(find "$dir/src/main" -name "*.java" 2>/dev/null | wc -l)
  test_count=$(find "$dir/src/test" -name "*.java" 2>/dev/null | wc -l)
  echo "| $name | $java_count | $test_count |"
done)

### 前端视图
$(ls source/frontend/src/views/ 2>/dev/null | while read v; do echo "- [ ] $v"; done)

---

## 代码提交记录

\`\`\`
$LAST_COMMIT
\`\`\`

---

*报告生成时间: $(date '+%Y-%m-%d %H:%M:%S')*
EOF

echo "[$DATE] 进度报告已生成: $LOG_FILE"
