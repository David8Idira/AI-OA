// API配置
export interface ApiConfig {
  baseUrl: string
  timeout: number
  enableLog: boolean
}

// 后端服务地址（根据部署环境修改）
// 开发环境: http://localhost:8080 (本机启动的后端)
// Docker部署: http://localhost:30080 (通过nginx反向代理)
// K8s部署: http://aioa-gateway.aioa-system.svc.cluster.local:8080
const BACKEND_BASE_URL = 'https://dev-api.example.com'

// 开发环境配置
export const devConfig: ApiConfig = {
  baseUrl: 'https://dev-api.example.com',
  timeout: 30000,
  enableLog: true
}

// 测试环境配置
export const testConfig: ApiConfig = {
  baseUrl: 'https://test-api.example.com',
  timeout: 30000,
  enableLog: true
}

// 生产环境配置
export const prodConfig: ApiConfig = {
  baseUrl: 'https://api.example.com',
  timeout: 30000,
  enableLog: false
}

// 根据环境导出配置
export function getApiConfig(): ApiConfig {
  // TODO: 部署时根据实际环境修改
  // dev: 开发环境 (本机Docker或本地服务)
  // prod: 生产环境
  const env = AppStorage.get<string>('api_env') || 'dev'
  switch (env) {
    case 'dev':
      return devConfig
    case 'test':
      return testConfig
    case 'prod':
      return prodConfig
    default:
      return devConfig
  }
}

// API端点定义
export const API_ENDPOINTS = {
  // 认证
  LOGIN: '/api/auth/login',
  LOGOUT: '/api/auth/logout',
  REFRESH_TOKEN: '/api/auth/refresh',
  
  // 用户
  USER_INFO: '/api/user/info',
  USER_LIST: '/api/user/list',
  UPDATE_PASSWORD: '/api/user/password',
  
  // 审批
  APPROVAL_LIST: '/api/approval/list',
  APPROVAL_DETAIL: '/api/approval/detail',
  APPROVAL_CREATE: '/api/approval/create',
  APPROVAL_SUBMIT: '/api/approval/submit',
  APPROVAL_PASS: '/api/approval/pass',
  APPROVAL_REJECT: '/api/approval/reject',
  
  // 消息
  MESSAGE_LIST: '/api/message/list',
  MESSAGE_DETAIL: '/api/message/detail',
  MESSAGE_READ: '/api/message/read',
  
  // 日程
  SCHEDULE_LIST: '/api/schedule/list',
  SCHEDULE_CREATE: '/api/schedule/create',
  SCHEDULE_UPDATE: '/api/schedule/update',
  SCHEDULE_DELETE: '/api/schedule/delete',
  
  // 考勤
  CHECKIN_RECORD: '/api/checkin/record',
  CHECKIN_SUBMIT: '/api/checkin/submit'
} as const

// 导出完整配置
export default {
  ...getApiConfig(),
  endpoints: API_ENDPOINTS
}
