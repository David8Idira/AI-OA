<template>
  <div class="ai-chat-container">
    <div class="chat-header">
      <h2>🤖 AI 助手</h2>
      <div class="model-selector">
        <el-select v-model="selectedModel" placeholder="选择AI模型">
          <el-option label="GPT-4o" value="gpt-4o" />
          <el-option label="Kimi Pro" value="kimi-pro" />
          <el-option label="Claude 3.5" value="claude-3.5" />
        </el-select>
      </div>
    </div>
    
    <!-- 对话区域 -->
    <div class="chat-messages" ref="messagesRef">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message', msg.role]"
      >
        <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
        <div class="content">
          <div class="text">{{ msg.content }}</div>
          <div v-if="msg.role === 'assistant'" class="actions">
            <el-button size="small" text @click="copyText(msg.content)">复制</el-button>
            <el-button size="small" text @click="regenerate(msg)">重新生成</el-button>
          </div>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="message assistant loading">
        <div class="avatar">🤖</div>
        <div class="content">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>AI正在思考...</span>
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
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="3"
        placeholder="输入消息... (Enter发送, Shift+Enter换行)"
        @keydown.enter.exact.prevent="handleSend"
        @keydown.enter.shift="handleNewLine"
      />
      <el-button
        type="primary"
        :loading="loading"
        @click="handleSend"
        class="send-btn"
      >
        发送
      </el-button>
    </div>
    
    <!-- 配额提示 -->
    <div v-if="quotaInfo" class="quota-info">
      <el-progress
        :percentage="quotaInfo.usagePercent"
        :status="quotaInfo.usagePercent > 80 ? 'exception' : undefined"
      />
      <span>今日配额: {{ quotaInfo.used }}/{{ quotaInfo.dailyLimit }} tokens</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { chat, getModels, getQuota } from '@/api/ai'
import { ElMessage } from 'element-plus'

const messagesRef = ref<HTMLElement>()
const inputText = ref('')
const messages = ref<Array<{role: string, content: string}>>([])
const loading = ref(false)
const selectedModel = ref('gpt-4o')
const quotaInfo = ref<any>(null)

const quickCommands = [
  '总结本周工作',
  '生成月度报表',
  '帮我写周报',
  '查询审批进度',
  '解释什么是RPA',
  '写一份产品需求文档'
]

// 发送消息
const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return
  
  // 添加用户消息
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  try {
    const res = await chat({
      message: text,
      modelCode: selectedModel.value,
      conversationId: 'default'
    })
    
    messages.value.push({
      role: 'assistant',
      content: res.data?.reply || 'AI无响应'
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
  
  // 更新配额
  loadQuota()
}

// 快捷指令
const sendQuickCommand = (cmd: string) => {
  inputText.value = cmd
  handleSend()
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
const regenerate = async (msg: any) => {
  const lastUserMsg = messages.value.slice().reverse().find(m => m.role === 'user')
  if (lastUserMsg) {
    inputText.value = lastUserMsg.content
    messages.value = messages.value.filter(m => m !== msg)
    handleSend()
  }
}

// 加载配额
const loadQuota = async () => {
  try {
    const res = await getQuota()
    quotaInfo.value = res.data
  } catch (e) {
    // 忽略
  }
}

onMounted(() => {
  // 欢迎消息
  messages.value.push({
    role: 'assistant',
    content: '你好！我是AI助手，有什么可以帮你的？'
  })
  loadQuota()
})
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  padding: 20px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chat-header h2 {
  margin: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
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

.actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.loading {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-icon {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
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

.send-btn {
  height: 80px;
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