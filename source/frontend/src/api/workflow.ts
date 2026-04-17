import request from '@/utils/request'

/**
 * 工作流API
 */
export function triggerWorkflow(workflowId: string, data?: any) {
  return request({
    url: `/api/workflow/n8n/trigger/${workflowId}`,
    method: 'POST',
    data
  })
}

/**
 * 获取工作流状态
 */
export function getWorkflowStatus(workflowId: string) {
  return request({
    url: `/api/workflow/n8n/status/${workflowId}`,
    method: 'GET'
  })
}

/**
 * 注册工作流
 */
export function registerWorkflow(data: {
  workflowId: string
  name: string
  type: string
  webhookUrl: string
  triggerCondition?: string
  enabled?: boolean
}) {
  return request({
    url: '/api/workflow/n8n/register',
    method: 'POST',
    data
  })
}

/**
 * 报表API
 */
export function generateReport(data: {
  type: string
  title: string
  data: Record<string, any>
  recipients: string[]
}) {
  return request({
    url: '/api/report/generate',
    method: 'POST',
    data
  })
}

/**
 * 获取报表列表
 */
export function getReportList(params?: {
  page?: number
  pageSize?: number
  type?: string
}) {
  return request({
    url: '/api/report/list',
    method: 'GET',
    params
  })
}