import request from '@/utils/request'

export interface LoginDTO {
  username: string
  password: string
}

export interface UserVO {
  id: string
  username: string
  nickname: string
  email: string
  mobile: string
  avatar: string
  deptId: string
  deptName: string
  position: string
  permissions: string[]
  token: string
  expiresIn: number
}

export const login = (data: LoginDTO) => {
  return request.post<UserVO>('/users/login', data)
}

export const register = (data: { username: string; password: string; nickname?: string }) => {
  return request.post('/users/register', data)
}

export const getCurrentUser = () => {
  return request.get<UserVO>('/users/current')
}

export const updatePassword = (data: { oldPassword: string; newPassword: string }) => {
  return request.put('/users/password', data)
}

export const getPermissions = () => {
  return request.get<string[]>('/users/permissions')
}

export const getMenus = () => {
  return request.get('/users/menus')
}
