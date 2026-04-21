<template>
  <div :class="['ai-sidebar', { collapsed: !visible }]">
    <!-- 展开按钮 -->
    <div class="toggle-btn" @click="toggle">
      <span v-if="!visible">💬</span>
      <span v-else>✕</span>
    </div>
    
    <!-- 对话框主体 -->
    <div v-if="visible" class="chat-panel">
      <!-- 标题 -->
      <div class="chat-header">
        <h3>AI 助手</h3>
        <select v-model="selectedModel" class="model-select">
          <option value="gpt-4o">GPT-4o</option>
          <option value="kimi-pro">Kimi Pro</option>
          <option value="claude-3.5">Claude 3.5</option>
        </select>
      </div>
      
      <!-- 消息列表 -->
      <div class="messages" ref="messagesRef">
        <div 
          v-for="(msg, index) in messages" 
          :key="index"
          :class="['message', msg.role]"
        >
          <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
          <div class="content">{{ msg.content }}</div>
        </div>
        <div v-if="loading" class="message assistant loading">
          <div class="avatar">🤖</div>
          <div class="content">思考中...</div>
        </div>
      </div>
      
      <!-- 快捷指令 -->
      <div class="quick-commands">
        <button 
          v-for="cmd in quickCommands" 
          :key="cmd"
          @click="sendQuick(cmd)"
        >
          {{ cmd }}
        </button>
      </div>
      
      <!-- 输入框 -->
      <div class="input-area">
        <textarea 
          v-model="inputText"
          @keydown.enter.exact.prevent="send"
          placeholder="输入消息... (Enter发送, Shift+Enter换行)"
          rows="2"
        ></textarea>
        <button @click="send" :disabled="!inputText.trim() || loading">
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { chat } from '@/api/ai'

// 状态
const visible = ref(true)
const inputText = ref('')
const messages = ref<Array<{role: string, content: string}>>([])
const loading = ref(false)
const selectedModel = ref('gpt-4o')
const messagesRef = ref<HTMLElement>()

// 快捷指令
const quickCommands = [
  '总结本周工作',
  '生成月度报表',
  '帮我写周报',
  '查询审批进度'
]

// 切换显示
const toggle = () => {
  visible.value = !visible.value
}

// 发送消息
const send = async () => {
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
    // 调用AI API
    const res = await chat({
      message: text,
      modelCode: selectedModel.value,
      conversationId: 'default'
    })
    
    // 添加AI回复
    messages.value.push({ 
      role: 'assistant', 
      content: res.data?.reply || 'AI回复内容' 
    })
  } catch (error) {
    messages.value.push({ 
      role: 'assistant', 
      content: '抱歉，AI服务暂时不可用' 
    })
  }
  
  loading.value = false
  await nextTick()
  scrollToBottom()
}

// 快捷指令
const sendQuick = (cmd: string) => {
  inputText.value = cmd
  send()
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

onMounted(() => {
  // 初始化欢迎消息
  messages.value.push({
    role: 'assistant',
    content: '你好！我是AI助手，有什么可以帮你的吗？'
  })
})
</script>

<style scoped>
.ai-sidebar {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1000;
}

.ai-sidebar.collapsed .toggle-btn {
  display: block;
}

.toggle-btn {
  width: 48px;
  height: 48px;
  border-radius: 24px;
  background: #4a90e2;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.toggle-btn:hover {
  background: #357abd;
}

.chat-panel {
  position: absolute;
  right: 60px;
  bottom: 0;
  width: 400px;
  height: 600px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
}

.model-select {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message {
  display: flex;
  margin-bottom: 12px;
}

.message .avatar {
  width: 32px;
  height: 32px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.message.user .avatar {
  background: #e3f2fd;
}

.message.assistant .avatar {
  background: #f3e5f5;
}

.message .content {
  max-width: 280px;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message.user {
  flex-direction: row-reverse;
}

.message.user .content {
  background: #4a90e2;
  color: white;
}

.message.assistant .content {
  background: #f5f5f5;
  color: #333;
}

.quick-commands {
  padding: 8px 16px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  border-top: 1px solid #eee;
}

.quick-commands button {
  padding: 4px 12px;
  border: 1px solid #ddd;
  border-radius: 16px;
  background: white;
  font-size: 12px;
  cursor: pointer;
}

.quick-commands button:hover {
  background: #f0f0f0;
}

.input-area {
  padding: 12px 16px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 8px;
}

.input-area textarea {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: none;
  font-size: 14px;
}

.input-area button {
  padding: 8px 20px;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.input-area button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>