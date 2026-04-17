import request from '@/utils/request'

// ============ AI模型配置 ============

export interface AIModelVO {
  id: string
  name: string
  capability: 'TEXT' | 'IMAGE' | 'VIDEO' | 'AUDIO'
  provider: string
  endpoint?: string
  status: 'ENABLED' | 'DISABLED' | 'QUOTA_EXCEEDED'
  quota?: number
  usedQuota?: number
  parameters?: Record<string, any>
}

export interface ModelAssignmentVO {
  id: string
  functionModule: string
  capability: string
  modelId: string
  modelName: string
  priority: number
  failoverModelId?: string
}

export interface AIUsageStatVO {
  modelId: string
  modelName: string
  usageCount: number
  tokenCount: number
  cost: number
  statDate: string
}

// ============ AI对话 ============

export interface ChatMessageVO {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  model?: string
  createTime: string
  attachments?: Array<{ type: string; url: string }>
  references?: Array<{ title: string; url: string; source: string }>
}

export interface ChatRequestDTO {
  message: string
  model?: string
  imageUrls?: string[]
  context?: ChatMessageVO[]
}

// ============ API 方法 ============

/** 获取AI模型列表 */
export const getModelList = () => {
  return request.get<AIModelVO[]>('/ai/models')
}

/** 添加AI模型 */
export const addModel = (data: Partial<AIModelVO>) => {
  return request.post<string>('/ai/models', data)
}

/** 更新AI模型 */
export const updateModel = (id: string, data: Partial<AIModelVO>) => {
  return request.put(`/ai/models/${id}`, data)
}

/** 删除AI模型 */
export const deleteModel = (id: string) => {
  return request.delete(`/ai/models/${id}`)
}

/** 测试模型连接 */
export const testModel = (id: string) => {
  return request.post(`/ai/models/${id}/test`)
}

/** 获取功能模型分配 */
export const getAssignments = () => {
  return request.get<ModelAssignmentVO[]>('/ai/assignments')
}

/** 更新功能模型分配 */
export const updateAssignments = (data: ModelAssignmentVO[]) => {
  return request.put('/ai/assignments', data)
}

/** 获取使用量统计 */
export const getUsageStats = (params?: { startDate?: string; endDate?: string }) => {
  return request.get<AIUsageStatVO[]>('/ai/usage', { params })
}

/** AI对话 */
export const chat = (data: ChatRequestDTO) => {
  return request.post<ChatMessageVO>('/ai/chat', data)
}

/** 获取对话历史 */
export const getChatHistory = (sessionId?: string) => {
  return request.get<ChatMessageVO[]>('/ai/chat/history', { params: { sessionId } })
}
