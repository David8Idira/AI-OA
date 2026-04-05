<template>
  <div class="chat-page">
    <!-- 左侧会话列表 -->
    <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="sidebar-title" v-if="!sidebarCollapsed">
          <h3>会话列表</h3>
          <el-badge :value="totalUnread" :hidden="totalUnread === 0">
            <el-icon><Message /></el-icon>
          </el-badge>
        </div>
        <el-icon class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <component :is="sidebarCollapsed ? 'DArrowRight' : 'DArrowLeft'" />
        </el-icon>
      </div>
      
      <div class="sidebar-search" v-if="!sidebarCollapsed">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索"
          :prefix-icon="Search"
          size="small"
          @input="handleSearchConversation"
        />
      </div>
      
      <el-scrollbar class="sidebar-list" v-if="!sidebarCollapsed">
        <div
          v-for="conv in filteredConversations"
          :key="conv.id"
          class="sidebar-item"
          :class="{ active: conv.id === currentConversationId }"
          @click="handleSwitchConversation(conv)"
        >
          <el-avatar :size="40" :src="conv.avatar">
            {{ getNameInitial(conv.name) }}
          </el-avatar>
          <div class="sidebar-item-content">
            <div class="sidebar-item-header">
              <span class="name">{{ conv.name }}</span>
              <span class="time">{{ formatRelativeTime(conv.lastMessageTime) }}</span>
            </div>
            <div class="sidebar-item-footer">
              <span class="last-msg">{{ conv.lastMessage }}</span>
              <el-badge
                v-if="conv.unreadCount > 0"
                :value="conv.unreadCount > 99 ? '99+' : conv.unreadCount"
                :max="99"
                class="unread-badge"
              />
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-main">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="chat-info">
          <el-avatar :size="40" :src="currentConversation?.avatar">
            {{ getNameInitial(currentConversation?.name || '') }}
          </el-avatar>
          <div class="chat-info-text">
            <span class="chat-name">{{ currentConversation?.name }}</span>
            <span class="chat-members" v-if="currentConversation?.type === 'GROUP'">
              {{ currentConversation?.members?.length || 0 }} 位成员
            </span>
          </div>
        </div>
        <div class="chat-actions">
          <el-button :icon="Search" circle @click="showSearchDialog = true" />
          <el-button :icon="MoreFilled" circle @click="showConversationInfo = true" />
        </div>
      </div>

      <!-- 消息区域 -->
      <el-scrollbar ref="scrollbarRef" class="message-area" @scroll="handleScroll">
        <!-- 加载更多 -->
        <div class="load-more" v-if="hasMore" v-loading="loadingMore">
          <span v-if="!loadingMore" class="load-more-btn" @click="loadMoreMessages">
            加载更多消息
          </span>
        </div>

        <!-- 时间分隔线 -->
        <div class="time-divider" v-if="showTimeDivider">
          <span>{{ formatDateDivider(messageList[0]?.createTime) }}</span>
        </div>

        <!-- 消息列表 -->
        <div
          v-for="(msg, index) in messageList"
          :key="msg.id"
          class="message-wrapper"
          :class="{ 'message-self': msg.isSelf }"
        >
          <!-- 时间分隔线：超过5分钟显示 -->
          <div
            class="time-divider-inline"
            v-if="shouldShowTimeDivider(msg.createTime, messageList[index - 1]?.createTime)"
          >
            <span>{{ formatMessageTime(msg.createTime) }}</span>
          </div>

          <!-- 消息气泡 -->
          <div class="message-content-wrapper">
            <!-- 头像（对方消息） -->
            <el-avatar
              v-if="!msg.isSelf"
              :size="36"
              :src="msg.senderAvatar"
              class="message-avatar"
            >
              {{ getNameInitial(msg.senderName) }}
            </el-avatar>

            <!-- 消息气泡 -->
            <div class="message-bubble">
              <div class="message-sender" v-if="msg.isSelf === false && currentConversation?.type === 'GROUP'">
                {{ msg.senderName }}
              </div>
              <div class="message-text" :class="{ 'text-self': msg.isSelf }">
                <!-- 文本消息 -->
                <template v-if="msg.type === 'TEXT'">
                  {{ msg.content }}
                </template>
                <!-- 图片消息 -->
                <template v-else-if="msg.type === 'IMAGE'">
                  <el-image
                    :src="msg.content"
                    :preview-src-list="[msg.content]"
                    fit="cover"
                    class="message-image"
                  />
                </template>
                <!-- 文件消息 -->
                <template v-else-if="msg.type === 'FILE'">
                  <div class="message-file">
                    <el-icon><Document /></el-icon>
                    <span>{{ msg.content }}</span>
                  </div>
                </template>
                <!-- 系统消息 -->
                <template v-else-if="msg.type === 'SYSTEM'">
                  <div class="message-system">{{ msg.content }}</div>
                </template>
              </div>
              <div class="message-time">{{ formatMessageTime(msg.createTime) }}</div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty
          v-if="messageList.length === 0 && !loading"
          description="暂无消息，发送消息开始聊天吧"
          :image-size="80"
        />
      </el-scrollbar>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-toolbar">
          <el-tooltip content="发送图片">
            <el-button text @click="handleSendImage">
              <el-icon><Picture /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="发送文件">
            <el-button text @click="handleSendFile">
              <el-icon><Document /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="表情">
            <el-button text @click="showEmojiPicker = !showEmojiPicker">
              <el-icon><Smile /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="聊天记录">
            <el-button text @click="showSearchDialog = true">
              <el-icon><Search /></el-icon>
            </el-button>
          </el-tooltip>
        </div>

        <!-- 表情选择器 -->
        <div class="emoji-picker" v-if="showEmojiPicker">
          <span
            v-for="emoji in emojiList"
            :key="emoji"
            class="emoji-item"
            @click="handleSelectEmoji(emoji)"
          >
            {{ emoji }}
          </span>
        </div>

        <!-- 草稿输入 -->
        <div class="input-box">
          <el-input
            ref="inputRef"
            v-model="inputText"
            type="textarea"
            :rows="3"
            :placeholder="`发送消息给 ${currentConversation?.name}`"
            resize="none"
            @keydown.enter.exact.prevent="handleSend"
            @keydown.enter.shift.exact="inputText += '\n'"
          />
        </div>

        <div class="input-footer">
          <span class="input-hint">
            <el-icon><InfoFilled /></el-icon>
            按 Enter 发送，Shift + Enter 换行
          </span>
          <el-button
            type="primary"
            :disabled="!inputText.trim()"
            :loading="sending"
            @click="handleSend"
          >
            <el-icon v-if="!sending"><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </div>

    <!-- 会话详情抽屉 -->
    <el-drawer
      v-model="showConversationInfo"
      title="会话详情"
      direction="rtl"
      size="320px"
    >
      <div class="conversation-info" v-if="currentConversation">
        <div class="info-header">
          <el-avatar :size="80" :src="currentConversation.avatar">
            {{ getNameInitial(currentConversation.name) }}
          </el-avatar>
          <h3>{{ currentConversation.name }}</h3>
          <el-tag v-if="currentConversation.type === 'GROUP'" size="small">
            群聊
          </el-tag>
          <el-tag v-else size="small" type="success">单聊</el-tag>
        </div>

        <el-divider />

        <div class="info-section" v-if="currentConversation.type === 'GROUP'">
          <h4>群成员 ({{ currentConversation.members?.length || 0 }})</h4>
          <div class="member-list">
            <div
              v-for="member in currentConversation.members"
              :key="member.id"
              class="member-item"
            >
              <el-avatar :size="32" :src="member.avatar">
                {{ getNameInitial(member.name) }}
              </el-avatar>
              <span class="member-name">{{ member.name }}</span>
              <el-tag v-if="member.role === 'OWNER'" size="small" type="warning">
                群主
              </el-tag>
              <el-tag v-else-if="member.role === 'ADMIN'" size="small">
                管理员
              </el-tag>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h4>会话操作</h4>
          <el-button
            v-if="!currentConversation.pinned"
            type="primary"
            plain
            @click="handlePinConversation"
          >
            <el-icon><Star /></el-icon>
            置顶会话
          </el-button>
          <el-button
            v-else
            plain
            @click="handlePinConversation"
          >
            <el-icon><Star /></el-icon>
            取消置顶
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 搜索消息对话框 -->
    <el-dialog
      v-model="showSearchDialog"
      title="搜索聊天记录"
      width="600px"
    >
      <el-input
        v-model="searchMessageKeyword"
        placeholder="输入关键词搜索"
        :prefix-icon="Search"
        clearable
        @keyup.enter="handleSearchMessage"
      />
      <el-scrollbar height="400px" class="search-results" v-if="searchResults.length > 0">
        <div
          v-for="msg in searchResults"
          :key="msg.id"
          class="search-result-item"
          @click="handleJumpToMessage(msg)"
        >
          <el-avatar :size="32" :src="msg.senderAvatar">
            {{ getNameInitial(msg.senderName) }}
          </el-avatar>
          <div class="result-content">
            <div class="result-header">
              <span class="sender-name">{{ msg.senderName }}</span>
              <span class="result-time">{{ formatMessageTime(msg.createTime) }}</span>
            </div>
            <div class="result-text">{{ msg.content }}</div>
          </div>
        </div>
      </el-scrollbar>
      <el-empty v-else-if="searched" description="未找到相关消息" />
      <template #footer>
        <el-button @click="showSearchDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  MoreFilled,
  Picture,
  Document,
  Smile,
  InfoFilled,
  Promotion,
  Star,
  DArrowLeft,
  DArrowRight,
  Message
} from '@element-plus/icons-vue'
import {
  getConversationList,
  getConversationDetail,
  getMessageHistory,
  sendMessage,
  markAsRead,
  generateMockConversations,
  generateMockMessages,
  formatMessageTime,
  formatRelativeTime,
  getRelativeTime,
  type ConversationVO,
  type MessageVO
} from '@/api/chat'

const route = useRoute()
const router = useRouter()

// ============ 状态定义 ============

const currentConversationId = ref<string>('')
const currentConversation = ref<ConversationVO | null>(null)
const messageList = ref<MessageVO[]>([])
const conversationList = ref<ConversationVO[]>([])

const inputText = ref('')
const sending = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)

const searchKeyword = ref('')
const showEmojiPicker = ref(false)
const showConversationInfo = ref(false)
const showSearchDialog = ref(false)
const searchMessageKeyword = ref('')
const searchResults = ref<MessageVO[]>([])
const searched = ref(false)
const sidebarCollapsed = ref(false)
const showTimeDivider = ref(false)

const inputRef = ref<HTMLTextAreaElement>()
const scrollbarRef = ref()

let pollingTimer: ReturnType<typeof setInterval> | null = null

// ============ 表情列表 ============

const emojiList = [
  '😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂',
  '😉', '😌', '😍', '🥰', '😘', '😋', '😛', '😜', '🤪', '😝',
  '🤗', '🤔', '👍', '👎', '👏', '🙌', '💪', '❤️', '🧡', '💛',
  '💚', '💙', '💜', '🖤', '🤍', '💯', '🔥', '⭐', '✨', '🌟'
]

// ============ 计算属性 ============

const totalUnread = computed(() => {
  return conversationList.value.reduce((sum, item) => sum + item.unreadCount, 0)
})

const filteredConversations = computed(() => {
  if (!searchKeyword.value) return conversationList.value
  const keyword = searchKeyword.value.toLowerCase()
  return conversationList.value.filter(conv =>
    conv.name.toLowerCase().includes(keyword)
  )
})

// ============ 生命周期 ============

onMounted(() => {
  loadConversations()
  
  // 从路由参数获取会话ID
  const id = route.params.id as string
  if (id) {
    currentConversationId.value = id
    loadConversationDetail(id)
    loadMessages(id)
  }
  
  // 启动消息轮询
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})

// 监听路由变化
watch(() => route.params.id, (newId) => {
  if (newId && newId !== currentConversationId.value) {
    currentConversationId.value = newId as string
    loadConversationDetail(newId as string)
    loadMessages(newId as string)
  }
})

// ============ 方法 ============

/** 加载会话列表 */
const loadConversations = async () => {
  try {
    const res = await getConversationList()
    conversationList.value = res.data
  } catch (error) {
    console.error('加载会话列表失败', error)
    conversationList.value = generateMockConversations()
  }
}

/** 加载会话详情 */
const loadConversationDetail = async (id: string) => {
  try {
    const res = await getConversationDetail(id)
    currentConversation.value = res.data
  } catch (error) {
    console.error('加载会话详情失败', error)
    // 从列表中查找
    const conv = conversationList.value.find(c => c.id === id)
    if (conv) {
      currentConversation.value = conv
    }
  }
}

/** 加载消息列表 */
const loadMessages = async (conversationId: string, before?: string) => {
  if (!before) {
    loading.value = true
    showTimeDivider.value = false
  } else {
    loadingMore.value = true
  }
  
  try {
    const res = await getMessageHistory({
      conversationId,
      pageSize: 20,
      before
    })
    
    if (before) {
      messageList.value = [...res.data.list, ...messageList.value]
    } else {
      messageList.value = res.data.list
    }
    
    hasMore.value = res.data.hasMore
    showTimeDivider.value = messageList.value.length > 0
    
    // 滚动到底部
    nextTick(() => {
      if (!before) {
        scrollToBottom()
      }
    })
    
    // 标记已读
    if (!before) {
      markAsRead(conversationId).catch(console.error)
    }
  } catch (error) {
    console.error('加载消息失败', error)
    if (!before) {
      messageList.value = generateMockMessages(conversationId)
      showTimeDivider.value = true
      nextTick(() => scrollToBottom())
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

/** 加载更多消息 */
const loadMoreMessages = () => {
  if (messageList.value.length > 0) {
    const oldestMessage = messageList.value[0]
    loadMessages(currentConversationId.value, oldestMessage.createTime)
  }
}

/** 发送消息 */
const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || !currentConversationId.value) return
  
  sending.value = true
  const content = text
  inputText.value = ''
  
  try {
    // 先添加到本地（乐观更新）
    const tempMessage: MessageVO = {
      id: `temp-${Date.now()}`,
      conversationId: currentConversationId.value,
      senderId: 'current-user',
      senderName: '我',
      content,
      type: 'TEXT',
      createTime: new Date().toISOString(),
      isSelf: true
    }
    messageList.value.push(tempMessage)
    scrollToBottom()
    
    // 发送到服务器
    const res = await sendMessage({
      conversationId: currentConversationId.value,
      content,
      type: 'TEXT'
    })
    
    // 更新为真实消息
    const index = messageList.value.findIndex(m => m.id === tempMessage.id)
    if (index > -1) {
      messageList.value[index] = res.data
    }
    
    // 更新会话列表中的最后消息
    const conv = conversationList.value.find(c => c.id === currentConversationId.value)
    if (conv) {
      conv.lastMessage = content
      conv.lastMessageTime = res.data.createTime
    }
  } catch (error) {
    console.error('发送消息失败', error)
    ElMessage.error('发送失败，请重试')
    // 恢复输入
    inputText.value = content
    // 移除临时消息
    messageList.value = messageList.value.filter(m => !m.id.startsWith('temp-'))
  } finally {
    sending.value = false
    inputRef.value?.focus()
  }
}

/** 选择表情 */
const handleSelectEmoji = (emoji: string) => {
  inputText.value += emoji
  showEmojiPicker.value = false
  inputRef.value?.focus()
}

/** 发送图片 */
const handleSendImage = () => {
  ElMessage.info('图片上传功能开发中')
}

/** 发送文件 */
const handleSendFile = () => {
  ElMessage.info('文件发送功能开发中')
}

/** 搜索会话 */
const handleSearchConversation = () => {
  // 由计算属性处理
}

/** 搜索消息 */
const handleSearchMessage = async () => {
  if (!searchMessageKeyword.value.trim()) return
  
  searched.value = true
  searchResults.value = messageList.value.filter(msg =>
    msg.content.toLowerCase().includes(searchMessageKeyword.value.toLowerCase())
  )
}

/** 跳转到消息 */
const handleJumpToMessage = (msg: MessageVO) => {
  showSearchDialog.value = false
  // 滚动到对应消息
  const index = messageList.value.findIndex(m => m.id === msg.id)
  if (index > -1) {
    scrollToIndex(index)
  }
}

/** 切换会话 */
const handleSwitchConversation = (conv: ConversationVO) => {
  router.push(`/chat/${conv.id}`)
}

/** 置顶会话 */
const handlePinConversation = async () => {
  if (!currentConversation.value) return
  currentConversation.value.pinned = !currentConversation.value.pinned
  ElMessage.success(currentConversation.value.pinned ? '已置顶' : '已取消置顶')
}

/** 滚动到底部 */
const scrollToBottom = () => {
  if (scrollbarRef.value) {
    scrollbarRef.value.setScrollTop(9999999)
  }
}

/** 滚动到指定索引 */
const scrollToIndex = (index: number) => {
  // 简单实现：滚动到底部
  scrollToBottom()
}

/** 处理滚动 */
const handleScroll = ({ scrollTop }: { scrollTop: number }) => {
  // 如果滚动到顶部，加载更多
  if (scrollTop < 100 && hasMore.value && !loadingMore.value) {
    loadMoreMessages()
  }
}

/** 判断是否显示时间分隔线 */
const shouldShowTimeDivider = (currentTime: string, prevTime?: string): boolean => {
  if (!prevTime) return false
  const current = new Date(currentTime).getTime()
  const prev = new Date(prevTime).getTime()
  // 超过5分钟显示时间
  return current - prev > 5 * 60 * 1000
}

/** 格式化日期分割线 */
const formatDateDivider = (time?: string): string => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  
  if (date.toDateString() === now.toDateString()) {
    return '今天'
  }
  
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }
  
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

/** 获取名字首字母 */
const getNameInitial = (name: string): string => {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}

/** 启动轮询 */
const startPolling = () => {
  pollingTimer = setInterval(async () => {
    if (currentConversationId.value) {
      try {
        const res = await getMessageHistory({
          conversationId: currentConversationId.value,
          pageSize: 20
        })
        // 检查是否有新消息
        if (res.data.list.length > messageList.value.length) {
          const newMessages = res.data.list.slice(messageList.value.length)
          messageList.value.push(...newMessages)
          nextTick(() => scrollToBottom())
        }
      } catch (error) {
        // 忽略轮询错误
      }
    }
  }, 10000) // 每10秒轮询
}

/** 停止轮询 */
const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}
</script>

<style lang="scss" scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 120px);
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

// ============ 侧边栏 ============

.sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  
  &.collapsed {
    width: 48px;
  }
  
  .sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #e8e8e8;
    
    .sidebar-title {
      display: flex;
      align-items: center;
      gap: 8px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }
      
      .el-icon {
        font-size: 18px;
        color: #667eea;
      }
    }
    
    .collapse-btn {
      font-size: 16px;
      color: #999;
      cursor: pointer;
      
      &:hover {
        color: #667eea;
      }
    }
  }
  
  .sidebar-search {
    padding: 12px 16px;
    border-bottom: 1px solid #e8e8e8;
  }
  
  .sidebar-list {
    flex: 1;
    padding: 8px;
    
    .sidebar-item {
      display: flex;
      align-items: center;
      padding: 10px 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: background-color 0.2s;
      margin-bottom: 4px;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      &.active {
        background-color: #ecf0ff;
      }
      
      .sidebar-item-content {
        flex: 1;
        min-width: 0;
        margin-left: 12px;
        
        .sidebar-item-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 4px;
          
          .name {
            font-size: 14px;
            font-weight: 500;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .time {
            font-size: 11px;
            color: #999;
          }
        }
        
        .sidebar-item-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .last-msg {
            font-size: 12px;
            color: #999;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex: 1;
          }
          
          .unread-badge {
            flex-shrink: 0;
            margin-left: 8px;
            
            :deep(.el-badge__content) {
              background-color: #F56C6C;
            }
          }
        }
      }
    }
  }
}

// ============ 聊天主区域 ============

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  
  .chat-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .chat-info-text {
      display: flex;
      flex-direction: column;
      
      .chat-name {
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
      
      .chat-members {
        font-size: 12px;
        color: #999;
        margin-top: 2px;
      }
    }
  }
  
  .chat-actions {
    .el-button {
      color: #666;
      
      &:hover {
        color: #667eea;
      }
    }
  }
}

// ============ 消息区域 ============

.message-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  
  .load-more {
    text-align: center;
    padding: 12px;
    
    .load-more-btn {
      color: #667eea;
      cursor: pointer;
      font-size: 13px;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
  
  .time-divider {
    text-align: center;
    margin: 20px 0;
    
    span {
      display: inline-block;
      padding: 4px 12px;
      background: rgba(0, 0, 0, 0.05);
      border-radius: 4px;
      font-size: 12px;
      color: #999;
    }
  }
  
  .message-wrapper {
    margin-bottom: 16px;
    
    &.message-self {
      .message-content-wrapper {
        flex-direction: row-reverse;
        
        .message-bubble {
          align-items: flex-end;
          
          .message-text {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
            border-radius: 18px 18px 4px 18px;
            
            &.text-self {
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            }
          }
          
          .message-sender {
            text-align: right;
          }
          
          .message-time {
            text-align: right;
            margin-right: 8px;
          }
        }
      }
    }
    
    .time-divider-inline {
      text-align: center;
      margin: 12px 0;
      font-size: 12px;
      color: #999;
    }
    
    .message-content-wrapper {
      display: flex;
      align-items: flex-start;
      
      .message-avatar {
        flex-shrink: 0;
        margin-right: 10px;
      }
      
      .message-bubble {
        display: flex;
        flex-direction: column;
        max-width: 70%;
        
        .message-sender {
          font-size: 12px;
          color: #999;
          margin-bottom: 4px;
        }
        
        .message-text {
          display: inline-block;
          padding: 10px 14px;
          background: #fff;
          border-radius: 18px 18px 18px 4px;
          font-size: 14px;
          line-height: 1.5;
          color: #333;
          word-break: break-word;
          
          &.text-self {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
            border-radius: 18px 18px 4px 18px;
          }
        }
        
        .message-image {
          max-width: 300px;
          max-height: 300px;
          border-radius: 8px;
        }
        
        .message-file {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 12px;
          background: rgba(0, 0, 0, 0.05);
          border-radius: 8px;
          
          .el-icon {
            font-size: 20px;
          }
        }
        
        .message-system {
          font-size: 12px;
          color: #999;
          text-align: center;
          padding: 4px 12px;
        }
        
        .message-time {
          font-size: 11px;
          color: #ccc;
          margin-top: 4px;
          margin-left: 8px;
        }
      }
    }
  }
}

// ============ 输入区域 ============

.input-area {
  background: #fff;
  border-top: 1px solid #e8e8e8;
  
  .input-toolbar {
    display: flex;
    gap: 4px;
    padding: 8px 20px;
    border-bottom: 1px solid #f0f0f0;
    
    .el-button {
      color: #666;
      
      &:hover {
        color: #667eea;
        background: #ecf0ff;
      }
    }
  }
  
  .emoji-picker {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    padding: 12px 20px;
    background: #fafafa;
    border-bottom: 1px solid #f0f0f0;
    
    .emoji-item {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      cursor: pointer;
      border-radius: 4px;
      
      &:hover {
        background: #e8e8e8;
      }
    }
  }
  
  .input-box {
    padding: 12px 20px;
    
    :deep(.el-textarea__inner) {
      border: none;
      padding: 8px 0;
      font-size: 14px;
      line-height: 1.6;
      
      &:focus {
        box-shadow: none;
      }
    }
  }
  
  .input-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 20px 16px;
    
    .input-hint {
      font-size: 12px;
      color: #999;
      display: flex;
      align-items: center;
      gap: 4px;
      
      .el-icon {
        font-size: 14px;
      }
    }
    
    .el-button--primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      
      &:hover {
        opacity: 0.9;
      }
      
      &:disabled {
        background: #ccc;
        opacity: 1;
      }
    }
  }
}

// ============ 会话详情抽屉 ============

.conversation-info {
  .info-header {
    text-align: center;
    padding: 20px;
    
    h3 {
      margin: 16px 0 8px;
      font-size: 18px;
    }
  }
  
  .info-section {
    padding: 0 20px;
    margin-bottom: 20px;
    
    h4 {
      margin: 0 0 12px;
      font-size: 14px;
      color: #666;
    }
    
    .member-list {
      .member-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 8px 0;
        
        .member-name {
          flex: 1;
          font-size: 14px;
        }
      }
    }
    
    .el-button {
      width: 100%;
      margin-bottom: 8px;
    }
  }
}

// ============ 搜索结果 ============

.search-results {
  margin-top: 16px;
  
  .search-result-item {
    display: flex;
    align-items: flex-start;
    padding: 12px;
    cursor: pointer;
    border-radius: 8px;
    
    &:hover {
      background: #f5f7fa;
    }
    
    .result-content {
      flex: 1;
      margin-left: 12px;
      
      .result-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 4px;
        
        .sender-name {
          font-size: 13px;
          font-weight: 500;
          color: #333;
        }
        
        .result-time {
          font-size: 12px;
          color: #999;
        }
      }
      
      .result-text {
        font-size: 13px;
        color: #666;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
    }
  }
}
</style>
