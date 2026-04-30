// 应用常量
export class Constants {
  // API配置
  static readonly API_BASE_URL = 'https://api.example.com'
  static readonly API_TIMEOUT = 30000

  // 存储Keys
  static readonly STORAGE_KEY_TOKEN = 'auth_token'
  static readonly STORAGE_KEY_USER_ID = 'user_id'
  static readonly STORAGE_KEY_USERNAME = 'username'
  static readonly STORAGE_KEY_USER_INFO = 'user_info'

  // 路由路径
  static readonly ROUTE_LOGIN = 'pages/Login'
  static readonly ROUTE_MAIN = 'pages/Main'
  static readonly ROUTE_HOME = 'pages/Home'
  static readonly ROUTE_APPROVALS = 'pages/Approvals'
  static readonly ROUTE_MESSAGES = 'pages/Messages'
  static readonly ROUTE_PROFILE = 'pages/Profile'

  // 主题颜色
  static readonly COLOR_PRIMARY = '#1890FF'
  static readonly COLOR_SUCCESS = '#52C41A'
  static readonly COLOR_WARNING = '#FAAD14'
  static readonly COLOR_ERROR = '#FF4D4F'
  static readonly COLOR_INFO = '#1890FF'

  // 背景颜色
  static readonly BG_COLOR_PAGE = '#F5F5F5'
  static readonly BG_COLOR_CARD = '#FFFFFF'

  // 字体颜色
  static readonly TEXT_COLOR_PRIMARY = '#333333'
  static readonly TEXT_COLOR_SECONDARY = '#666666'
  static readonly TEXT_COLOR_TERTIARY = '#999999'

  // 边框圆角
  static readonly BORDER_RADIUS_SM = 4
  static readonly BORDER_RADIUS_MD = 8
  static readonly BORDER_RADIUS_LG = 12
  static readonly BORDER_RADIUS_XL = 24
}

// HTTP状态码
export enum HttpCode {
  OK = 200,
  CREATED = 201,
  NO_CONTENT = 204,
  BAD_REQUEST = 400,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  INTERNAL_SERVER_ERROR = 500
}

// 业务错误码
export enum BusinessCode {
  SUCCESS = 0,
  TOKEN_EXPIRED = 40101,
  TOKEN_INVALID = 40102,
  PERMISSION_DENIED = 40301,
  RESOURCE_NOT_FOUND = 40401,
  PARAMETER_ERROR = 40001,
  SYSTEM_ERROR = 50001
}
