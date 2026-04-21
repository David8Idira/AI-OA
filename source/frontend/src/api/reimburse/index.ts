import request from '@/utils/request'
import dayjs from 'dayjs'

// ============ 枚举和类型定义 ============

/** 报销状态 */
export type ReimburseStatus = 'DRAFT' | 'OCR_PROCESSING' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'PAID' | 'CANCELLED'

/** 发票类型 */
export type InvoiceType = 'VAT_NORMAL' | 'VAT_SPECIAL' | 'FLIGHT' | 'TRAIN' | 'TAXI' | 'OTHER'

/** 报销状态信息 */
export const REIMBURSE_STATUS_MAP: Record<ReimburseStatus, { label: string; type: string }> = {
  DRAFT: { label: '草稿', type: 'info' },
  OCR_PROCESSING: { label: 'OCR识别中', type: 'warning' },
  PENDING: { label: '待审批', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  PAID: { label: '已打款', type: 'success' },
  CANCELLED: { label: '已撤回', type: 'info' }
}

/** 发票类型信息 */
export const INVOICE_TYPE_MAP: Record<InvoiceType, { label: string }> = {
  VAT_NORMAL: { label: '增值税普票' },
  VAT_SPECIAL: { label: '增值税专票' },
  FLIGHT: { label: '机票' },
  TRAIN: { label: '火车票' },
  TAXI: { label: '打车票' },
  OTHER: { label: '其他' }
}

// ============ 数据传输对象 ============

/** 发票信息 */
export interface InvoiceVO {
  id: string
  type: InvoiceType
  code?: string
  number?: string
  date?: string
  buyerName?: string
  sellerName?: string
  amount: number
  taxAmount: number
  totalAmount: number
  imageUrl?: string
  confidence?: number
  status: 'PENDING' | 'CONFIRMED' | 'DOUBTFUL'
  doubtfulFields?: string[]
  createTime: string
}

/** 行程单信息 */
export interface TripExpenseVO {
  id: string
  platform: string
  tripNo?: string
  startTime?: string
  endTime?: string
  departure?: string
  destination?: string
  distance?: number
  amount: number
  status: string
  passenger?: string
  department?: string
  createTime: string
}

/** 报销单数据 */
export interface ReimburseVO {
  id: string
  title: string
  applicantId: string
  applicantName: string
  deptName: string
  totalAmount: number
  invoiceCount: number
  tripCount: number
  status: ReimburseStatus
  currentApprover?: string
  createTime: string
  updateTime?: string
  remark?: string
}

/** 报销单详情 */
export interface ReimburseDetailVO extends ReimburseVO {
  invoices: InvoiceVO[]
  trips: TripExpenseVO[]
  attachments: Array<{ id: string; name: string; url: string; size: number }>
  approvalHistory?: Array<{
    approverName: string
    action: string
    comment?: string
    createTime: string
  }>
}

/** 报销查询参数 */
export interface ReimburseQueryDTO {
  page: number
  pageSize: number
  status?: ReimburseStatus
  keyword?: string
  startDate?: string
  endDate?: string
}

/** OCR识别结果 */
export interface OCRResultVO {
  invoiceType: InvoiceType
  fields: Record<string, any>
  confidence: number
  imageUrl: string
  doubtfulFields: Array<{ field: string; confidence: number; value: string }>
}

// ============ API 方法 ============

/** 获取报销单列表 */
export const getReimburseList = (params: ReimburseQueryDTO) => {
  return request.get<{
    list: ReimburseVO[]
    total: number
    page: number
    pageSize: number
  }>('/reimburses', { params })
}

/** 获取报销单详情 */
export const getReimburseDetail = (id: string) => {
  return request.get<ReimburseDetailVO>(`/reimburses/${id}`)
}

/** 创建报销单 */
export const createReimburse = (data: {
  title: string
  invoiceIds?: string[]
  tripIds?: string[]
  remark?: string
}) => {
  return request.post<string>('/reimburses', data)
}

/** 更新报销单 */
export const updateReimburse = (id: string, data: Partial<ReimburseDetailVO>) => {
  return request.put(`/reimburses/${id}`, data)
}

/** 删除报销单 */
export const deleteReimburse = (id: string) => {
  return request.delete(`/reimburses/${id}`)
}

/** 提交报销审批 */
export const submitReimburse = (id: string) => {
  return request.post(`/reimburses/${id}/submit`)
}

/** 撤回报销 */
export const cancelReimburse = (id: string) => {
  return request.post(`/reimburses/${id}/cancel`)
}

// ============ OCR相关 ============

/** 上传发票并OCR识别 */
export const uploadInvoice = (formData: FormData) => {
  return request.post<OCRResultVO>('/ocr/invoices/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 获取发票列表 */
export const getInvoiceList = (params?: { status?: string }) => {
  return request.get<InvoiceVO[]>('/ocr/invoices', { params })
}

/** 确认发票信息 */
export const confirmInvoice = (id: string, data: Partial<InvoiceVO>) => {
  return request.put(`/ocr/invoices/${id}/confirm`, data)
}

/** 删除发票 */
export const deleteInvoice = (id: string) => {
  return request.delete(`/ocr/invoices/${id}`)
}

// ============ 行程单相关 ============

/** 获取行程单列表 */
export const getTripList = (params?: { startDate?: string; endDate?: string }) => {
  return request.get<TripExpenseVO[]>('/reimburses/trips', { params })
}

/** 同步行程单 */
export const syncTrips = (platform: string) => {
  return request.post(`/reimburses/trips/sync/${platform}`)
}

// ============ 统计 ============

/** 获取报销统计 */
export const getReimburseStats = () => {
  return request.get<{
    totalAmount: number
    pendingCount: number
    approvedCount: number
    rejectedCount: number
    monthlyTrend: Array<{ month: string; amount: number }>
  }>('/reimburses/stats')
}

// ============ 辅助函数 ============

/** 获取状态标签配置 */
export const getStatusConfig = (status: ReimburseStatus) => {
  return REIMBURSE_STATUS_MAP[status] || { label: '未知', type: 'info' }
}

/** 获取发票类型配置 */
export const getInvoiceTypeConfig = (type: InvoiceType) => {
  return INVOICE_TYPE_MAP[type] || { label: '其他' }
}

/** 格式化金额 */
export const formatAmount = (amount: number) => {
  return `¥${amount.toFixed(2)}`
}
