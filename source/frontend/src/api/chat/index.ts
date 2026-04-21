import request from '@/utils/request'
import dayjs from 'dayjs'

// ============ 枚举和类型定义 ============

/** 消息类型 */
export type MessageType = 'TEXT' | 'IMAGE' | 'FILE' | 'AUDIO' | 'SYSTEM'

/** 会话类型 */
export type ConversationType = 'SINGLE' | 'GROUP' | 'BOT'

/** 消息对象 */
export interface MessageVO {
  id: string
  conversationId: string
  senderId: string
  senderName: string
  senderAvatar?: string
  content: string
  type: MessageType
  createTime: string
  isSelf: boolean
}

/** 会话对象 */
export interface ConversationVO {
  id: string
  name: string
  avatar?: string
  type: ConversationType
  lastMessage?: string
  lastMessageTime?: string
  unreadCount: number
  members?: MemberInfo[]
  pinned?: boolean
  muted?: boolean
  draft?: string
}

/** 成员信息 */
export interface MemberInfo {
  id: string
  name: string
  avatar?: string
  role?: 'OWNER' | 'ADMIN' | 'MEMBER'
}

/** 消息查询参数 */
export interface MessageQueryDTO {
  conversationId: string
  page?: number
  pageSize?: number
  before?: string
}

/** 发送消息参数 */
export interface SendMessageDTO {
  conversationId: string
  content: string
  type?: MessageType
}

/** API 响应封装 */
interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

// ============ API 函数 ============

/** 获取会话列表 */
export const getConversationList = async (): Promise<ApiResponse<ConversationVO[]>> => {
  return request.get('/im/conversations')
}

/** 获取会话详情 */
export const getConversationDetail = async (id: string): Promise<ApiResponse<ConversationVO>> => {
  return request.get(`/im/conversations/${id}`)
}

/** 获取消息历史 */
export const getMessageHistory = async (params: MessageQueryDTO): Promise<ApiResponse<{
  list: MessageVO[]
  hasMore: boolean
}>> => {
  return request.get('/im/messages', { params })
}

/** 发送消息 */
export const sendMessage = async (data: SendMessageDTO): Promise<ApiResponse<MessageVO>> => {
  return request.post('/im/messages', data)
}

/** 标记消息已读 */
export const markAsRead = async (conversationId: string): Promise<ApiResponse<void>> => {
  return request.put(`/im/conversations/${conversationId}/read`)
}

/** 创建会话 */
export const createConversation = async (data: {
  type: ConversationType
  name?: string
  memberIds: string[]
}): Promise<ApiResponse<ConversationVO>> => {
  return request.post('/im/conversations', data)
}

/** 获取会话成员 */
export const getConversationMembers = async (id: string): Promise<ApiResponse<MemberInfo[]>> => {
  return request.get(`/im/conversations/${id}/members`)
}

/** 搜索会话 */
export const searchConversations = async (keyword: string): Promise<ApiResponse<ConversationVO[]>> => {
  return request.get('/im/conversations/search', { params: { keyword } })
}

/** 搜索消息 */
export const searchMessages = async (params: {
  keyword: string
  conversationId?: string
}): Promise<ApiResponse<MessageVO[]>> => {
  return request.get('/im/messages/search', { params })
}

/** 格式化消息时间 */
export const formatMessageTime = (time: string): string => {
  const date = dayjs(time)
  const now = dayjs()
  
  if (date.isSame(now, 'day')) {
    return date.format('HH:mm')
  } else if (date.isSame(now.subtract(1, 'day'), 'day')) {
    return '昨天 ' + date.format('HH:mm')
  } else if (date.isSame(now, 'year')) {
    return date.format('MM-DD HH:mm')
  } else {
    return date.format('YYYY-MM-DD HH:mm')
  }
}

/** 获取相对时间 */
export const getRelativeTime = (time: string): string => {
  const date = dayjs(time)
  const now = dayjs()
  const diffMinutes = now.diff(date, 'minute')
  
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  if (diffMinutes < 1440) return `${Math.floor(diffMinutes / 60)}小时前`
  if (diffMinutes < 10080) return `${Math.floor(diffMinutes / 1440)}天前`
  return date.format('YYYY-MM-DD')
}

/** 截断消息文本 */
export const truncateMessage = (content: string, maxLength: number = 50): string => {
  if (content.length <= maxLength) return content
  return content.substring(0, maxLength) + '...'
}

/** 模拟数据 - 会话列表 */
export const generateMockConversations = (): ConversationVO[] => {
  const names = ['张伟', '产品研发群', '李娜', '王强', '技术交流组', '刘芳', '周杰', '行政通知群']
  const lastMessages = [
    '好的，我们下午开会讨论一下',
    '[图片]',
    '这个需求优先级比较高，需要尽快处理',
    '收到，我这边马上安排',
    '[文件] 项目计划书.pdf',
    '请问年假是怎么计算的？',
    '明天上午9点会议室B有预约吗',
    '各位同事，明天开始实行新的考勤制度'
  ]
  const types: ConversationType[] = ['SINGLE', 'GROUP', 'SINGLE', 'SINGLE', 'GROUP', 'SINGLE', 'SINGLE', 'GROUP']
  
  return names.map((name, i) => ({
    id: `conv-${i + 1}`,
    name,
    avatar: i === 1 ? '' : `https://api.dicebear.com/7.x/avataaars/svg?seed=${name}`,
    type: types[i],
    lastMessage: lastMessages[i],
    lastMessageTime: dayjs().subtract(i * 15 + Math.floor(Math.random() * 30), 'minute').toISOString(),
    unreadCount: i < 3 ? Math.floor(Math.random() * 5) + 1 : 0,
    pinned: i === 0,
    muted: i === 7
  }))
}

/** 模拟数据 - 消息列表 */
export const generateMockMessages = (conversationId: string): MessageVO[] => {
  const currentUserId = 'current-user'
  const otherUserId = 'other-user'
  const otherUserName = '张伟'
  
  const messages: MessageVO[] = [
    {
      id: 'msg-1',
      conversationId,
      senderId: otherUserId,
      senderName: otherUserName,
      content: '早上好！昨天的会议纪要你看了吗？',
      type: 'TEXT',
      createTime: dayjs().subtract(2, 'hour').toISOString(),
      isSelf: false
    },
    {
      id: 'msg-2',
      conversationId,
      senderId: currentUserId,
      senderName: '我',
      content: '看了，结论是本周五之前要完成v2.0的开发和测试。',
      type: 'TEXT',
      createTime: dayjs().subtract(1, 'hour').subtract(55, 'minute').toISOString(),
      isSelf: true
    },
    {
      id: 'msg-3',
      conversationId,
      senderId: otherUserId,
      senderName: otherUserName,
      content: '对的，另外有几个技术方案需要你确认一下',
      type: 'TEXT',
      createTime: dayjs().subtract(1, 'hour').subtract(50, 'minute').toISOString(),
      isSelf: false
    },
    {
      id: 'msg-4',
      conversationId,
      senderId: currentUserId,
      senderName: '我',
      content: '好的，我把代码review一下，晚点给你反馈',
      type: 'TEXT',
      createTime: dayjs().subtract(1, 'hour').subtract(45, 'minute').toISOString(),
      isSelf: true
    },
    {
      id: 'msg-5',
      conversationId,
      senderId: otherUserId,
      senderName: otherUserName,
      content: '好的，我们下午开会讨论一下',
      type: 'TEXT',
      createTime: dayjs().subtract(30, 'minute').toISOString(),
      isSelf: false
    },
    {
      id: 'msg-6',
      conversationId,
      senderId: currentUserId,
      senderName: '我',
      content: '下午几点？我下午有个客户电话会议',
      type: 'TEXT',
      createTime: dayjs().subtract(25, 'minute').toISOString(),
      isSelf: true
    },
    {
      id: 'msg-7',
      conversationId,
      senderId: otherUserId,
      senderName: otherUserName,
      content: '3点吧，应该不会太久，大概30分钟左右',
      type: 'TEXT',
      createTime: dayjs().subtract(20, 'minute').toISOString(),
      isSelf: false
    }
  ]
  
  return messages
}
