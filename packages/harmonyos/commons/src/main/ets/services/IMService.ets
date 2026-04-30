import ApiClient, { ApiResponse } from '../data/api/ApiClient'
import { Message, MessageType, CreateMessageParams } from '../data/model/Message'

/**
 * 角色模型
 */

// 会话类型
export enum ConversationType {
  PRIVATE = 'private',       // 单聊
  GROUP = 'group',          // 群聊
  SYSTEM = 'system'         // 系统消息
}

// 会话模型
export interface Conversation {
  id: string
  type: ConversationType
  name?: string
  avatar?: string
  lastMessage?: Message
  lastMessageTime?: number
  unreadCount: number
  isPinned: boolean
  isMuted: boolean
  isArchived: boolean
  memberCount?: number
  members?: { id: string; name: string; avatar?: string }[]
  createdAt: number
  updatedAt: number
}

// 联系人模型
export interface Contact {
  id: string
  name: string
  avatar?: string
  email?: string
  phone?: string
  department?: string
  position?: string
  status?: 'online' | 'offline' | 'busy'
}

// 群成员
export interface GroupMember {
  id: string
  name: string
  avatar?: string
  role: 'owner' | 'admin' | 'member'
  joinedAt: number
}

// 消息已读状态
export interface ReadStatus {
  messageId: string
  readBy: { userId: string; userName: string; readAt: number }[]
}

// 草稿
export interface Draft {
  conversationId: string
  content: string
  updatedAt: number
}

/**
 * IM服务类
 * 封装即时通讯模块所有API调用
 */
export class IMService {
  private client: ApiClient

  constructor() {
    this.client = ApiClient
  }

  // ============ 会话管理 ============

  /**
   * 获取会话列表
   * @param params 查询参数
   */
  async getConversations(params?: {
    type?: ConversationType
    isPinned?: boolean
    isMuted?: boolean
    page?: number
    pageSize?: number
  }): Promise<ApiResponse<{ conversations: Conversation[]; total: number }>> {
    return this.client.get<{ conversations: Conversation[]; total: number }>('/api/v1/im/conversations', params as Record<string, any>)
  }

  /**
   * 获取会话详情
   * @param conversationId 会话ID
   */
  async getConversation(conversationId: string): Promise<ApiResponse<Conversation>> {
    return this.client.get<Conversation>(`/api/v1/im/conversations/${conversationId}`)
  }

  /**
   * 创建群聊
   * @param data 创建参数
   */
  async createGroup(data: {
    name: string
    avatar?: string
    memberIds: string[]
    description?: string
  }): Promise<ApiResponse<Conversation>> {
    return this.client.post<Conversation>('/api/v1/im/conversations/group', data)
  }

  /**
   * 更新群信息
   * @param conversationId 会话ID
   * @param data 更新数据
   */
  async updateGroup(conversationId: string, data: {
    name?: string
    avatar?: string
    description?: string
  }): Promise<ApiResponse<Conversation>> {
    return this.client.put<Conversation>(`/api/v1/im/conversations/${conversationId}`, data)
  }

  /**
   * 解散群聊
   * @param conversationId 会话ID
   */
  async dismissGroup(conversationId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/im/conversations/${conversationId}`)
  }

  /**
   * 退出群聊
   * @param conversationId 会话ID
   */
  async leaveGroup(conversationId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/im/conversations/${conversationId}/leave`, {})
  }

  /**
   * 置顶会话
   * @param conversationId 会话ID
   */
  async pinConversation(conversationId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/im/conversations/${conversationId}/pin`, {})
  }

  /**
   * 取消置顶会话
   * @param conversationId 会话ID
   */
  async unpinConversation(conversationId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/im/conversations/${conversationId}/pin`)
  }

  /**
   * 静音会话
   * @param conversationId 会话ID
   */
  async muteConversation(conversationId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/im/conversations/${conversationId}/mute`, {})
  }

  /**
   * 取消静音会话
   * @param conversationId 会话ID
   */
  async unmuteConversation(conversationId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/im/conversations/${conversationId}/mute`)
  }

  /**
   * 归档会话
   * @param conversationId 会话ID
   */
  async archiveConversation(conversationId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/im/conversations/${conversationId}/archive`, {})
  }

  /**
   * 标记会话已读
   * @param conversationId 会话ID
   * @param messageId 消息ID（传入则标记到该消息）
   */
  async markRead(conversationId: string, messageId?: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/im/conversations/${conversationId}/read`, messageId ? { messageId } : {})
  }

  // ============ 消息管理 ============

  /**
   * 获取消息列表
   * @param conversationId 会话ID
   * @param params 查询参数
   */
  async getMessages(conversationId: string, params?: {
    before?: number
    after?: number
    limit?: number
  }): Promise<ApiResponse<{ messages: Message[]; hasMore: boolean }>> {
    return this.client.get<{ messages: Message[]; hasMore: boolean }>(`/api/v1/im/conversations/${conversationId}/messages`, params as Record<string, any>)
  }

  /**
   * 发送消息
   * @param data 消息数据
   */
  async sendMessage(data: CreateMessageParams): Promise<ApiResponse<Message>> {
    return this.client.post<Message>('/api/v1/im/messages', data)
  }

  /**
   * 发送文本消息
   * @param conversationId 会话ID
   * @param content 文本内容
   */
  async sendTextMessage(conversationId: string, content: string): Promise<ApiResponse<Message>> {
    return this.sendMessage({
      conversationId,
      type: MessageType.TEXT,
      content
    })
  }

  /**
   * 发送图片消息
   * @param conversationId 会话ID
   * @param imageUrl 图片URL
   * @param content 可选的图片描述
   */
  async sendImageMessage(conversationId: string, imageUrl: string, content?: string): Promise<ApiResponse<Message>> {
    return this.sendMessage({
      conversationId,
      type: MessageType.IMAGE,
      content: content || '',
      imageUrl
    })
  }

  /**
   * 撤回消息
   * @param messageId 消息ID
   */
  async recallMessage(messageId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/im/messages/${messageId}`)
  }

  /**
   * 删除消息（删除本地和服务器）
   * @param messageId 消息ID
   */
  async deleteMessage(messageId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/im/messages/${messageId}`)
  }

  /**
   * 获取消息已读状态
   * @param messageId 消息ID
   */
  async getReadStatus(messageId: string): Promise<ApiResponse<ReadStatus>> {
    return this.client.get<ReadStatus>(`/api/v1/im/messages/${messageId}/read`)
  }

  // ============ 群成员管理 ============

  /**
   * 获取群成员列表
   * @param conversationId 会话ID
   */
  async getGroupMembers(conversationId: string): Promise<ApiResponse<{ members: GroupMember[] }>> {
    return this.client.get<{ members: GroupMember[] }>(`/api/v1/im/conversations/${conversationId}/members`)
  }

  /**
   * 添加群成员
   * @param conversationId 会话ID
   * @param memberIds 成员ID列表
   */
  async addGroupMembers(conversationId: string, memberIds: string[]): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/im/conversations/${conversationId}/members`, { memberIds })
  }

  /**
   * 移除群成员
   * @param conversationId 会话ID
   * @param memberId 成员ID
   */
  async removeGroupMember(conversationId: string, memberId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/im/conversations/${conversationId}/members/${memberId}`)
  }

  /**
   * 设置群管理员
   * @param conversationId 会话ID
   * @param memberId 成员ID
   */
  async setGroupAdmin(conversationId: string, memberId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/im/conversations/${conversationId}/admins/${memberId}`, {})
  }

  /**
   * 取消群管理员
   * @param conversationId 会话ID
   * @param memberId 成员ID
   */
  async removeGroupAdmin(conversationId: string, memberId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/im/conversations/${conversationId}/admins/${memberId}`)
  }

  /**
   * 转让群主
   * @param conversationId 会话ID
   * @param newOwnerId 新群主ID
   */
  async transferGroupOwner(conversationId: string, newOwnerId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/im/conversations/${conversationId}/owner`, { newOwnerId })
  }

  // ============ 联系人 ============

  /**
   * 获取联系人列表
   */
  async getContacts(params?: { keyword?: string; department?: string; page?: number; pageSize?: number }): Promise<ApiResponse<{ contacts: Contact[]; total: number }>> {
    return this.client.get<{ contacts: Contact[]; total: number }>('/api/v1/im/contacts', params as Record<string, any>)
  }

  /**
   * 获取联系人详情
   * @param contactId 联系人ID
   */
  async getContact(contactId: string): Promise<ApiResponse<Contact>> {
    return this.client.get<Contact>(`/api/v1/im/contacts/${contactId}`)
  }

  /**
   * 搜索联系人
   * @param keyword 关键词
   */
  async searchContacts(keyword: string): Promise<ApiResponse<Contact[]>> {
    return this.client.get<Contact[]>('/api/v1/im/contacts/search', { keyword })
  }

  /**
   * 获取部门联系人列表
   * @param departmentId 部门ID
   */
  async getDepartmentContacts(departmentId?: string): Promise<ApiResponse<{ contacts: Contact[] }>> {
    return this.client.get<{ contacts: Contact[] }>('/api/v1/im/contacts/department', departmentId ? { departmentId } : undefined)
  }

  // ============ 草稿与收藏 ============

  /**
   * 保存草稿
   * @param conversationId 会话ID
   * @param content 草稿内容
   */
  async saveDraft(conversationId: string, content: string): Promise<ApiResponse<any>> {
    return this.client.post<any>('/api/v1/im/drafts', { conversationId, content })
  }

  /**
   * 获取草稿列表
   */
  async getDrafts(): Promise<ApiResponse<{ drafts: Draft[] }>> {
    return this.client.get<{ drafts: Draft[] }>('/api/v1/im/drafts')
  }

  /**
   * 删除草稿
   * @param conversationId 会话ID
   */
  async deleteDraft(conversationId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/im/drafts/${conversationId}`)
  }

  // ============ 消息提醒 ============

  /**
   * 获取未读消息数
   */
  async getTotalUnreadCount(): Promise<ApiResponse<{ count: number }>> {
    return this.client.get<{ count: number }>('/api/v1/im/unread-count')
  }

  /**
   * 全部标记已读
   */
  async markAllRead(): Promise<ApiResponse<any>> {
    return this.client.post<any>('/api/v1/im/read-all', {})
  }
}

// 导出服务实例
export default new IMService()
