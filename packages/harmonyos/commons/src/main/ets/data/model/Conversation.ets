// 会话模型

// 会话类型
export enum ConversationType {
  PRIVATE = 'private',     // 单聊
  GROUP = 'group',         // 群聊
  SYSTEM = 'system',       // 系统通知
  ORGANIZATION = 'organization' // 组织通知
}

// 会话置顶状态
export enum PinStatus {
  NONE = 0,
  PINNED = 1
}

// 会话模型
export interface Conversation {
  id: string
  name: string
  avatar?: string
  type: ConversationType
  lastMessage?: string
  lastMessageTime: number
  unreadCount: number
  isPinned: PinStatus
  members?: ConversationMember[]
  isMuted: boolean // 免打扰
}

// 会话成员
export interface ConversationMember {
  userId: string
  name: string
  avatar?: string
  role?: 'owner' | 'admin' | 'member'
}

// 会话列表响应
export interface ConversationListResponse {
  conversations: Conversation[]
  total: number
  page: number
  pageSize: number
}

// 创建群聊会话请求
export interface CreateGroupConversationParams {
  name: string
  avatar?: string
  memberIds: string[]
}

// 会话草稿管理
export interface ConversationDraft {
  conversationId: string
  draft: string
  updatedAt: number
}
