import ApiClient, { ApiResponse } from '../data/api/ApiClient'
import {
  Approval,
  ApprovalStatus,
  ApprovalType,
  ApprovalListParams,
  CreateApprovalParams,
  ApprovalActionParams,
  ApprovalStatistics,
  ApprovalTypeConfig
} from '../data/model/Approval'

/**
 * 审批服务类
 * 封装审批模块所有API调用
 */
export class ApprovalService {
  private client: ApiClient

  constructor() {
    this.client = ApiClient
  }

  /**
   * 获取审批列表
   * @param params 查询参数
   */
  async getApprovals(params: ApprovalListParams): Promise<ApiResponse<{ approvals: Approval[]; total: number }>> {
    return this.client.get<{ approvals: Approval[]; total: number }>('/api/v1/approval/approvals', params as Record<string, any>)
  }

  /**
   * 获取我发起的审批列表
   * @param params 查询参数
   */
  async getMyInitiatedApprovals(params?: { status?: ApprovalStatus; page?: number; pageSize?: number }): Promise<ApiResponse<{ approvals: Approval[]; total: number }>> {
    return this.client.get<{ approvals: Approval[]; total: number }>('/api/v1/approval/approvals/initiated', params as Record<string, any>)
  }

  /**
   * 获取待我审批的列表
   * @param params 查询参数
   */
  async getPendingApprovals(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<{ approvals: Approval[]; total: number }>> {
    return this.client.get<{ approvals: Approval[]; total: number }>('/api/v1/approval/approvals/pending', params as Record<string, any>)
  }

  /**
   * 获取审批详情
   * @param approvalId 审批ID
   */
  async getApprovalDetail(approvalId: string): Promise<ApiResponse<Approval>> {
    return this.client.get<Approval>(`/api/v1/approval/approvals/${approvalId}`)
  }

  /**
   * 创建审批（保存草稿）
   * @param params 创建参数
   */
  async createApproval(params: CreateApprovalParams): Promise<ApiResponse<{ approvalId: string }>> {
    return this.client.post<{ approvalId: string }>('/api/v1/approval/approvals', params)
  }

  /**
   * 提交审批
   * @param approvalId 审批ID
   */
  async submitApproval(approvalId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/approval/approvals/${approvalId}/submit`, {})
  }

  /**
   * 审批操作（同意/拒绝/转交）
   * @param params 操作参数
   */
  async approvalAction(params: ApprovalActionParams): Promise<ApiResponse<any>> {
    return this.client.post<any>('/api/v1/approval/approvals/action', params)
  }

  /**
   * 同意审批
   * @param approvalId 审批ID
   * @param nodeId 节点ID
   * @param comment 审批意见
   */
  async approve(approvalId: string, nodeId: string, comment?: string): Promise<ApiResponse<any>> {
    return this.approvalAction({ approvalId, nodeId, action: 'approve', comment })
  }

  /**
   * 拒绝审批
   * @param approvalId 审批ID
   * @param nodeId 节点ID
   * @param comment 拒绝原因
   */
  async reject(approvalId: string, nodeId: string, comment?: string): Promise<ApiResponse<any>> {
    return this.approvalAction({ approvalId, nodeId, action: 'reject', comment })
  }

  /**
   * 转交审批
   * @param approvalId 审批ID
   * @param nodeId 节点ID
   * @param transferAssigneeId 被转交给人的ID
   * @param comment 转交原因
   */
  async transferApproval(approvalId: string, nodeId: string, transferAssigneeId: string, comment?: string): Promise<ApiResponse<any>> {
    return this.approvalAction({ approvalId, nodeId, action: 'transfer', comment, transferAssigneeId })
  }

  /**
   * 撤回审批
   * @param approvalId 审批ID
   */
  async cancelApproval(approvalId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/approval/approvals/${approvalId}/cancel`, {})
  }

  /**
   * 删除审批草稿
   * @param approvalId 审批ID
   */
  async deleteApproval(approvalId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/approval/approvals/${approvalId}`)
  }

  /**
   * 获取审批类型列表
   */
  async getApprovalTypes(): Promise<ApiResponse<ApprovalTypeConfig[]>> {
    return this.client.get<ApprovalTypeConfig[]>('/api/v1/approval/types')
  }

  /**
   * 获取审批统计数据
   */
  async getApprovalStatistics(): Promise<ApiResponse<ApprovalStatistics>> {
    return this.client.get<ApprovalStatistics>('/api/v1/approval/statistics')
  }

  /**
   * 获取可用的审批人列表
   * @param type 审批类型
   */
  async getAvailableApprovers(type?: ApprovalType): Promise<ApiResponse<{ id: string; name: string; avatar?: string; department: string }[]>> {
    return this.client.get<{ id: string; name: string; avatar?: string; department: string }[]>('/api/v1/approval/approvers', type ? { type } : undefined)
  }

  /**
   * 获取审批历史记录
   * @param approvalId 审批ID
   */
  async getApprovalHistory(approvalId: string): Promise<ApiResponse<any[]>> {
    return this.client.get<any[]>(`/api/v1/approval/approvals/${approvalId}/history`)
  }

  /**
   * 催办审批
   * @param approvalId 审批ID
   */
  async urgeApproval(approvalId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/approval/approvals/${approvalId}/urge`, {})
  }

  /**
   * 添加审批抄送人
   * @param approvalId 审批ID
   * @param userIds 抄送人ID列表
   */
  async addCC(approvalId: string, userIds: string[]): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/approval/approvals/${approvalId}/cc`, { userIds })
  }

  /**
   * 签收审批任务
   * @param approvalId 审批ID
   * @param nodeId 节点ID
   */
  async claimTask(approvalId: string, nodeId: string): Promise<ApiResponse<any>> {
    return this.client.post<any>(`/api/v1/approval/approvals/${approvalId}/nodes/${nodeId}/claim`, {})
  }
}

// 导出服务实例
export default new ApprovalService()
