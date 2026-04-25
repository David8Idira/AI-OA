import request from '@/utils/request'

// Types
export interface CheckinDTO {
  userId: string
  checkinType: number // 0-checkin, 1-checkout
  method: number // 0-GPS, 1-WiFi, 2-Manual, 3-Remote
  latitude?: number | null
  longitude?: number | null
  address?: string | null
  wifiMac?: string | null
  deviceId: string
  ip: string
  remark?: string
}

export interface AttendanceQueryDTO {
  pageNum: number
  pageSize: number
  userId?: string
  startDate?: string
  endDate?: string
  status?: number | null
  abnormal?: number | null
  keyword?: string
  orderBy?: string
  includeDeleted?: boolean
}

export interface AttendanceRecord {
  id: number
  userId: string
  userName: string
  attendanceDate: string
  groupId: number
  ruleId: number
  checkinTime?: string
  checkoutTime?: string
  checkinLatitude?: number
  checkinLongitude?: number
  checkoutLatitude?: number
  checkoutLongitude?: number
  checkinAddress?: string
  checkoutAddress?: string
  checkinWifiMac?: string
  checkoutWifiMac?: string
  checkinDeviceId?: string
  checkoutDeviceId?: string
  checkinIp?: string
  checkoutIp?: string
  checkinMethod?: number
  checkoutMethod?: number
  status: number
  lateMinutes?: number
  leaveEarlyMinutes?: number
  overtimeMinutes?: number
  workHours?: number
  shouldWorkHours?: number
  abnormal: number
  abnormalReason?: string
  approvalId?: number
  remark?: string
  createTime: string
  updateTime: string
}

export interface AttendanceRule {
  id: number
  ruleName: string
  ruleCode: string
  status: number
  workStartTime: string
  workEndTime: string
  allowLateMinutes: number
  allowLeaveEarlyMinutes: number
  overtimeRule: number
  overtimeStartTime?: string
  minOvertimeDuration?: number
  flexibleWork: number
  flexibleWorkHours?: number
  includeRestDays: number
  weekdays?: string
  excludeHolidays?: string
  specialWorkDays?: string
  deptIds?: string
  positionIds?: string
  remark?: string
  timezone: string
  createTime: string
  updateTime: string
}

export interface AttendanceGroup {
  id: number
  groupName: string
  groupCode: string
  status: number
  ruleId: number
  scheduleType: number
  scheduleData?: string
  managerId: string
  managerName: string
  deptIds?: string
  positionIds?: string
  userIds?: string
  checkinLocations?: string
  wifiList?: string
  allowRemote: number
  maxRemoteDistance?: number
  remark?: string
  createTime: string
  updateTime: string
}

export interface AttendanceException {
  id: number
  userId: string
  userName: string
  exceptionType: number // 0-Leave, 1-Business trip, 2-Overtime, 3-Other
  startTime: string
  endTime: string
  reason: string
  attachments?: string
  status: number // 0-Pending, 1-Approved, 2-Rejected
  approverId?: string
  approverName?: string
  approveTime?: string
  approveComment?: string
  processed: number // 0-Not processed, 1-Processed
  createTime: string
  updateTime: string
}

export interface AttendanceStat {
  id: number
  userId: string
  userName: string
  statDate: string
  statType: number // 0-Daily, 1-Weekly, 2-Monthly, 3-Quarterly, 4-Yearly
  totalDays: number
  workDays: number
  normalDays: number
  lateDays: number
  leaveEarlyDays: number
  absentDays: number
  overtimeDays: number
  totalLateMinutes: number
  totalLeaveEarlyMinutes: number
  totalOvertimeMinutes: number
  totalWorkHours: number
  attendanceScore: number
  normalRate: number
  createTime: string
  updateTime: string
}

// API functions
export const checkin = (data: CheckinDTO) => {
  return request.post('/api/attendance/checkin', data)
}

export const getTodayAttendance = (userId: string) => {
  return request.get('/api/attendance/today', { params: { userId } })
}

export const hasCheckedInToday = (userId: string) => {
  return request.get('/api/attendance/hasCheckedIn', { params: { userId } })
}

export const getAttendanceRecords = (params: AttendanceQueryDTO) => {
  return request.post('/api/attendance/list', params)
}

export const getAttendanceSummary = (userId: string, startDate?: string, endDate?: string) => {
  return request.get('/api/attendance/summary', { 
    params: { userId, startDate, endDate } 
  })
}

export const getMonthlyReport = (userId: string, year: number, month: number) => {
  return request.get('/api/attendance/monthlyReport', { 
    params: { userId, year, month } 
  })
}

export const getDepartmentReport = (deptId: string, startDate?: string, endDate?: string) => {
  return request.get('/api/attendance/departmentReport', { 
    params: { deptId, startDate, endDate } 
  })
}

export const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number) => {
  return request.get('/api/attendance/calculateDistance', { 
    params: { lat1, lon1, lat2, lon2 } 
  })
}

export const autoCheckout = () => {
  return request.post('/api/attendance/autoCheckout')
}

// Attendance Rule APIs
export const getAttendanceRules = (params: any) => {
  return request.post('/api/attendance/rules/list', params)
}

export const getAttendanceRule = (id: number) => {
  return request.get(`/api/attendance/rules/${id}`)
}

export const createAttendanceRule = (data: any) => {
  return request.post('/api/attendance/rules', data)
}

export const updateAttendanceRule = (id: number, data: any) => {
  return request.put(`/api/attendance/rules/${id}`, data)
}

export const deleteAttendanceRule = (id: number) => {
  return request.delete(`/api/attendance/rules/${id}`)
}

// Attendance Group APIs
export const getAttendanceGroups = (params: any) => {
  return request.post('/api/attendance/groups/list', params)
}

export const getAttendanceGroup = (id: number) => {
  return request.get(`/api/attendance/groups/${id}`)
}

export const createAttendanceGroup = (data: any) => {
  return request.post('/api/attendance/groups', data)
}

export const updateAttendanceGroup = (id: number, data: any) => {
  return request.put(`/api/attendance/groups/${id}`, data)
}

export const deleteAttendanceGroup = (id: number) => {
  return request.delete(`/api/attendance/groups/${id}`)
}

// Attendance Exception APIs
export const applyException = (data: any) => {
  return request.post('/api/attendance/exceptions/apply', data)
}

export const getExceptions = (params: any) => {
  return request.post('/api/attendance/exceptions/list', params)
}

export const getException = (id: number) => {
  return request.get(`/api/attendance/exceptions/${id}`)
}

export const approveException = (id: number, data: any) => {
  return request.post(`/api/attendance/exceptions/${id}/approve`, data)
}

export const deleteException = (id: number) => {
  return request.delete(`/api/attendance/exceptions/${id}`)
}

// Statistics APIs
export const getAttendanceStatistics = (params: any) => {
  return request.post('/api/attendance/statistics', params)
}

export const getDepartmentStatistics = (deptId: string, params: any) => {
  return request.post(`/api/attendance/statistics/department/${deptId}`, params)
}

export const getCompanyStatistics = (params: any) => {
  return request.post('/api/attendance/statistics/company', params)
}

export const getAttendanceRanking = (params: any) => {
  return request.post('/api/attendance/statistics/ranking', params)
}

export const getAttendanceTrends = (userId: string, params: any) => {
  return request.post(`/api/attendance/statistics/trends/${userId}`, params)
}

export const getAbnormalAnalysis = (deptId: string, params: any) => {
  return request.post(`/api/attendance/statistics/abnormal/${deptId}`, params)
}

// Export APIs
export const exportAttendanceRecords = (params: any) => {
  return request.post('/api/attendance/export/records', params, {
    responseType: 'blob'
  })
}

export const exportStatisticsReport = (params: any) => {
  return request.post('/api/attendance/export/statistics', params, {
    responseType: 'blob'
  })
}

// Update record
export const updateAttendanceRecord = (data: any) => {
  return request.put(`/api/attendance/records/${data.id}`, data)
}

// Utility functions
export const getStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '正常',
    1: '迟到',
    2: '早退',
    3: '缺勤',
    4: '加班',
    5: '请假',
    6: '出差'
  }
  return statusMap[status] || '未知'
}

export const getStatusColor = (status: number): string => {
  const colorMap: Record<number, string> = {
    0: 'success',
    1: 'warning',
    2: 'warning',
    3: 'danger',
    4: 'info',
    5: 'info',
    6: 'info'
  }
  return colorMap[status] || 'info'
}

export const getMethodText = (method: number): string => {
  const methodMap: Record<number, string> = {
    0: 'GPS',
    1: 'WiFi',
    2: '手动',
    3: '远程'
  }
  return methodMap[method] || '未知'
}

export const getMethodColor = (method: number): string => {
  const colorMap: Record<number, string> = {
    0: 'success',
    1: 'info',
    2: 'warning',
    3: 'primary'
  }
  return colorMap[method] || 'info'
}

export default {
  checkin,
  getTodayAttendance,
  hasCheckedInToday,
  getAttendanceRecords,
  getAttendanceSummary,
  getMonthlyReport,
  getDepartmentReport,
  calculateDistance,
  autoCheckout,
  getAttendanceRules,
  getAttendanceRule,
  createAttendanceRule,
  updateAttendanceRule,
  deleteAttendanceRule,
  getAttendanceGroups,
  getAttendanceGroup,
  createAttendanceGroup,
  updateAttendanceGroup,
  deleteAttendanceGroup,
  applyException,
  getExceptions,
  getException,
  approveException,
  deleteException,
  getAttendanceStatistics,
  getDepartmentStatistics,
  getCompanyStatistics,
  getAttendanceRanking,
  getAttendanceTrends,
  getAbnormalAnalysis,
  exportAttendanceRecords,
  exportStatisticsReport,
  updateAttendanceRecord,
  getStatusText,
  getStatusColor,
  getMethodText,
  getMethodColor
}