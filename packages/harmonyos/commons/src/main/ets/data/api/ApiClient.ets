import http from '@ohos.net.http'
import { getApiConfig } from '../../../../config/api-config'

// API配置常量
export const API_BASE_URL = 'https://api.example.com'
export const API_TIMEOUT = 30000

// ============ 认证模块 API ============

// 登录
export const API_AUTH_LOGIN = '/api/v1/auth/login'
export const API_AUTH_LOGOUT = '/api/v1/auth/logout'
export const API_AUTH_REFRESH = '/api/v1/auth/refresh'
export const API_AUTH_REGISTER = '/api/v1/auth/register'
export const API_AUTH_FORGET_PASSWORD = '/api/v1/auth/forget-password'
export const API_AUTH_RESET_PASSWORD = '/api/v1/auth/reset-password'

// ============ 用户模块 API ============

export const API_USER_INFO = '/api/v1/user/info'
export const API_USER_LIST = '/api/v1/user/list'
export const API_USER_DETAIL = '/api/v1/user/detail'
export const API_USER_UPDATE = '/api/v1/user/update'
export const API_USER_PASSWORD = '/api/v1/user/password'
export const API_USER_AVATAR = '/api/v1/user/avatar'

// ============ 角色模块 API ============

export const API_ROLE_LIST = '/api/v1/roles'
export const API_ROLE_DETAIL = '/api/v1/roles/detail'
export const API_ROLE_CREATE = '/api/v1/roles/create'
export const API_ROLE_UPDATE = '/api/v1/roles/update'
export const API_ROLE_DELETE = '/api/v1/roles/delete'
export const API_ROLE_PERMISSIONS = '/api/v1/roles/permissions'
export const API_ROLE_USERS = '/api/v1/roles/users'

// ============ 部门模块 API ============

export const API_DEPT_LIST = '/api/v1/departments'
export const API_DEPT_DETAIL = '/api/v1/departments/detail'
export const API_DEPT_CREATE = '/api/v1/departments/create'
export const API_DEPT_UPDATE = '/api/v1/departments/update'
export const API_DEPT_DELETE = '/api/v1/departments/delete'
export const API_DEPT_USERS = '/api/v1/departments/users'

// ============ 知识库模块 API ============

export const API_KNOWLEDGE_DOCUMENTS = '/api/v1/knowledge/documents'
export const API_KNOWLEDGE_CATEGORIES = '/api/v1/knowledge/categories'
export const API_KNOWLEDGE_SEARCH = '/api/v1/knowledge/search'
export const API_KNOWLEDGE_STARS = '/api/v1/knowledge/stars'
export const API_KNOWLEDGE_RECENT = '/api/v1/knowledge/recent'
export const API_KNOWLEDGE_RECOMMENDED = '/api/v1/knowledge/recommended'
export const API_KNOWLEDGE_MY = '/api/v1/knowledge/my'

// ============ 审批模块 API ============

export const API_APPROVAL_LIST = '/api/v1/approval/approvals'
export const API_APPROVAL_DETAIL = '/api/v1/approval/approvals/detail'
export const API_APPROVAL_CREATE = '/api/v1/approval/approvals/create'
export const API_APPROVAL_ACTION = '/api/v1/approval/approvals/action'
export const API_APPROVAL_TYPES = '/api/v1/approval/types'
export const API_APPROVAL_STATISTICS = '/api/v1/approval/statistics'
export const API_APPROVAL_APPROVERS = '/api/v1/approval/approvers'

// ============ 考勤模块 API ============

export const API_ATTENDANCE_CHECKIN = '/api/v1/attendance/checkin'
export const API_ATTENDANCE_RECORDS = '/api/v1/attendance/records'
export const API_ATTENDANCE_TODAY = '/api/v1/attendance/today'
export const API_ATTENDANCE_SUMMARY = '/api/v1/attendance/summary'
export const API_ATTENDANCE_RULE = '/api/v1/attendance/rule'
export const API_ATTENDANCE_LEAVE = '/api/v1/attendance/leave'
export const API_ATTENDANCE_STATISTICS = '/api/v1/attendance/statistics'
export const API_ATTENDANCE_MAKEUP = '/api/v1/attendance/makeup'
export const API_ATTENDANCE_OVERTIME = '/api/v1/attendance/overtime'
export const API_ATTENDANCE_SCHEDULE = '/api/v1/attendance/schedule'
export const API_ATTENDANCE_ABNORMAL = '/api/v1/attendance/abnormal'

// ============ IM模块 API ============

export const API_IM_CONVERSATIONS = '/api/v1/im/conversations'
export const API_IM_MESSAGES = '/api/v1/im/messages'
export const API_IM_CONTACTS = '/api/v1/im/contacts'
export const API_IM_DRAFTS = '/api/v1/im/drafts'
export const API_IM_READ_ALL = '/api/v1/im/read-all'
export const API_IM_UNREAD_COUNT = '/api/v1/im/unread-count'

// ============ 报表模块 API ============

export const API_REPORT_LIST = '/api/v1/reports'
export const API_REPORT_DETAIL = '/api/v1/reports/detail'
export const API_REPORT_CREATE = '/api/v1/reports/create'
export const API_REPORT_EXPORT = '/api/v1/reports/export'
export const API_REPORT_DASHBOARD = '/api/v1/reports/dashboard'

// ============ 通知模块 API ============

export const API_NOTIFICATION_LIST = '/api/v1/notifications'
export const API_NOTIFICATION_READ = '/api/v1/notifications/read'
export const API_NOTIFICATION_READ_ALL = '/api/v1/notifications/read-all'
export const API_NOTIFICATION_COUNT = '/api/v1/notifications/count'

// ============ 文件模块 API ============

export const API_FILE_UPLOAD = '/api/v1/files/upload'
export const API_FILE_DOWNLOAD = '/api/v1/files/download'
export const API_FILE_DELETE = '/api/v1/files/delete'
export const API_FILE_LIST = '/api/v1/files/list'

// HTTP响应结果封装
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// API错误类型
export class ApiError extends Error {
  code: number
  response?: any

  constructor(code: number, message: string, response?: any) {
    super(message)
    this.code = code
    this.response = response
    this.name = 'ApiError'
  }
}

// ApiClient配置
interface ClientConfig {
  baseUrl: string
  timeout: number
}

// 请求选项
interface RequestOptions {
  url?: string
  method?: http.RequestMethod
  header?: Record<string, string>
  extraData?: any
  timeout?: number
}

// API客户端单例
export class ApiClient {
  private static instance: ApiClient
  private config: ClientConfig = {
    baseUrl: API_BASE_URL,
    timeout: API_TIMEOUT
  }
  private context: any = null

  private constructor() {}

  // 初始化
  static init(config?: Partial<ClientConfig>): void {
    if (!ApiClient.instance) {
      ApiClient.instance = new ApiClient()
    }
    const apiConfig = getApiConfig()
    ApiClient.instance.config = {
      baseUrl: config?.baseUrl || apiConfig.baseUrl || API_BASE_URL,
      timeout: config?.timeout || apiConfig.timeout || API_TIMEOUT
    }
  }

  // 设置上下文
  static setContext(context: any): void {
    if (ApiClient.instance) {
      ApiClient.instance.context = context
    }
  }

  // 获取Token
  private getToken(): string | null {
    return AppStorage.get<string>('auth_token')
  }

  // 通用请求方法
  async request<T>(options: RequestOptions): Promise<ApiResponse<T>> {
    const config = this.config
    const token = this.getToken()

    // 构建完整URL
    const fullUrl = options.url?.startsWith('http') ? options.url : `${config.baseUrl}${options.url}`

    // 创建HTTP请求
    const httpRequest = http.createHttp()

    // 构建请求头
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...options.header
    }

    // 添加Token
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    try {
      const response = await httpRequest.request(
        fullUrl,
        {
          method: options.method || http.RequestMethod.GET,
          header: headers,
          extraData: options.extraData ? JSON.stringify(options.extraData) : undefined,
          connectTimeout: options.timeout || config.timeout,
          readTimeout: options.timeout || config.timeout
        }
      )

      // 解析响应
      const result = JSON.parse(response.result as string) as ApiResponse<T>

      // 检查业务错误
      if (result.code !== 0 && result.code !== 200) {
        throw new ApiError(result.code, result.message, result)
      }

      return result
    } catch (error) {
      if (error instanceof ApiError) {
        throw error
      }
      throw new ApiError(-1, error instanceof Error ? error.message : 'Network error', error)
    } finally {
      httpRequest.destroy()
    }
  }

  // GET请求
  async get<T>(url: string, params?: Record<string, any>, headers?: Record<string, string>): Promise<ApiResponse<T>> {
    let queryString = ''
    if (params) {
      queryString = '?' + Object.entries(params)
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
        .join('&')
    }
    return this.request<T>({
      url: url + queryString,
      method: http.RequestMethod.GET,
      header: headers
    })
  }

  // POST请求
  async post<T>(url: string, data?: any, headers?: Record<string, string>): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: http.RequestMethod.POST,
      extraData: data,
      header: headers
    })
  }

  // PUT请求
  async put<T>(url: string, data?: any, headers?: Record<string, string>): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: http.RequestMethod.PUT,
      extraData: data,
      header: headers
    })
  }

  // DELETE请求
  async delete<T>(url: string, headers?: Record<string, string>): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: http.RequestMethod.DELETE,
      header: headers
    })
  }

  // ============ 消息模块 API ============

  // 获取会话列表
  async getConversations(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<any>> {
    return this.get<any>('/api/im/conversations', params)
  }

  // 获取会话详情
  async getConversation(conversationId: string): Promise<ApiResponse<any>> {
    return this.get<any>(`/api/im/conversations/${conversationId}`)
  }

  // 获取消息列表
  async getMessages(conversationId: string, params?: { page?: number; pageSize?: number; before?: number }): Promise<ApiResponse<any>> {
    return this.get<any>(`/api/im/conversations/${conversationId}/messages`, params)
  }

  // 发送消息
  async sendMessage(data: { conversationId: string; type: string; content: string; imageUrl?: string }): Promise<ApiResponse<any>> {
    return this.post<any>('/api/im/messages', data)
  }

  // 标记消息已读
  async markRead(conversationId: string, messageId?: string): Promise<ApiResponse<any>> {
    return this.post<any>(`/api/im/conversations/${conversationId}/read`, { messageId })
  }

  // ============ 审批模块 API ============

  // 获取审批列表
  async getApprovals(params?: {
    status?: string;
    type?: string;
    page?: number;
    pageSize?: number;
    keyword?: string;
  }): Promise<ApiResponse<any>> {
    return this.get<any>('/api/approval/approvals', params)
  }

  // 获取审批详情
  async getApproval(approvalId: string): Promise<ApiResponse<any>> {
    return this.get<any>(`/api/approval/approvals/${approvalId}`)
  }

  // 创建审批
  async createApproval(data: any): Promise<ApiResponse<any>> {
    return this.post<any>('/api/approval/approvals', data)
  }

  // 审批操作（同意/拒绝/转交）
  async approvalAction(data: { approvalId: string; nodeId: string; action: string; comment?: string; transferAssigneeId?: string }): Promise<ApiResponse<any>> {
    return this.post<any>('/api/approval/approvals/action', data)
  }

  // 撤回审批
  async cancelApproval(approvalId: string): Promise<ApiResponse<any>> {
    return this.post<any>(`/api/approval/approvals/${approvalId}/cancel`, {})
  }

  // 获取审批类型列表
  async getApprovalTypes(): Promise<ApiResponse<any>> {
    return this.get<any>('/api/approval/types')
  }

  // 获取审批统计数据
  async getApprovalStatistics(): Promise<ApiResponse<any>> {
    return this.get<any>('/api/approval/statistics')
  }

  // ============ 考勤模块 API ============

  // 打卡
  async checkIn(data: { type: string; latitude: number; longitude: number; location: string }): Promise<ApiResponse<any>> {
    return this.post<any>('/api/attendance/checkin', data)
  }

  // 获取打卡记录列表
  async getCheckInRecords(params?: { startDate?: string; endDate?: string; page?: number; pageSize?: number }): Promise<ApiResponse<any>> {
    return this.get<any>('/api/attendance/records', params)
  }

  // 获取今日打卡状态
  async getTodayCheckInStatus(): Promise<ApiResponse<any>> {
    return this.get<any>('/api/attendance/today')
  }

  // 获取考勤月统计
  async getAttendanceSummary(year: number, month: number): Promise<ApiResponse<any>> {
    return this.get<any>('/api/attendance/summary', { year, month })
  }

  // 获取考勤规则
  async getAttendanceRule(): Promise<ApiResponse<any>> {
    return this.get<any>('/api/attendance/rule')
  }

  // 申请请假
  async applyLeave(data: { type: string; startDate: string; endDate: string; totalDays: number; reason: string }): Promise<ApiResponse<any>> {
    return this.post<any>('/api/attendance/leave', data)
  }

  // 获取请假记录
  async getLeaveRecords(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<any>> {
    return this.get<any>('/api/attendance/leave', params)
  }

  // 取消请假申请
  async cancelLeave(leaveId: string): Promise<ApiResponse<any>> {
    return this.delete<any>(`/api/attendance/leave/${leaveId}`)
  }

  // 获取考勤统计数据
  async getAttendanceStatistics(): Promise<ApiResponse<any>> {
    return this.get<any>('/api/attendance/statistics')
  }
}

// 导出默认实例
export default ApiClient

// 用户模型
export interface User {
  id: string
  name: string
  avatar?: string
  email?: string
  phone?: string
  department?: string
  position?: string
  roles?: string[]
  token?: string
}

// 登录请求参数
export interface LoginParams {
  username: string
  password: string
  rememberMe?: boolean
}

// 登录响应
export interface LoginResponse {
  token: string
  user: User
  expireAt: number
}

// 用户信息响应
export interface UserInfoResponse {
  user: User
  permissions: string[]
}
