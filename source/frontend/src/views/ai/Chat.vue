<template>
  <div class="ai-chat-container">
    <div class="chat-layout">
      <!-- 左侧：会话历史 -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h3>对话历史</h3>
          <el-button type="primary" size="small" @click="newChat">
            <el-icon><Plus /></el-icon>
            新建
          </el-button>
        </div>
        <div class="chat-list">
          <div
            v-for="chat in chatSessions"
            :key="chat.id"
            :class="['chat-item', { active: chat.id === currentChatId }]"
            @click="selectChat(chat.id)"
          >
            <div class="chat-item-title">{{ chat.title }}</div>
            <div class="chat-item-time">{{ chat.time }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧：聊天区域 -->
      <div class="chat-main">
        <div class="chat-header">
          <h2>AI 助手</h2>
          <div class="model-selector">
            <span class="model-label">当前模型：</span>
            <el-select v-model="selectedModel" size="small" style="width: 150px">
              <el-option label="GPT-4o" value="gpt-4o" />
              <el-option label="Kimi Pro" value="kimi-pro" />
              <el-option label="Claude 3.5" value="claude-3.5" />
            </el-select>
          </div>
        </div>

        <!-- 对话区域 -->
        <div class="chat-messages" ref="messagesRef">
          <!-- 欢迎页面 -->
          <div v-if="messages.length === 0" class="welcome-screen">
            <el-icon :size="64" color="#667eea"><ChatDotRound /></el-icon>
            <h2>AI 智能助手</h2>
            <p>我可以帮你解答问题、生成内容、查询信息</p>
            <div class="quick-prompts">
              <el-button @click="sendQuickPrompt('本月报销总额是多少？')">本月报销总额</el-button>
              <el-button @click="sendQuickPrompt('帮我生成一份月度工作报告')">生成月报</el-button>
              <el-button @click="sendQuickPrompt('查询公司的休假政策')">休假政策</el-button>
              <el-button @click="sendQuickPrompt('帮我写一封邮件')">写邮件</el-button>
            </div>
          </div>

          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
            <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
            <div class="content">
              <div class="text" v-html="formatText(msg.content)"></div>
              <div v-if="msg.references && msg.references.length > 0" class="references">
                <span class="ref-label">参考来源：</span>
                <el-link
                  v-for="(ref, idx) in msg.references"
                  :key="idx"
                  :href="ref.url"
                  target="_blank"
                  type="primary"
                  size="small"
                  class="ref-link"
                >
                  {{ ref.title }}
                </el-link>
              </div>
              <div v-if="msg.role === 'assistant'" class="actions">
                <el-button size="small" text @click="copyText(msg.content)">复制</el-button>
                <el-button size="small" text @click="regenerate(index)">重新生成</el-button>
              </div>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="message assistant loading">
            <div class="avatar">🤖</div>
            <div class="content">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷指令 -->
        <div class="quick-commands">
          <el-tag
            v-for="cmd in quickCommands"
            :key="cmd"
            class="command-tag"
            @click="sendQuickCommand(cmd)"
          >
            {{ cmd }}
          </el-tag>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-toolbar">
            <el-tooltip content="上传图片" placement="top">
              <el-button text size="small" @click="triggerUpload">
                <el-icon><Picture /></el-icon>
              </el-button>
            </el-tooltip>
            <input ref="uploadInput" type="file" accept="image/*" style="display: none" @change="handleUpload" />
          </div>
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入消息... (Enter发送, Shift+Enter换行)"
            @keydown.enter.exact.prevent="handleSend"
            @keydown.enter.shift="handleNewLine"
          />
          <el-button
            type="primary"
            :loading="loading"
            :disabled="!inputText.trim()"
            @click="handleSend"
            class="send-btn"
          >
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>

        <!-- 配额提示 -->
        <div v-if="quotaInfo" class="quota-info">
          <el-progress
            :percentage="quotaInfo.usagePercent"
            :status="quotaInfo.usagePercent > 80 ? 'exception' : undefined"
            :stroke-width="6"
          />
          <span>今日配额: {{ quotaInfo.used }}/{{ quotaInfo.dailyLimit }} tokens</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const messagesRef = ref<HTMLElement>()
const uploadInput = ref<HTMLInputElement | null>(null)
const inputText = ref('')
const messages = ref<Array<{role: string, content: string, references?: Array<{title: string, url: string, source: string}>}>>([])
const loading = ref(false)
const selectedModel = ref('gpt-4o')
const quotaInfo = ref<any>(null)

// 会话管理
const currentChatId = ref('chat-1')
const chatSessions = ref([
  { id: 'chat-1', title: '本月报销咨询', time: '10:30' },
  { id: 'chat-2', title: '休假政策查询', time: '昨天' },
  { id: 'chat-3', title: '工作报告生成', time: '04-15' }
])

const quickCommands = [
  '总结本周工作',
  '生成月度报表',
  '帮我写周报',
  '查询审批进度',
  '解释什么是RPA',
  '写一份产品需求文档'
]

const formatText = (text: string) => {
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
}

const selectChat = (id: string) => {
  currentChatId.value = id
  messages.value = []
}

const newChat = () => {
  const newId = `chat-${Date.now()}`
  chatSessions.value.unshift({ id: newId, title: '新对话', time: '刚刚' })
  currentChatId.value = newId
  messages.value = []
}

const sendQuickPrompt = (cmd: string) => {
  inputText.value = cmd
  handleSend()
}

// 发送消息
const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true

  await nextTick()
  scrollToBottom()

  // 更新会话标题
  if (messages.value.length === 1) {
    const session = chatSessions.value.find(s => s.id === currentChatId.value)
    if (session) {
      session.title = text.substring(0, 20) + (text.length > 20 ? '...' : '')
    }
  }

  try {
    // 模拟AI响应
    await new Promise(resolve => setTimeout(resolve, 1500))

    const reply = generateAIResponse(text)
    messages.value.push({
      role: 'assistant',
      content: reply,
      references: [
        { title: '公司报销管理制度', url: '/knowledge/doc/1', source: '知识库' },
        { title: '审批流程说明', url: '/workflow/approval/guide', source: '流程中心' }
      ]
    })
  } catch (error: any) {
    ElMessage.error(error.message || 'AI服务调用失败')
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI服务暂时不可用'
    })
  }

  loading.value = false
  await nextTick()
  scrollToBottom()
  loadQuota()
}

const generateAIResponse = (question: string): string => {
  if (question.includes('报销')) {
    return `**本月报销数据汇总：**

- **报销总额**：¥45,680.00
- **报销笔数**：32笔
- **平均单笔**：¥1,427.50
- **待审批**：5笔

相比上月，报销总额增长了 8%。`
  }
  if (question.includes('月报') || question.includes('报告')) {
    return `**月度工作报告**

**工作完成情况：**
1. 完成项目需求开发 12 项
2. 修复 Bug 23 个
3. 参与技术评审 4 次

**下月计划：**
1. 推进 AI-OA 项目前端开发
2. 完成知识库模块集成`
  }
  if (question.includes('休假') || question.includes('请假')) {
    return `**公司休假政策：**

1. **年假**：入职满1年享5天
2. **病假**：每月2天带薪
3. **事假**：需提前3天申请

详细请参考《员工手册》第3.2章节`
  }
  return `收到您的问题，以下是相关回答：

这个问题涉及到公司制度和流程，建议您参考知识库中的相关文档获取详细信息。`
}

// 换行
const handleNewLine = () => {
  inputText.value += '\n'
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 复制
const copyText = (text: string) => {
  navigator.clipboard.writeText(text)
  ElMessage.success('已复制')
}

// 重新生成
const regenerate = async (index: number) => {
  const lastUserMsg = messages.value.slice(0, index).reverse().find(m => m.role === 'user')
  if (lastUserMsg) {
    messages.value.splice(index, 1)
    loading.value = true
    await new Promise(resolve => setTimeout(resolve, 1500))
    messages.value.push({
      role: 'assistant',
      content: '这是重新生成的内容...',
      references: [{ title: '重新生成', url: '#', source: 'AI' }]
    })
    loading.value = false
    scrollToBottom()
  }
}

const triggerUpload = () => {
  uploadInput.value?.click()
}

const handleUpload = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    ElMessage.info(`已选择图片: ${input.files[0].name}`)
  }
}

// 加载配额
const loadQuota = async () => {
  try {
    quotaInfo.value = { usagePercent: 45, used: 156, dailyLimit: 1000 }
  } catch (e) {
    // 忽略
  }
}

onMounted(() => {
  loadQuota()
})
</script>

<style scoped>
.ai-chat-container {
  height: calc(100vh - 120px);
  padding: 0;
}

.chat-layout {
  display: flex;
  height: 100%;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
}

.chat-sidebar {
  width: 240px;
  background: #f5f7fa;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e8e8e8;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
}

.chat-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #eee;
  transition: background 0.2s;
}

.chat-item:hover {
  background: #e8ecf1;
}

.chat-item.active {
  background: #e3eaff;
  border-left: 3px solid #667eea;
}

.chat-item-title {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chat-header h2 {
  margin: 0;
}

.model-label {
  font-size: 14px;
  color: #666;
  margin-right: 8px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.welcome-screen {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.welcome-screen h2 {
  margin: 16px 0 8px;
  color: #333;
}

.quick-prompts {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.message {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.message.assistant {
  flex-direction: row;
}

.message.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  margin: 0 12px;
}

.message.user .avatar {
  background: #e3f2fd;
}

.message.assistant .avatar {
  background: #f3e5f5;
}

.content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
}

.message.user .content {
  background: #4a90e2;
  color: white;
}

.message.assistant .content {
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.text {
  line-height: 1.6;
  white-space: pre-wrap;
}

.references {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #eee;
}

.ref-label {
  font-size: 12px;
  color: #999;
  margin-right: 8px;
}

.ref-link {
  margin-right: 8px;
}

.actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.loading .content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.7; }
  30% { transform: translateY(-8px); opacity: 1; }
}

.quick-commands {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.command-tag {
  cursor: pointer;
}

.input-area {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-area .el-textarea {
  flex: 1;
}

.input-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.send-btn {
  height: 60px;
  padding: 0 24px;
}

.quota-info {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.quota-info .el-progress {
  width: 200px;
}
</style>