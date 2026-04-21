import request from '@/utils/request'
import dayjs from 'dayjs'

// ============ 枚举和类型定义 ============

/** 审批状态 */
export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

/** 审批类型 */
export type ApprovalType = 
  | 'LEAVE'      // 请假申请
  | 'EXPENSE'    // 费用报销
  | 'PURCHASE'   // 采购申请
  | 'TRAVEL'     // 差旅申请
  | 'OVERTIME'   // 加班申请
  | 'WORK_FROM_HOME'  // 居家办公
  | 'RESIGN'     // 离职申请
  | 'REIMBURSE'  // 通用报销

/** 审批状态信息 */
export const APPROVAL_STATUS_MAP: Record<ApprovalStatus, { label: string; type: string }> = {
  PENDING: { label: '待审批', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  CANCELLED: { label: '已撤回', type: 'info' }
}

/** 审批类型信息 */
export const APPROVAL_TYPE_MAP: Record<ApprovalType, { label: string; icon: string; color: string }> = {
  LEAVE: { label: '请假申请', icon: 'Calendar', color: '#409EFF' },
  EXPENSE: { label: '费用报销', icon: 'Coin', color: '#67C23A' },
  PURCHASE: { label: '采购申请', icon: 'ShoppingCart', color: '#E6A23C' },
  TRAVEL: { label: '差旅申请', icon: 'Vehicle', color: '#909399' },
  OVERTIME: { label: '加班申请', icon: 'Clock', color: '#F56C6C' },
  WORK_FROM_HOME: { label: '居家办公', icon: 'HomeFilled', color: '#9B59B6' },
  RESIGN: { label: '离职申请', icon: 'WarnTriangleFilled', color: '#F56C6C' },
  REIMBURSE: { label: '通用报销', icon: 'Document', color: '#409EFF' }
}

// ============ 数据传输对象 ============

/** 审批表单数据 */
export interface ApprovalFormDTO {
  id?: string
  type: ApprovalType
  title: string
  content: string
  reason: string
  amount?: number
  startDate?: string
  endDate?: string
  attachments?: string[]
  formData?: Record<string, any>
}

/** 审批记录查询参数 */
export interface ApprovalQueryDTO {
  page: number
  pageSize: number
  status?: ApprovalStatus
  type?: ApprovalType
  keyword?: string
  startDate?: string
  endDate?: string
  tab?: 'all' | 'pending' | 'processed' | 'initiated'
}

/** 审批处理操作 */
export interface ApprovalActionDTO {
  id: string
  action: 'APPROVE' | 'REJECT' | 'CANCEL'
  comment?: string
}

// ============ 视图对象 ============

/** 审批概要信息 */
export interface ApprovalVO {
  id: string
  title: string
  type: ApprovalType
  status: ApprovalStatus
  submitterId: string
  submitterName: string
  submitterAvatar?: string
  deptName?: string
  reason: string
  amount?: number
  startDate?: string
  endDate?: string
  createTime: string
  updateTime?: string
  commentCount?: number
  currentStep?: number
  totalSteps?: number
}

/** 审批详情信息 */
export interface ApprovalDetailVO extends ApprovalVO {
  content: string
  attachments: Array<{ id: string; name: string; url: string; size: number }>
  formData: Record<string, any>
  history: ApprovalHistoryVO[]
  currentApprover?: {
    id: string
    name: string
    avatar?: string
  }
  nextApprovers?: Array<{
    id: string
    name: string
    avatar?: string
  }>
}

/** 审批历史记录 */
export interface ApprovalHistoryVO {
  id: string
  approvalId: string
  step: number
  action: 'SUBMIT' | 'APPROVE' | 'REJECT' | 'CANCEL' | 'TRANSFER'
  operatorId: string
  operatorName: string
  operatorAvatar?: string
  comment?: string
  createTime: string
  duration?: string // 处理耗时
}

// ============ API 方法 ============

/** 获取审批列表 */
export const getApprovalList = (params: ApprovalQueryDTO) => {
  return request.get<{
    list: ApprovalVO[]
    total: number
    page: number
    pageSize: number
  }>('/approvals', { params })
}

/** 获取审批详情 */
export const getApprovalDetail = (id: string) => {
  return request.get<ApprovalDetailVO>(`/approvals/${id}`)
}

/** 提交审批 */
export const submitApproval = (data: ApprovalFormDTO) => {
  return request.post<string>('/approvals', data)
}

/** 审批操作（通过/拒绝） */
export const processApproval = (data: ApprovalActionDTO) => {
  return request.post(`/approvals/${data.id}/process`, {
    action: data.action,
    comment: data.comment
  })
}

/** 撤回审批 */
export const cancelApproval = (id: string) => {
  return request.post(`/approvals/${id}/cancel`)
}

/** 获取待我审批的数量 */
export const getPendingCount = () => {
  return request.get<number>('/approvals/pending/count')
}

/** 批量获取审批统计 */
export const getApprovalStats = () => {
  return request.get<{
    pending: number
    approved: number
    rejected: number
    initiated: number
  }>('/approvals/stats')
}

/** 删除审批草稿 */
export const deleteApprovalDraft = (id: string) => {
  return request.delete(`/approvals/draft/${id}`)
}

// ============ 辅助函数 ============

/** 格式化审批时间显示 */
export const formatApprovalTime = (time: string) => {
  const t = dayjs(time)
  const now = dayjs()
  const diff = now.diff(t, 'minute')
  
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  if (diff < 10080) return `${Math.floor(diff / 1440)}天前`
  return t.format('YYYY-MM-DD')
}

/** 获取状态标签配置 */
export const getStatusConfig = (status: ApprovalStatus) => {
  return APPROVAL_STATUS_MAP[status] || { label: '未知', type: 'info' }
}

/** 获取类型标签配置 */
export const getTypeConfig = (type: ApprovalType) => {
  return APPROVAL_TYPE_MAP[type] || { label: '其他', icon: 'Document', color: '#909399' }
}
