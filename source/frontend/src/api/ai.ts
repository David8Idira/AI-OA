import request from './index'

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