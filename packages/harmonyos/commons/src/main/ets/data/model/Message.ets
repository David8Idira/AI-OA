// 消息模型

// 消息类型枚举
export enum MessageType {
  TEXT = 'text',
  IMAGE = 'image',
  VOICE = 'voice',
  FILE = 'file',
  VIDEO = 'video'
}

// 消息发送者类型
export enum SenderType {
  USER = 'user',
  SYSTEM = 'system',
  BOT = 'bot'
}

// 消息模型
export interface Message {
  id: string
  conversationId: string
  senderId: string
  senderName: string
  senderAvatar?: string
  senderType: SenderType
  type: MessageType
  content: string
  imageUrl?: string
  createdAt: number
  isRead: boolean
}

// 消息草稿
export interface MessageDraft {
  conversationId: string
  content: string
  updatedAt: number
}

// 消息未读数
export interface UnreadCount {
  conversationId: string
  count: number
}

// 创建消息请求
export interface CreateMessageParams {
  conversationId: string
  type: MessageType
  content: string
  imageUrl?: string
}

// 消息列表响应
export interface MessageListResponse {
  messages: Message[]
  total: number
  page: number
  pageSize: number
}

// 标记已读请求
export interface MarkReadParams {
  conversationId: string
  messageId?: string // 传入则只标记该消息之前，已读；不传则标记全部
}
