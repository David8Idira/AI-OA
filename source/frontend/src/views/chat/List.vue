<template>
  <div class="chat-list-page">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <div class="header-left">
        <h2>企业聊天</h2>
        <el-badge :value="totalUnread" :hidden="totalUnread === 0" class="unread-badge">
          <el-icon><Message /></el-icon>
        </el-badge>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索会话"
          :prefix-icon="Search"
          clearable
          class="search-input"
          @input="handleSearch"
        />
        <el-button type="primary" @click="handleNewChat">
          <el-icon><Plus /></el-icon>
          新建会话
        </el-button>
      </div>
    </div>

    <!-- 筛选Tabs -->
    <el-tabs v-model="activeTab" class="chat-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="单聊" name="SINGLE" />
      <el-tab-pane label="群聊" name="GROUP" />
      <el-tab-pane label="机器人" name="BOT" />
    </el-tabs>

    <!-- 会话列表 -->
    <div class="conversation-list" v-loading="loading">
      <!-- 空状态 -->
      <el-empty v-if="!loading && filteredList.length === 0" description="暂无会话记录">
        <el-button type="primary" @click="handleNewChat">发起新会话</el-button>
      </el-empty>

      <!-- 会话卡片 -->
      <div v-else>
        <!-- 置顶会话 -->
        <div v-if="pinnedList.length > 0" class="section-title">
          <el-icon><Star /></el-icon>
          <span>置顶</span>
        </div>
        <div
          v-for="item in pinnedList"
          :key="item.id"
          class="conversation-item pinned"
          @click="handleEnterChat(item)"
        >
          <div class="avatar-wrapper">
            <el-avatar :size="52" :src="item.avatar">
              {{ getNameInitial(item.name) }}
            </el-avatar>
            <span v-if="item.type === 'GROUP'" class="avatar-badge">
              <el-icon><ChatDotRound /></el-icon>
            </span>
          </div>
          <div class="conversation-content">
            <div class="conversation-header">
              <span class="conversation-name">{{ item.name }}</span>
              <span class="conversation-time">
                {{ getRelativeTime(item.lastMessageTime) }}
              </span>
            </div>
            <div class="conversation-footer">
              <span class="last-message" v-if="item.draft">
                <span class="draft-prefix">草稿：</span>{{ item.draft }}
              </span>
              <span class="last-message" v-else>{{ item.lastMessage }}</span>
              <div class="conversation-meta">
                <span v-if="item.muted" class="muted-icon">
                  <el-icon><CloseBold /></el-icon>
                </span>
                <el-badge
                  v-if="item.unreadCount > 0"
                  :value="item.unreadCount > 99 ? '99+' : item.unreadCount"
                  :max="99"
                  class="unread-badge-item"
                />
              </div>
            </div>
          </div>
          <div class="conversation-actions">
            <el-dropdown trigger="click" @command="(cmd: string) => handleAction(cmd, item)">
              <el-icon class="action-btn"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="pin">
                    <el-icon><Star /></el-icon>
                    {{ item.pinned ? '取消置顶' : '置顶' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="mute">
                    <el-icon><CloseBold /></el-icon>
                    {{ item.muted ? '取消免打扰' : '免打扰' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除会话
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 普通会话 -->
        <div v-if="normalList.length > 0" class="section-title">
          <span>会话</span>
        </div>
        <div
          v-for="item in normalList"
          :key="item.id"
          class="conversation-item"
          @click="handleEnterChat(item)"
        >
          <div class="avatar-wrapper">
            <el-avatar :size="52" :src="item.avatar">
              {{ getNameInitial(item.name) }}
            </el-avatar>
            <span v-if="item.type === 'GROUP'" class="avatar-badge">
              <el-icon><ChatDotRound /></el-icon>
            </span>
          </div>
          <div class="conversation-content">
            <div class="conversation-header">
              <span class="conversation-name">{{ item.name }}</span>
              <span class="conversation-time">
                {{ getRelativeTime(item.lastMessageTime) }}
              </span>
            </div>
            <div class="conversation-footer">
              <span class="last-message" v-if="item.draft">
                <span class="draft-prefix">草稿：</span>{{ item.draft }}
              </span>
              <span class="last-message" v-else>{{ item.lastMessage }}</span>
              <div class="conversation-meta">
                <span v-if="item.muted" class="muted-icon">
                  <el-icon><CloseBold /></el-icon>
                </span>
                <el-badge
                  v-if="item.unreadCount > 0"
                  :value="item.unreadCount > 99 ? '99+' : item.unreadCount"
                  :max="99"
                  class="unread-badge-item"
                />
              </div>
            </div>
          </div>
          <div class="conversation-actions">
            <el-dropdown trigger="click" @command="(cmd: string) => handleAction(cmd, item)">
              <el-icon class="action-btn"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="pin">
                    <el-icon><Star /></el-icon>
                    {{ item.pinned ? '取消置顶' : '置顶' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="mute">
                    <el-icon><CloseBold /></el-icon>
                    {{ item.muted ? '取消免打扰' : '免打扰' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>
                    删除会话
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建会话对话框 -->
    <el-dialog
      v-model="newChatVisible"
      title="发起新会话"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="newChatForm" class="new-chat-form">
        <el-form-item label="会话类型">
          <el-radio-group v-model="newChatForm.type" @change="handleTypeChange">
            <el-radio label="SINGLE">单聊</el-radio>
            <el-radio label="GROUP">群聊</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="会话名称" v-if="newChatForm.type === 'GROUP'">
          <el-input
            v-model="newChatForm.name"
            placeholder="请输入群聊名称"
            maxlength="30"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item :label="newChatForm.type === 'GROUP' ? '选择成员' : '选择联系人'">
          <div class="member-select">
            <div class="selected-members" v-if="selectedMembers.length > 0">
              <el-tag
                v-for="member in selectedMembers"
                :key="member.id"
                closable
                @close="handleRemoveMember(member.id)"
                class="member-tag"
              >
                {{ member.name }}
              </el-tag>
            </div>
            <el-button type="primary" plain @click="handleSelectMember" class="select-btn">
              <el-icon><Plus /></el-icon>
              添加成员
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="newChatVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateChat" :loading="creating" :disabled="!canCreate">
            创建会话
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 选择成员对话框 -->
    <el-dialog
      v-model="selectMemberVisible"
      title="选择成员"
      width="600px"
    >
      <el-input
        v-model="memberSearchKeyword"
        placeholder="搜索成员"
        :prefix-icon="Search"
        clearable
        class="member-search"
      />
      
      <el-scrollbar height="400px" class="member-list">
        <div
          v-for="member in filteredMembers"
          :key="member.id"
          class="member-item"
          :class="{ selected: isMemberSelected(member.id) }"
          @click="handleToggleMember(member)"
        >
          <el-avatar :size="40" :src="member.avatar">
            {{ getNameInitial(member.name) }}
          </el-avatar>
          <div class="member-info">
            <span class="member-name">{{ member.name }}</span>
            <span class="member-dept">{{ member.dept || '技术部' }}</span>
          </div>
          <el-icon v-if="isMemberSelected(member.id)" class="check-icon"><Check /></el-icon>
        </div>
      </el-scrollbar>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="selectMemberVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmMember">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  MoreFilled,
  Star,
  Delete,
  CloseBold,
  ChatDotRound,
  Check,
  Message
} from '@element-plus/icons-vue'
import {
  getConversationList,
  createConversation,
  generateMockConversations,
  getRelativeTime,
  truncateMessage,
  type ConversationVO,
  type ConversationType
} from '@/api/chat'

const router = useRouter()

// ============ 状态定义 ============

const loading = ref(false)
const activeTab = ref<'all' | 'SINGLE' | 'GROUP' | 'BOT'>('all')
const searchKeyword = ref('')
const list = ref<ConversationVO[]>([])

// 新建会话
const newChatVisible = ref(false)
const creating = ref(false)
const newChatForm = reactive({
  type: 'SINGLE' as ConversationType,
  name: ''
})
const selectedMembers = ref<Array<{ id: string; name: string; avatar?: string }>>([])

// 选择成员
const selectMemberVisible = ref(false)
const memberSearchKeyword = ref('')
const allMembers = ref([
  { id: 'user-1', name: '张伟', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=张伟', dept: '产品部' },
  { id: 'user-2', name: '李娜', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=李娜', dept: '设计部' },
  { id: 'user-3', name: '王强', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=王强', dept: '技术部' },
  { id: 'user-4', name: '刘芳', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=刘芳', dept: '市场部' },
  { id: 'user-5', name: '赵敏', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=赵敏', dept: '人事部' },
  { id: 'user-6', name: '钱龙', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=钱龙', dept: '财务部' },
  { id: 'user-7', name: '孙琪', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=孙琪', dept: '运营部' },
  { id: 'user-8', name: '周杰', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=周杰', dept: '技术部' }
])
const tempSelectedIds = ref<string[]>([])

// ============ 计算属性 ============

const totalUnread = computed(() => {
  return list.value.reduce((sum, item) => sum + item.unreadCount, 0)
})

const filteredList = computed(() => {
  let result = list.value
  
  // Tab筛选
  if (activeTab.value !== 'all') {
    result = result.filter(item => item.type === activeTab.value)
  }
  
  // 搜索筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item =>
      item.name.toLowerCase().includes(keyword) ||
      (item.lastMessage && item.lastMessage.toLowerCase().includes(keyword))
    )
  }
  
  return result
})

const pinnedList = computed(() => {
  return filteredList.value.filter(item => item.pinned)
})

const normalList = computed(() => {
  return filteredList.value.filter(item => !item.pinned)
})

const filteredMembers = computed(() => {
  if (!memberSearchKeyword.value) return allMembers.value
  const keyword = memberSearchKeyword.value.toLowerCase()
  return allMembers.value.filter(m =>
    m.name.toLowerCase().includes(keyword) ||
    (m.dept && m.dept.toLowerCase().includes(keyword))
  )
})

const canCreate = computed(() => {
  if (newChatForm.type === 'GROUP' && !newChatForm.name.trim()) return false
  if (selectedMembers.value.length === 0) return false
  return true
})

// ============ 生命周期 ============

onMounted(() => {
  loadList()
})

// ============ 方法 ============

/** 加载会话列表 */
const loadList = async () => {
  loading.value = true
  try {
    const res = await getConversationList()
    list.value = res.data
  } catch (error) {
    console.error('加载会话列表失败', error)
    // 使用模拟数据
    list.value = generateMockConversations()
  } finally {
    loading.value = false
  }
}

/** Tab切换 */
const handleTabChange = () => {
  // 无需特殊处理，计算属性会自动过滤
}

/** 搜索 */
const handleSearch = () => {
  // 搜索由计算属性处理
}

/** 进入聊天 */
const handleEnterChat = (item: ConversationVO) => {
  router.push(`/chat/${item.id}`)
}

/** 新建会话 */
const handleNewChat = () => {
  newChatForm.type = 'SINGLE'
  newChatForm.name = ''
  selectedMembers.value = []
  tempSelectedIds.value = []
  newChatVisible.value = true
}

/** 会话类型变化 */
const handleTypeChange = () => {
  selectedMembers.value = []
  tempSelectedIds.value = []
}

/** 移除已选成员 */
const handleRemoveMember = (id: string) => {
  selectedMembers.value = selectedMembers.value.filter(m => m.id !== id)
  tempSelectedIds.value = tempSelectedIds.value.filter(i => i !== id)
}

/** 选择成员 */
const handleSelectMember = () => {
  tempSelectedIds.value = [...selectedMembers.value.map(m => m.id)]
  memberSearchKeyword.value = ''
  selectMemberVisible.value = true
}

/** 切换成员选中状态 */
const handleToggleMember = (member: typeof allMembers.value[0]) => {
  const index = tempSelectedIds.value.indexOf(member.id)
  if (index > -1) {
    tempSelectedIds.value.splice(index, 1)
  } else {
    // 单聊只能选一个
    if (newChatForm.type === 'SINGLE') {
      tempSelectedIds.value = [member.id]
    } else {
      tempSelectedIds.value.push(member.id)
    }
  }
}

/** 判断成员是否已选 */
const isMemberSelected = (id: string) => {
  return tempSelectedIds.value.includes(id)
}

/** 确认选择成员 */
const handleConfirmMember = () => {
  selectedMembers.value = allMembers.value.filter(m => tempSelectedIds.value.includes(m.id))
  selectMemberVisible.value = false
}

/** 创建会话 */
const handleCreateChat = async () => {
  if (!canCreate.value) return
  
  creating.value = true
  try {
    const data = {
      type: newChatForm.type,
      name: newChatForm.type === 'GROUP' ? newChatForm.name : undefined,
      memberIds: selectedMembers.value.map(m => m.id)
    }
    
    const res = await createConversation(data)
    ElMessage.success('会话创建成功')
    newChatVisible.value = false
    router.push(`/chat/${res.data.id}`)
  } catch (error) {
    console.error('创建会话失败', error)
    ElMessage.error('创建会话失败')
  } finally {
    creating.value = false
  }
}

/** 操作处理 */
const handleAction = async (command: string, item: ConversationVO) => {
  switch (command) {
    case 'pin':
      item.pinned = !item.pinned
      ElMessage.success(item.pinned ? '已置顶' : '已取消置顶')
      break
    case 'mute':
      item.muted = !item.muted
      ElMessage.success(item.muted ? '已开启免打扰' : '已关闭免打扰')
      break
    case 'delete':
      try {
        await ElMessageBox.confirm('确定要删除该会话吗？删除后聊天记录将无法恢复。', '提示', {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning'
        })
        list.value = list.value.filter(i => i.id !== item.id)
        ElMessage.success('会话已删除')
      } catch {
        // 用户取消
      }
      break
  }
}

/** 获取名字首字母 */
const getNameInitial = (name: string): string => {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}
</script>

<style lang="scss" scoped>
.chat-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
      
      h2 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
      }
      
      .unread-badge {
        :deep(.el-badge__content) {
          background-color: #F56C6C;
        }
      }
      
      .el-icon {
        font-size: 24px;
        color: #667eea;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .search-input {
        width: 240px;
      }
    }
  }
  
  .chat-tabs {
    margin-bottom: 16px;
  }
  
  .conversation-list {
    background: #fff;
    border-radius: 8px;
    padding: 8px 0;
    min-height: 500px;
    
    .section-title {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 12px 16px 8px;
      font-size: 12px;
      color: #999;
      
      .el-icon {
        font-size: 14px;
      }
    }
    
    .conversation-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      cursor: pointer;
      transition: background-color 0.2s;
      position: relative;
      
      &:hover {
        background-color: #f5f7fa;
        
        .conversation-actions {
          opacity: 1;
        }
      }
      
      &.pinned {
        background-color: #fafafa;
      }
      
      .avatar-wrapper {
        position: relative;
        flex-shrink: 0;
        margin-right: 12px;
        
        .avatar-badge {
          position: absolute;
          bottom: -2px;
          right: -2px;
          width: 18px;
          height: 18px;
          background: #667eea;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          
          .el-icon {
            font-size: 10px;
            color: #fff;
          }
        }
      }
      
      .conversation-content {
        flex: 1;
        min-width: 0;
        overflow: hidden;
        
        .conversation-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 4px;
          
          .conversation-name {
            font-size: 15px;
            font-weight: 500;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .conversation-time {
            font-size: 12px;
            color: #999;
            flex-shrink: 0;
            margin-left: 8px;
          }
        }
        
        .conversation-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .last-message {
            font-size: 13px;
            color: #999;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex: 1;
            
            .draft-prefix {
              color: #E6A23C;
            }
          }
          
          .conversation-meta {
            display: flex;
            align-items: center;
            gap: 8px;
            flex-shrink: 0;
            
            .muted-icon {
              .el-icon {
                font-size: 14px;
                color: #c0c4cc;
              }
            }
            
            .unread-badge-item {
              :deep(.el-badge__content) {
                background-color: #F56C6C;
              }
            }
          }
        }
      }
      
      .conversation-actions {
        position: absolute;
        right: 16px;
        opacity: 0;
        transition: opacity 0.2s;
        
        .action-btn {
          padding: 8px;
          font-size: 16px;
          color: #666;
          cursor: pointer;
          
          &:hover {
            color: #667eea;
          }
        }
      }
    }
  }
  
  .new-chat-form {
    .member-select {
      width: 100%;
      
      .selected-members {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 12px;
        min-height: 32px;
        
        .member-tag {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border: none;
          color: #fff;
        }
      }
      
      .select-btn {
        width: 100%;
      }
    }
  }
  
  .member-search {
    margin-bottom: 16px;
  }
  
  .member-list {
    .member-item {
      display: flex;
      align-items: center;
      padding: 12px;
      cursor: pointer;
      border-radius: 8px;
      transition: background-color 0.2s;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      &.selected {
        background-color: #ecf5ff;
      }
      
      .member-info {
        flex: 1;
        margin-left: 12px;
        display: flex;
        flex-direction: column;
        
        .member-name {
          font-size: 14px;
          color: #333;
          font-weight: 500;
        }
        
        .member-dept {
          font-size: 12px;
          color: #999;
          margin-top: 2px;
        }
      }
      
      .check-icon {
        font-size: 18px;
        color: #667eea;
      }
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
