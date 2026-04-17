/**
 * 知识库API
 */
import request from '@/utils/request'

/**
 * 搜索知识库
 */
export function searchKnowledge(keyword: string) {
  return request({
    url: '/api/knowledge/search',
    method: 'GET',
    params: { keyword }
  })
}

/**
 * 语义搜索
 */
export function semanticSearch(query: string, topN = 5) {
  return request({
    url: '/api/knowledge/semantic',
    method: 'GET',
    params: { query, topN }
  })
}

/**
 * 获取知识文档
 */
export function getKnowledgeDoc(id: number) {
  return request({
    url: `/api/knowledge/doc/${id}`,
    method: 'GET'
  })
}

/**
 * 获取分类
 */
export function getKnowledgeCategories() {
  return request({
    url: '/api/knowledge/categories',
    method: 'GET'
  })
}

/**
 * 获取统计
 */
export function getKnowledgeStats() {
  return request({
    url: '/api/knowledge/stats',
    method: 'GET'
  })
}