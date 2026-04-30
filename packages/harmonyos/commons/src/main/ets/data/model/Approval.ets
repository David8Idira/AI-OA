// 审批模型

// 审批状态
export enum ApprovalStatus {
  DRAFT = 'draft',           // 草稿
  PENDING = 'pending',       // 待审批
  APPROVED = 'approved',     // 已同意
  REJECTED = 'rejected',     // 已拒绝
  CANCELLED = 'cancelled',  // 已撤回
  TRANSFERRED = 'transferred' // 已转交
}

// 审批类型
export enum ApprovalType {
  LEAVE = 'leave',           // 请假
  OVERTIME = 'overtime',     // 加班
  TRIP = 'trip',             // 出差
  EXPENSE = 'expense',       // 报销
  PURCHASE = 'purchase',     // 采购
  CUSTOM = 'custom'          // 自定义
}

// 审批紧急程度
export enum ApprovalPriority {
  NORMAL = 0,
  URGENT = 1,
  VERY_URGENT = 2
}

// 审批节点
export interface ApprovalNode {
  id: string
  name: string
  type: 'start' | 'approval' | '抄送' | 'end'
  assigneeId: string
  assigneeName: string
  assigneeAvatar?: string
  status: 'pending' | 'approved' | 'rejected' | 'skipped'
  comment?: string
  operatedAt?: number
}

// 审批表单字段
export interface ApprovalFormField {
  key: string
  label: string
  type: 'text' | 'number' | 'date' | 'select' | 'textarea'
  value: string | number
  required: boolean
  options?: { label: string; value: string }[]
}

// 审批实例
export interface Approval {
  id: string
  title: string
  type: ApprovalType
  status: ApprovalStatus
  priority: ApprovalPriority
  applicantId: string
  applicantName: string
  applicantAvatar?: string
  applicantDept: string
  formFields: ApprovalFormField[]
  flowNodes: ApprovalNode[]
  currentNodeId: string
  createdAt: number
  updatedAt: number
  completedAt?: number
  comment?: string
}

// 审批列表查询参数
export interface ApprovalListParams {
  status?: ApprovalStatus
  type?: ApprovalType
  page: number
  pageSize: number
  keyword?: string
}

// 审批列表响应
export interface ApprovalListResponse {
  approvals: Approval[]
  total: number
}

// 创建审批请求
export interface CreateApprovalParams {
  title: string
  type: ApprovalType
  priority?: ApprovalPriority
  formFields: ApprovalFormField[]
  firstNodeAssigneeId: string
}

// 审批操作请求
export interface ApprovalActionParams {
  approvalId: string
  nodeId: string
  action: 'approve' | 'reject' | 'transfer'
  comment?: string
  transferAssigneeId?: string
}

// 审批统计数据
export interface ApprovalStatistics {
  pendingCount: number      // 待我审批数
  initiatedCount: number    // 我发起的审批数
  processedCount: number    // 我已处理的审批数
  draftCount: number        // 草稿数
}

// 审批类型配置（用于发起审批时选择类型）
export interface ApprovalTypeConfig {
  type: ApprovalType
  name: string
  icon: string
  description: string
  formTemplate: ApprovalFormField[]
}
