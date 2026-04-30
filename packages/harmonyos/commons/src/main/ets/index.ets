/**
 * AI-OA HarmonyOS 服务层统一导出
 */

// API客户端
export { ApiClient, ApiResponse, ApiError, default } from '../data/api/ApiClient'
export * from '../data/api/ApiClient'

// 服务类
export { default as ApprovalService } from './services/ApprovalService'
export { default as KnowledgeService } from './services/KnowledgeService'
export { default as AttendanceService } from './services/AttendanceService'
export { default as IMService } from './services/IMService'

// 数据模型
export * from './data/model/User'
export * from './data/model/Approval'
export * from './data/model/Message'
export * from './data/model/Attendance'
export * from './data/model/Knowledge'
export * from './data/model/Conversation'

// API端点常量
export * from './data/api/ApiClient'

// 考勤模型重新导出（包含枚举）
export {
  CheckInType,
  CheckInStatus,
  LeaveType,
  LeaveStatus,
  AttendanceRule,
  CheckInRecord,
  CheckInResult,
  TodayStatus,
  AttendanceSummary,
  LeaveRecord,
  ApplyLeaveParams,
  AttendanceStatistics
} from './services/AttendanceService'

// IM模型重新导出（包含枚举和接口）
export {
  ConversationType,
  Conversation,
  Contact,
  GroupMember,
  ReadStatus,
  Draft
} from './services/IMService'

// 知识库模型重新导出（包含枚举）
export {
  DocumentType,
  DocumentStatus,
  KnowledgeDocument,
  KnowledgeCategory,
  KnowledgeSearchResult,
  CreateDocumentParams,
  UpdateDocumentParams
} from './services/KnowledgeService'
