<template>
  <div :class="['ai-sidebar-enhanced', { collapsed: !visible, 'fullscreen': isFullscreen }]">
    <!-- 展开/收起按钮 -->
    <div class="toggle-btn" @click="toggle">
      <span v-if="!visible">💬</span>
      <span v-else>✕</span>
    </div>
    
    <!-- 对话框主体 -->
    <div v-if="visible" class="chat-panel">
      <!-- 标题栏 -->
      <div class="chat-header">
        <div class="header-left">
          <h3>🤖 AI 智能助手</h3>
          <div class="status-indicator" :class="connectionStatus">
            <span class="dot"></span>
            <span class="text">{{ connectionStatusText }}</span>
          </div>
        </div>
        
        <div class="header-right">
          <!-- 模型选择 -->
          <el-select 
            v-model="selectedModel" 
            class="model-select"
            size="small"
            @change="onModelChange"
          >
            <el-option 
              v-for="model in availableModels" 
              :key="model.value"
              :label="model.label"
              :value="model.value"
            >
              <div class="model-option">
                <span class="model-icon">{{ model.icon }}</span>
                <span class="model-name">{{ model.label }}</span>
                <span v-if="model.quota" class="model-quota">{{ model.quota }}</span>
              </div>
            </el-option>
          </el-select>
          
          <!-- 功能按钮 -->
          <div class="header-actions">
            <el-tooltip content="截图提问" placement="bottom">
              <el-button 
                type="text" 
                size="small" 
                @click="screenshotQuestion"
                :disabled="!supportsScreenshot"
              >
                📸
              </el-button>
            </el-tooltip>
            
            <el-tooltip content="快捷指令" placement="bottom">
              <el-button type="text" size="small" @click="showQuickCommands">
                ⚡
              </el-button>
            </el-tooltip>
            
            <el-tooltip content="对话历史" placement="bottom">
              <el-button type="text" size="small" @click="showHistory">
                📚
              </el-button>
            </el-tooltip>
            
            <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" placement="bottom">
              <el-button type="text" size="small" @click="toggleFullscreen">
                {{ isFullscreen ? '📱' : '🖥️' }}
              </el-button>
            </el-tooltip>
            
            <el-tooltip content="设置" placement="bottom">
              <el-button type="text" size="small" @click="showSettings">
                ⚙️
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
      
      <!-- 对话区域 -->
      <div class="chat-container">
        <!-- 对话列表 -->
        <div class="messages" ref="messagesRef">
          <div 
            v-for="(msg, index) in messages" 
            :key="msg.id || index"
            :class="['message', msg.role, { 'thinking': msg.thinking }]"
          >
            <!-- 用户消息 -->
            <div v-if="msg.role === 'user'" class="message-user">
              <div class="avatar">👤</div>
              <div class="content-wrapper">
                <div class="content">{{ msg.content }}</div>
                <div v-if="msg.timestamp" class="timestamp">
                  {{ formatTime(msg.timestamp) }}
                </div>
              </div>
            </div>
            
            <!-- AI消息 -->
            <div v-else class="message-assistant">
              <div class="avatar">🤖</div>
              <div class="content-wrapper">
                <div class="model-badge">{{ getModelBadge(msg.model) }}</div>
                <div class="content">
                  <!-- 支持Markdown渲染 -->
                  <div v-if="msg.isMarkdown" v-html="renderMarkdown(msg.content)"></div>
                  <div v-else>{{ msg.content }}</div>
                  
                  <!-- 消息操作 -->
                  <div v-if="msg.role === 'assistant'" class="message-actions">
                    <el-button 
                      type="text" 
                      size="mini" 
                      @click="copyMessage(msg.content)"
                      title="复制"
                    >
                      📋
                    </el-button>
                    <el-button 
                      type="text" 
                      size="mini" 
                      @click="regenerateMessage(index)"
                      title="重新生成"
                    >
                      🔄
                    </el-button>
                    <el-button 
                      type="text" 
                      size="mini" 
                      @click="saveToKnowledgeBase(msg.content)"
                      title="保存到知识库"
                    >
                      💾
                    </el-button>
                    <el-button 
                      type="text" 
                      size="mini" 
                      @click="thumbsUp(index)"
                      title="点赞"
                      :class="{ 'liked': msg.liked }"
                    >
                      👍
                    </el-button>
                    <el-button 
                      type="text" 
                      size="mini" 
                      @click="thumbsDown(index)"
                      title="点踩"
                      :class="{ 'disliked': msg.disliked }"
                    >
                      👎
                    </el-button>
                  </div>
                </div>
                <div v-if="msg.timestamp" class="timestamp">
                  {{ formatTime(msg.timestamp) }}
                  <span v-if="msg.tokenCount" class="token-count">
                    ({{ msg.tokenCount }} tokens)
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 思考中状态 -->
          <div v-if="loading" class="message assistant thinking">
            <div class="avatar">🤖</div>
            <div class="content-wrapper">
              <div class="thinking-indicator">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
              <div class="thinking-text">AI正在思考中...</div>
            </div>
          </div>
        </div>
        
        <!-- 快捷指令面板 -->
        <div v-if="showQuickPanel" class="quick-commands-panel">
          <div class="panel-header">
            <h4>⚡ 快捷指令</h4>
            <el-button type="text" size="mini" @click="showQuickPanel = false">✕</el-button>
          </div>
          <div class="commands-grid">
            <button 
              v-for="cmd in categorizedCommands" 
              :key="cmd.id"
              @click="sendQuick(cmd.text)"
              class="command-btn"
              :title="cmd.description"
            >
              <span class="command-icon">{{ cmd.icon }}</span>
              <span class="command-text">{{ cmd.text }}</span>
              <span v-if="cmd.hotkey" class="command-hotkey">{{ cmd.hotkey }}</span>
            </button>
          </div>
          <div class="panel-footer">
            <el-button type="text" size="mini" @click="customizeCommands">
              自定义指令
            </el-button>
          </div>
        </div>
        
        <!-- 对话历史面板 -->
        <div v-if="showHistoryPanel" class="history-panel">
          <div class="panel-header">
            <h4>📚 对话历史</h4>
            <el-button type="text" size="mini" @click="showHistoryPanel = false">✕</el-button>
          </div>
          <div class="history-list">
            <div 
              v-for="session in chatSessions" 
              :key="session.id"
              class="history-item"
              :class="{ active: session.id === activeSessionId }"
              @click="loadSession(session.id)"
            >
              <div class="history-title">{{ session.title }}</div>
              <div class="history-info">
                <span class="history-time">{{ formatTime(session.lastActive) }}</span>
                <span class="history-count">{{ session.messageCount }} 条消息</span>
              </div>
              <el-button 
                type="text" 
                size="mini" 
                @click.stop="deleteSession(session.id)"
                class="delete-btn"
              >
                删除
              </el-button>
            </div>
          </div>
          <div class="panel-footer">
            <el-button type="text" size="mini" @click="newSession">
              新建对话
            </el-button>
            <el-button type="text" size="mini" @click="exportHistory">
              导出历史
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="input-area">
        <!-- 附件上传 -->
        <div class="input-attachments">
          <el-upload
            action="#"
            :before-upload="handleFileUpload"
            :show-file-list="false"
            :multiple="true"
            accept="image/*,.pdf,.doc,.docx,.txt"
          >
            <el-button type="text" size="small" title="上传文件">
              📎
            </el-button>
          </el-upload>
          
          <el-button 
            type="text" 
            size="small" 
            @click="takeScreenshot"
            :disabled="!supportsScreenshot"
            title="截图"
          >
            📸
          </el-button>
          
          <el-button 
            type="text" 
            size="small" 
            @click="showEmojiPicker = !showEmojiPicker"
            title="表情"
          >
            😊
          </el-button>
          
          <!-- 表情选择器 -->
          <div v-if="showEmojiPicker" class="emoji-picker">
            <div 
              v-for="emoji in emojis" 
              :key="emoji"
              class="emoji-item"
              @click="insertEmoji(emoji)"
            >
              {{ emoji }}
            </div>
          </div>
        </div>
        
        <!-- 输入框 -->
        <div class="input-main">
          <textarea 
            ref="inputRef"
            v-model="inputText"
            @keydown.enter.exact.prevent="send"
            @keydown.ctrl.enter="insertNewline"
            @keydown.up="showHistorySuggestions"
            placeholder="输入消息... (Enter发送, Ctrl+Enter换行)"
            rows="3"
            :disabled="loading"
          ></textarea>
          
          <!-- 输入建议 -->
          <div v-if="showSuggestions" class="input-suggestions">
            <div 
              v-for="suggestion in suggestions" 
              :key="suggestion"
              class="suggestion-item"
              @click="applySuggestion(suggestion)"
            >
              {{ suggestion }}
            </div>
          </div>
        </div>
        
        <!-- 发送按钮 -->
        <div class="input-actions">
          <el-button 
            type="primary" 
            @click="send" 
            :disabled="!inputText.trim() || loading"
            :loading="loading"
            class="send-btn"
          >
            {{ loading ? '思考中...' : '发送' }}
            <span v-if="!loading" class="send-shortcut">↵</span>
          </el-button>
          
          <el-button 
            type="text" 
            @click="clearConversation"
            title="清空对话"
          >
            清空
          </el-button>
        </div>
        
        <!-- 输入提示 -->
        <div class="input-hints">
          <span class="hint-item">💡 提示：使用 @ 提及知识库内容</span>
          <span class="hint-item">⚡ 快捷指令：输入 / 查看可用命令</span>
          <span class="hint-item">📊 支持：文本、图片、文件、截图</span>
        </div>
      </div>
      
      <!-- 状态栏 -->
      <div class="status-bar">
        <div class="status-left">
          <span class="connection-status" :class="connectionStatus">
            {{ connectionStatusText }}
          </span>
          <span v-if="selectedModel" class="model-status">
            模型：{{ getModelLabel(selectedModel) }}
          </span>
          <span v-if="tokenUsage" class="token-status">
            已用：{{ tokenUsage.used }} / {{ tokenUsage.total }} tokens
          </span>
        </div>
        <div class="status-right">
          <span class="typing-indicator" v-if="someoneTyping">
            {{ someoneTyping }} 正在输入...
          </span>
          <span class="last-updated">
            最后更新：{{ formatTime(lastUpdated) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { chat, getModels, uploadFile, screenshotQuestion as apiScreenshot } from '@/api/ai'
import { useChatStore } from '@/stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import MarkdownIt from 'markdown-it'

// 状态
const visible = ref(true)
const inputText = ref('')
const messages = ref<any[]>([])
const loading = ref(false)
const selectedModel = ref('gpt-4o')
const isFullscreen = ref(false)
const showQuickPanel = ref(false)
const showHistoryPanel = ref(false)
const showEmojiPicker = ref(false)
const showSuggestions = ref(false)
const suggestions = ref<string[]>([])
const someoneTyping = ref<string>('')
const lastUpdated = ref(new Date())
const inputRef = ref<HTMLTextAreaElement>()

// 存储
const chatStore = useChatStore()

// 计算属性
const connectionStatus = computed(() => {
  return 'connected' // 实际应该根据WebSocket状态判断
})

const connectionStatusText = computed(() => {
  switch (connectionStatus.value) {
    case 'connected': return '已连接'
    case 'connecting': return '连接中...'
    case 'disconnected': return '已断开'
    default: return '未知状态'
  }
})

const tokenUsage = computed(() => {
  return {
    used: 1250,
    total: 10000,
    percentage: 12.5
  }
})

const categorizedCommands = computed(() => [
  { id: 1, icon: '📊', text: '生成周报', description: '基于本周工作生成周报', hotkey: '/week' },
  { id: 2, icon: '📈', text: '数据分析', description: '分析数据趋势', hotkey: '/analyze' },
  { id: 3, icon: '📝', text: '写邮件', description: '起草工作邮件', hotkey: '/email' },
  { id: 4, icon: '🔍', text: '搜索知识库', description: '在知识库中搜索', hotkey: '/search' },
  { id: 5, icon: '📋', text: '创建待办', description: '创建待办事项', hotkey: '/todo' },
  { id: 6, icon: '📅', text: '安排会议', description: '安排会议日程', hotkey: '/meeting' },
  { id: 7, icon: '💡', text: '头脑风暴', description: '创意想法生成', hotkey: '/brainstorm' },
  { id: 8, icon: '🔧', text: '代码帮助', description: '编程问题解答', hotkey: '/code' },
  { id: 9, icon: '📚', text: '学习总结', description: '学习内容总结', hotkey: '/learn' },
  { id: 10, icon: '🎯', text: '设定目标', description: '设定SMART目标', hotkey: '/goal' }
])

const availableModels = computed(() => [
  { value: 'gpt-4o', label: 'GPT-4o', icon: '🤖', quota: '快速' },
  { value: 'kimi-pro', label: 'Kimi Pro', icon: '🔍', quota: '长文本' },
  { value: 'claude-3.5', label: 'Claude 3.5', icon: '🧠', quota: '推理' },
  { value: 'minimax-m2.7', label: 'MiniMax M2.7', icon: '⚡', quota: '经济' },
  { value: 'local-llama', label: '本地 Llama', icon: '🏠', quota: '离线' }
])

const chatSessions = computed(() => chatStore.sessions)
const activeSessionId = computed(() => chatStore.activeSessionId)

// 工具函数
const md = new MarkdownIt()

const formatTime = (date: Date | string) => {
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit',
    hour12: false
  })
}

const getModelBadge = (model: string) => {
  const modelInfo = availableModels.value.find(m => m.value === model)
  return modelInfo ? modelInfo.icon + ' ' + modelInfo.label : '🤖 AI'
}

const getModelLabel = (model: string) => {
  const modelInfo = availableModels.value.find(m => m.value === model)
  return modelInfo ? modelInfo.label : '未知模型'
}

const renderMarkdown = (content: string) => {
  return md.render(content)
}

// 方法
const toggle = () => {
  visible.value = !visible.value
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

const onModelChange = (model: string) => {
  ElMessage.success(`已切换到 ${getModelLabel(model)} 模型`)
  // 这里可以添加模型切换的逻辑
}

const send = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return
  
  // 添加用户消息
  const userMessage = {
    id: Date.now(),
    role: 'user',
    content: text,
    timestamp: new Date(),
    model: selectedModel.value
  }
  
  messages.value.push(userMessage)
  inputText.value = ''
  loading.value = true
  
  // 保存到历史
  chatStore.addMessage(userMessage)
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  try {
    // 调用AI接口
    const response = await chat({
      message: text,
      model: selectedModel.value,
      context: messages.value.slice(-10).map(m => ({ role: m.role, content: m.content }))
    })
    
    // 添加AI回复
    const aiMessage = {
      id: Date.now() + 1,
      role: 'assistant',
      content: response.content,
      model: selectedModel.value,
      timestamp: new Date(),
      tokenCount: response.tokenCount,
      isMarkdown: true,
      thinking: false
    }
    
    messages.value.push(aiMessage)
    chatStore.addMessage(aiMessage)
    
    // 更新最后活跃时间
    chatStore.updateSessionLastActive()
    
  } catch (error) {
    ElMessage.error('发送消息失败：' + (error as Error).message)
    
    // 添加错误消息
    const errorMessage = {
      id: Date.now() + 1,
      role: 'assistant',
      content: '抱歉，消息发送失败。请检查网络连接或稍后重试。',
      model: selectedModel.value,
      timestamp: new Date(),
      isError: true
    }
    
    messages.value.push(errorMessage)
    chatStore.addMessage(errorMessage)
    
  } finally {
    loading.value = false
    lastUpdated.value = new Date()
    await nextTick()
    scrollToBottom()
  }
}

const sendQuick = (command: string) => {
  inputText.value = command
  send()
  showQuickPanel.value = false
}

const showQuickCommands = () => {
  showQuickPanel.value = !showQuickPanel.value
  showHistoryPanel.value = false
}

const showHistory = () => {
  showHistoryPanel.value = !showHistoryPanel.value
  showQuickPanel.value = false
}

const showSettings = () => {
  ElMessageBox.alert('设置功能开发中...', '设置', {
    confirmButtonText: '确定'
  })
}

const copyMessage = (content: string) => {
  navigator.clipboard.writeText(content)
    .then(() => ElMessage.success('已复制到剪贴板'))
    .catch(() => ElMessage.error('复制失败'))
}

const regenerateMessage = async (index: number) => {
  // 重新生成消息的逻辑
  ElMessage.info('重新生成功能开发中...')
}

const saveToKnowledgeBase = (content: string) => {
  ElMessageBox.prompt('请输入知识库条目标题', '保存到知识库', {
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    inputPlaceholder: '知识库标题'
  }).then(({ value }) => {
    if (value) {
      // 调用知识库保存API
      ElMessage.success('已保存到知识库')
    }
  })
}

const thumbsUp = (index: number) => {
  messages.value[index].liked = true
  messages.value[index].disliked = false
  ElMessage.success('感谢反馈！')
}

const thumbsDown = (index: number) => {
  messages.value[index].disliked = true
  messages.value[index].liked = false
  ElMessage.success('感谢反馈！我们会改进。')
}

const loadSession = (sessionId: string) => {
  chatStore.setActiveSession(sessionId)
  messages.value = chatStore.getSessionMessages(sessionId)
  showHistoryPanel.value = false
}

const deleteSession = (sessionId: string) => {
  ElMessageBox.confirm('确定删除这个对话吗？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    chatStore.deleteSession(sessionId)
    ElMessage.success('对话已删除')
  })
}

const newSession = () => {
  chatStore.createNewSession()
  messages.value = []
  showHistoryPanel.value = false
  ElMessage.success('已创建新对话')
}

const exportHistory = () => {
  ElMessage.info('导出功能开发中...')
}

const handleFileUpload = (file: File) => {
  ElMessage.info(`已选择文件：${file.name}`)
  // 实际应该调用上传API
  return false // 阻止默认上传
}

const takeScreenshot = async () => {
  if (!supportsScreenshot) {
    ElMessage.warning('当前浏览器不支持截图功能')
    return
  }
  
  try {
    const screenshot = await apiScreenshot()
    // 处理截图
    ElMessage.success('截图已准备')
  } catch (error) {
    ElMessage.error('截图失败：' + (error as Error).message)
  }
}

const screenshotQuestion = () => {
  showQuickPanel.value = false
  takeScreenshot()
}

const insertEmoji = (emoji: string) => {
  inputText.value += emoji
  showEmojiPicker.value = false
}

const insertNewline = () => {
  const textarea = inputRef.value
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    inputText.value = inputText.value.substring(0, start) + '\n' + inputText.value.substring(end)
    nextTick(() => {
      textarea.selectionStart = textarea.selectionEnd = start + 1
      textarea.focus()
    })
  }
}

const showHistorySuggestions = () => {
  // 显示历史建议
  suggestions.value = chatStore.getRecentMessages(5).map(m => m.content)
  showSuggestions.value = true
}

const applySuggestion = (suggestion: string) => {
  inputText.value = suggestion
  showSuggestions.value = false
}

const clearConversation = () => {
  ElMessageBox.confirm('确定清空当前对话吗？', '确认清空', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    messages.value = []
    chatStore.clearCurrentSession()
    ElMessage.success('对话已清空')
  })
}

const scrollToBottom = () => {
  const messagesEl = messagesRef.value
  if (messagesEl) {
    messagesEl.scrollTop = messagesEl.scrollHeight
  }
}

const customizeCommands = () => {
  ElMessageBox.alert('快捷指令自定义功能开发中...', '自定义指令', {
    confirmButtonText: '确定'
  })
}

// 生命周期
onMounted(() => {
  // 加载当前会话消息
  if (chatStore.activeSessionId) {
    messages.value = chatStore.getSessionMessages(chatStore.activeSessionId)
  }
  
  // 设置自动滚动
  watch(messages, () => {
    nextTick(scrollToBottom)
  }, { deep: true })
  
  // 监听键盘快捷键
  document.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
})

const handleKeyDown = (e: KeyboardEvent) => {
  // Ctrl+/ 显示快捷指令
  if (e.ctrlKey && e.key === '/') {
    e.preventDefault()
    showQuickPanel.value = !showQuickPanel.value
    showHistoryPanel.value = false
  }
  
  // Ctrl+H 显示历史
  if (e.ctrlKey && e.key === 'h') {
    e.preventDefault()
    showHistoryPanel.value = !showHistoryPanel.value
    showQuickPanel.value = false
  }
  
  // Esc 关闭面板
  if (e.key === 'Escape') {
    showQuickPanel.value = false
    showHistoryPanel.value = false
    showEmojiPicker.value = false
    showSuggestions.value = false
  }
}

// 表情列表
const emojis = ['😊', '😂', '😍', '🥰', '😎', '🤔', '👍', '👏', '🎉', '🔥', '💯', '✨', '🌟', '💡', '📚', '🔧', '⚡', '🚀']

// 浏览器支持检测
const supportsScreenshot = 'mediaDevices' in navigator && 'getDisplayMedia' in navigator.mediaDevices
</script>

<style scoped>
.ai-sidebar-enhanced {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index:ాలు