/**
 * 报表API
 */
import request from '@/utils/request'

/**
 * 获取报表列表
 */
export function getReports(params?: any) {
  return request({
    url: '/api/report/list',
    method: 'GET',
    params
  })
}

/**
 * 获取报表详情
 */
export function getReportDetail(id: number) {
  return request({
    url: `/api/report/${id}`,
    method: 'GET'
  })
}

/**
 * 创建报表
 */
export function createReport(data: any) {
  return request({
    url: '/api/report',
    method: 'POST',
    data
  })
}

/**
 * 删除报表
 */
export function deleteReport(id: number) {
  return request({
    url: `/api/report/${id}`,
    method: 'DELETE'
  })
}

/**
 * 下载报表
 */
export function downloadReport(id: number) {
  return request({
    url: `/api/report/${id}/download`,
    method: 'GET',
    responseType: 'blob'
  })
}

/**
 * AI生成报表
 */
export function generateAIReport(data: {
  type: string
  dateRange: string[]
  prompt: string
}) {
  return request({
    url: '/api/report/ai-generate',
    method: 'POST',
    data
  })
}