/**
 * User Data Models
 */
export interface UserInfo {
  id: string;
  name: string;
  phone: string;
  email: string;
  avatar: string;
  department: string;
  position: string;
  status: number;
}

export interface LoginParams {
  username: string;
  password: string;
}

export interface AuthToken {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}