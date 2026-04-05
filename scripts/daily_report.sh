#!/bin/bash
# AI-OA 每日进度汇报
# 执行时间：每日21:00

cd /root/workspace/AI-OA

# 获取今日提交
TODAY=$(date +%Y-%m-%d)
TODAY_COMMITS=$(git log --since="$TODAY 00:00:00" --until="$TODAY 23:59:59" --oneline 2>/dev/null | wc -l)
RECENT_COMMITS=$(git log --oneline -10)

# 获取项目统计
BACKEND_FILES=$(find source/backend -name "*.java" 2>/dev/null | wc -l)
FRONTEND_FILES=$(find source/frontend/src -name "*.vue" -o -name "*.ts" 2>/dev/null | wc -l)
DOCS_FILES=$(find docs -name "*.md" 2>/dev/null | wc -l)

# 获取分支信息
CURRENT_BRANCH=$(git branch --show-current)
LAST_COMMIT=$(git log -1 --format="%h %s %an %ci")

# 生成汇报内容
REPORT="AI-OA 项目每日进度汇报
===========================
日期：$(date '+%Y年%m月%d日 %H:%M')

📊 今日开发进展
---------------------------
今日提交：$TODAY_COMMITS 次
后端文件：$BACKEND_FILES 个
前端文件：$FRONTEND_FILES 个
文档数量：$DOCS_FILES 个

🌿 Git状态
---------------------------
当前分支：$CURRENT_BRANCH
最新提交：$LAST_COMMIT

📋 最近提交记录
---------------------------
$RECENT_COMMITS

📁 项目结构
---------------------------
source/
├── backend/          # Spring Boot后端
│   ├── aioa-common/ # 通用模块
│   ├── aioa-system/ # 系统模块(完成)
│   ├── aioa-workflow/# 审批流程(待开发)
│   ├── aioa-ai/    # AI模块(完成)
│   ├── aioa-im/     # 即时通讯(待开发)
│   └── aioa-gateway/# API网关(待开发)
└── frontend/         # Vue 3前端(完成)
    ├── views/       # 页面(登录/工作台)
    ├── components/  # 组件
    ├── router/     # 路由
    └── store/      # 状态管理

🔄 Sprint进度
---------------------------
Sprint 1 ✅ Week 1-2 - 基础框架 (已完成)
Sprint 2 ⏳ Week 3-4 - 核心功能 (进行中)
Sprint 3 ⬜ Week 5-6 - 高级功能 (待开始)
Sprint 4 ⬜ Week 7-8 - 增强功能 (待开始)
Sprint 5 ⬜ Week 9-10 - 测试阶段 (待开始)
Sprint 6 ⬜ Week 11-12 - 优化上线 (待开始)
Sprint 7 ⬜ Week 13-14 - 验收交付 (待开始)

📝 明日计划
---------------------------
- Sprint 2 核心功能开发
  - OCR发票识别集成
  - 报销申请/审批流程
  - 企业聊天基础功能

如有问题请随时联系！

A1"

echo "$REPORT"

# 发送邮件
python3 << 'PYEOF'
import smtplib, ssl, json
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

with open('/root/.openclaw/workspace/.mail_config.json') as f:
    config = json.load(f)
smtp = config['smtp']

msg = MIMEMultipart()
msg['From'] = smtp['from']
msg['To'] = 'liq_idira@126.com'
msg['Subject'] = '【AI-OA日报】$(date +%m月%d日) 开发进度'

msg.attach(MIMEText("""$REPORT""", 'plain', 'utf-8'))

context = ssl.create_default_context()
try:
    with smtplib.SMTP_SSL(smtp['host'], smtp['port'], context=context, timeout=30) as server:
        server.login(smtp['from'], smtp['password'])
        server.send_message(msg)
    print("邮件发送成功")
except Exception as e:
    print(f"邮件发送失败: {e}")
PYEOF
