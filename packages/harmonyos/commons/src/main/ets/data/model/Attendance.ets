// 考勤模型

// 打卡类型
export enum CheckInType {
  WORK_START = 'work_start',   // 上班打卡
  WORK_END = 'work_end'        // 下班打卡
}

// 打卡状态
export enum CheckInStatus {
  NORMAL = 'normal',           // 正常
  LATE = 'late',               // 迟到
  LEAVE_EARLY = 'leave_early', // 早退
  ABSENT = 'absent',           // 缺勤
  REST = 'rest',               // 休息
  HOLIDAY = 'holiday'          // 节假日
}

// 考勤规则
export interface AttendanceRule {
  workStartTime: string        // 如 "09:00"
  workEndTime: string          // 如 "18:00"
  workDays: number[]           // 如 [1,2,3,4,5] 表示周一到周五
  lateThreshold: number        // 迟到阈值（分钟）
  earlyLeaveThreshold: number  // 早退阈值（分钟）
  locationName: string         // 打卡地点名称
  latitude: number             // 纬度
  longitude: number            // 经度
  range: number                // 有效范围（米）
}

// 打卡记录
export interface CheckInRecord {
  id: string
  userId: string
  userName: string
  date: string                  // 如 "2026-04-30"
  workStartTime?: number        // 上班打卡时间戳
  workEndTime?: number          // 下班打卡时间戳
  workStartStatus?: CheckInStatus
  workEndStatus?: CheckInStatus
  workStartLocation?: string
  workEndLocation?: string
  createdAt: number
}

// 打卡结果
export interface CheckInResult {
  success: boolean
  type: CheckInType
  status: CheckInStatus
  checkInTime: number
  location: string
  distance: number              // 距离打卡点的距离（米）
  message: string
}

// 考勤月统计
export interface AttendanceMonthSummary {
  year: number
  month: number
  userId: string
  normalDays: number            // 正常天数
  lateDays: number              // 迟到天数
  earlyLeaveDays: number        // 早退天数
  absentDays: number            // 缺勤天数
  restDays: number              // 休息天数
  holidayDays: number           // 节假日天数
  workDays: number              // 应工作天数
  actualWorkDays: number        // 实际出勤天数
  totalWorkHours: number        // 总工时
  overtimeHours: number         // 加班时长（小时）
}

// 请假类型
export enum LeaveType {
  ANNUAL = 'annual',           // 年假
  SICK = 'sick',               // 病假
  PERSONAL = 'personal',       // 事假
  MARRIAGE = 'marriage',       // 婚假
  MATERNITY = 'maternity',     // 产假
  PATERNITY = 'paternity',     // 陪产假
  FUNERAL = 'funeral',         // 丧假
  OTHER = 'other'              // 其他
}

// 请假记录
export interface LeaveRecord {
  id: string
  userId: string
  userName: string
  type: LeaveType
  startDate: string
  endDate: string
  totalDays: number
  reason: string
  status: 'pending' | 'approved' | 'rejected' | 'cancelled'
  approvalId?: string
  createdAt: number
}

// 请假申请请求
export interface LeaveApplyParams {
  type: LeaveType
  startDate: string
  endDate: string
  totalDays: number
  reason: string
}

// 考勤统计数据
export interface AttendanceStatistics {
  todayCheckIn: boolean
  todayCheckOut: boolean
  thisMonthNormal: number
  thisMonthLate: number
  thisMonthAbsent: number
  thisMonthOvertime: number
}

// 打卡列表查询参数
export interface CheckInListParams {
  startDate: string
  endDate: string
  page: number
  pageSize: number
}

// 打卡列表响应
export interface CheckInListResponse {
  records: CheckInRecord[]
  total: number
}

// 请假列表响应
export interface LeaveListResponse {
  records: LeaveRecord[]
  total: number
}
