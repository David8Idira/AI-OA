// HAR包导出入口
// 此文件用于导出HAR包的所有公共API

// 数据层
export { ApiClient, ApiResponse, ApiError } from '../../commons/src/main/ets/data/api/ApiClient'
export { User, LoginParams, LoginResponse, UserInfoResponse } from '../../commons/src/main/ets/data/model/User'

// 工具类
export { Constants, HttpCode, BusinessCode } from '../../commons/src/main/ets/utils/Constants'

// UI组件
export { HomeIndex } from '../../commons/src/main/ets/ui/home/HomeIndex'
export { LoginPage } from '../../commons/src/main/ets/ui/login/LoginPage'

// 版本信息
export const VERSION = '1.0.0'
export const NAME = '@ai-oa/harmonyos'
