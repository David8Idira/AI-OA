import request from '@/utils/request'

/**
 * AI聊天接口
 */
export function chat(data: {
  message: string
  modelCode?: string
  conversationId?: string
  temperature?: number
  maxTokens?: number
}) {
  return request({
    url: '/api/ai/chat',
    method: 'POST',
    data
  })
}

/**
 * 获取可用模型列表
 */
export function getModels() {
  return request({
    url: '/api/ai/models',
    method: 'GET'
  })
}

/**
 * AI健康检查
 */
export function health() {
  return request({
    url: '/api/ai/health',
    method: 'GET'
  })
}

/**
 * 获取用户配额
 */
export function getQuota(userId = 'default') {
  return request({
    url: '/api/ai/quota',
    method: 'GET',
    params: { userId }
  })
}