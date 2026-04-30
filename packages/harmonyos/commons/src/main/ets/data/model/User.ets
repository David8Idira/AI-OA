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
