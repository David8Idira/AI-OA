import ApiClient, { ApiResponse } from '../data/api/ApiClient'

/**
 * 考勤模型
 */

// 打卡类型
export enum CheckInType {
  NORMAL = 'normal',       // 正常打卡
  LATE = 'late',           // 迟到打卡
  LEAVE_EARLY = 'leave_early', // 早退打卡
  ABSENT = 'absent',       // 缺勤
  OVERTIME = 'overtime'   // 加班打卡
}

// 打卡状态
export enum CheckInStatus {
  SUCCESS = 'success',
  FAILED = 'failed',
  LATE = 'late',
  LEAVE_EARLY = 'leave_early'
}

// 请假类型
export enum LeaveType {
  ANNUAL = 'annual',           // 年假
  SICK = 'sick',               // 病假
  PERSONAL = 'personal',      // 事假
  MARRIAGE = 'marriage',       // 婚假
  MATERNITY = 'maternity',     // 产假
  PATERNITY = 'paternity',     // 陪产假
  BEREAVEMENT = 'bereavement', // 丧假
  HOME = 'home',               // 探亲假
  OUT = 'out'                  // 外出
}

// 请假状态
export enum LeaveStatus {
  PENDING = 'pending',     // 待审批
  APPROVED = 'approved',   // 已同意
  REJECTED = 'rejected',   // 已拒绝
  CANCELLED = 'cancelled'  // 已撤回
}

// 考勤规则
export interface AttendanceRule {
  workStartTime: string       // 如 "09:00"
  workEndTime: string         // 如 "18:00"
  workDays: number[]           // 工作日，1-7（周一到周日）
  checkInEnabled: boolean
  checkOutEnabled: boolean
  lateThreshold: number        // 迟到阈值（分钟）
  leaveEarlyThreshold: number  // 早退阈值（分钟）
  locationCheckEnabled: boolean
  wifiCheckEnabled: boolean
  allowedRadius: number        // 允许打卡范围（米）
  wifiName?: string            // 允许的WiFi名称
  needPhoto: boolean           // 是否需要拍照
  overtimeRule: {
    minDuration: number        // 最小加班时长（小时）
    allowRemote: boolean
  }
}

// 打卡记录
export interface CheckInRecord {
  id: string
  userId: string
  userName: string
  type: CheckInType
  status: CheckInStatus
  checkInTime: number
  location?: {
    latitude: number
    longitude: number
    address: string
  }
  wifiName?: string
  photoUrl?: string
  remark?: string
}

// 打卡结果
export interface CheckInResult {
  success: boolean
  type: CheckInType
  status: CheckInStatus
  message: string
  record?: CheckInRecord
  rules?: AttendanceRule
}

// 今日打卡状态
export interface TodayStatus {
  date: string
  checkInRecord?: CheckInRecord
  checkOutRecord?: CheckInRecord
  isHoliday: boolean
  holidayName?: string
  isWeekend: boolean
  canCheckIn: boolean
  canCheckOut: boolean
  rule?: AttendanceRule
}

// 考勤月统计
export interface AttendanceSummary {
  year: number
  month: number
  normalDays: number       // 正常天数
  lateDays: number        // 迟到天数
  leaveEarlyDays: number  // 早退天数
  absentDays: number      // 缺勤天数
  overtimeHours: number   // 加班时长（小时）
  leaveDays: number       // 请假天数
  actualWorkDays: number  // 实际出勤天数
  shouldWorkDays: number  // 应出勤天数
  checkInRate: number     // 打卡率（百分比）
}

// 请假记录
export interface LeaveRecord {
  id: string
  type: LeaveType
  status: LeaveStatus
  startDate: string
  endDate: string
  totalDays: number
  reason: string
  applicantId: string
  applicantName: string
  applicantAvatar?: string
  createdAt: number
  updatedAt: number
  approvalId?: string
  remark?: string
}

// 申请请假参数
export interface ApplyLeaveParams {
  type: LeaveType
  startDate: string    // YYYY-MM-DD
  endDate: string      // YYYY-MM-DD
  totalDays: number
  reason: string
  attachments?: string[] // 附件ID列表
}

// 考勤统计数据
export interface AttendanceStatistics {
  todayCheckInRate: number
  todayCheckOutRate: number
  weekCheckInRate: number
  monthCheckInRate: number
  averageWorkHours: number
  overtimeHours: number
  pendingLeaveCount: number
  approvedLeaveDays: number
}

/**
 * 考勤服务类
 * 封装考勤模块所有API调用
 */
export class AttendanceService {
  private client: ApiClient

  constructor() {
    this.client = ApiClient
  }

  // ============ 打卡相关 ============

  /**
   * 打卡
   * @param data 打卡数据
   */
  async checkIn(data: {
    type: 'check_in' | 'check_out'
    latitude: number
    longitude: number
    location: string
    wifiName?: string
    photoBase64?: string
    remark?: string
  }): Promise<ApiResponse<CheckInResult>> {
    return this.client.post<CheckInResult>('/api/v1/attendance/checkin', data)
  }

  /**
   * 获取打卡记录列表
   * @param params 查询参数
   */
  async getCheckInRecords(params?: {
    startDate?: string
    endDate?: string
    type?: CheckInType
    page?: number
    pageSize?: number
  }): Promise<ApiResponse<{ records: CheckInRecord[]; total: number }>> {
    return this.client.get<{ records: CheckInRecord[]; total: number }>('/api/v1/attendance/records', params as Record<string, any>)
  }

  /**
   * 获取今日打卡状态
   */
  async getTodayStatus(): Promise<ApiResponse<TodayStatus>> {
    return this.client.get<TodayStatus>('/api/v1/attendance/today')
  }

  /**
   * 获取考勤月统计
   * @param year 年份
   * @param month 月份
   */
  async getAttendanceSummary(year: number, month: number): Promise<ApiResponse<AttendanceSummary>> {
    return this.client.get<AttendanceSummary>('/api/v1/attendance/summary', { year, month })
  }

  /**
   * 补卡申请
   * @param data 补卡数据
   */
  async applyMakeup(data: {
    date: string
    type: 'check_in' | 'check_out'
    reason: string
    latitude: number
    longitude: number
    location: string
  }): Promise<ApiResponse<{ makeupId: string }>> {
    return this.client.post<{ makeupId: string }>('/api/v1/attendance/makeup', data)
  }

  /**
   * 获取补卡记录
   */
  async getMakeupRecords(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<any>> {
    return this.client.get<any>('/api/v1/attendance/makeup', params as Record<string, any>)
  }

  // ============ 考勤规则 ============

  /**
   * 获取考勤规则
   */
  async getAttendanceRule(): Promise<ApiResponse<AttendanceRule>> {
    return this.client.get<AttendanceRule>('/api/v1/attendance/rule')
  }

  /**
   * 获取排班计划
   * @param startDate 开始日期
   * @param endDate 结束日期
   */
  async getSchedule(startDate: string, endDate: string): Promise<ApiResponse<any[]>> {
    return this.client.get<any[]>('/api/v1/attendance/schedule', { startDate, endDate })
  }

  // ============ 请假相关 ============

  /**
   * 申请请假
   * @param params 请假参数
   */
  async applyLeave(params: ApplyLeaveParams): Promise<ApiResponse<{ leaveId: string; approvalId?: string }>> {
    return this.client.post<{ leaveId: string; approvalId?: string }>('/api/v1/attendance/leave', params)
  }

  /**
   * 获取请假记录
   * @param params 查询参数
   */
  async getLeaveRecords(params?: {
    status?: LeaveStatus
    type?: LeaveType
    startDate?: string
    endDate?: string
    page?: number
    pageSize?: number
  }): Promise<ApiResponse<{ records: LeaveRecord[]; total: number }>> {
    return this.client.get<{ records: LeaveRecord[]; total: number }>('/api/v1/attendance/leave', params as Record<string, any>)
  }

  /**
   * 获取请假详情
   * @param leaveId 请假ID
   */
  async getLeaveDetail(leaveId: string): Promise<ApiResponse<LeaveRecord>> {
    return this.client.get<LeaveRecord>(`/api/v1/attendance/leave/${leaveId}`)
  }

  /**
   * 取消请假申请
   * @param leaveId 请假ID
   */
  async cancelLeave(leaveId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`/api/v1/attendance/leave/${leaveId}`)
  }

  /**
   * 获取剩余假期天数
   */
  async getRemainingLeave(): Promise<ApiResponse<{ type: LeaveType; total: number; used: number; remaining: number }[]>> {
    return this.client.get<{ type: LeaveType; total: number; used: number; remaining: number }[]>('/api/v1/attendance/leave/balance')
  }

  // ============ 统计数据 ============

  /**
   * 获取考勤统计数据
   */
  async getAttendanceStatistics(): Promise<ApiResponse<AttendanceStatistics>> {
    return this.client.get<AttendanceStatistics>('/api/v1/attendance/statistics')
  }

  /**
   * 获取考勤异常记录
   * @param params 查询参数
   */
  async getAbnormalRecords(params?: { startDate?: string; endDate?: string; page?: number; pageSize?: number }): Promise<ApiResponse<{ records: any[]; total: number }>> {
    return this.client.get<{ records: any[]; total: number }>('/api/v1/attendance/abnormal', params as Record<string, any>)
  }

  /**
   * 获取加班记录
   * @param params 查询参数
   */
  async getOvertimeRecords(params?: { startDate?: string; endDate?: string; page?: number; pageSize?: number }): Promise<ApiResponse<{ records: any[]; total: number }>> {
    return this.client.get<{ records: any[]; total: number }>('/api/v1/attendance/overtime', params as Record<string, any>)
  }

  /**
   * 申请加班
   * @param data 加班数据
   */
  async applyOvertime(data: {
    date: string
    startTime: string
    endTime: string
    reason: string
  }): Promise<ApiResponse<{ overtimeId: string }>> {
    return this.client.post<{ overtimeId: string }>('/api/v1/attendance/overtime', data)
  }
}

// 导出服务实例
export default new AttendanceService()
