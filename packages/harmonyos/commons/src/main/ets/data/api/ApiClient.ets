import http from '@ohos.net.http'
import { ApiConfig } from '../../../../config/api-config'

// HTTP响应结果封装
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// API错误类型
export class ApiError extends Error {
  code: number
  response?: any

  constructor(code: number, message: string, response?: any) {
    super(message)
    this.code = code
    this.response = response
    this.name = 'ApiError'
  }
}

// ApiClient配置
interface ClientConfig {
  baseUrl: string
  timeout: number
}

// 请求选项
interface RequestOptions {
  url?: string
  method?: http.RequestMethod
  header?: Record<string, string>
  extraData?: any
  timeout?: number
}

// API客户端单例
export class ApiClient {
  private static instance: ApiClient
  private config: ClientConfig = {
    baseUrl: '',
    timeout: 30000
  }
  private context: any = null

  private constructor() {}

  // 初始化
  static init(config: ClientConfig): void {
    if (!ApiClient.instance) {
      ApiClient.instance = new ApiClient()
    }
    ApiClient.instance.config = { ...ApiClient.instance.config, ...config }
  }

  // 设置上下文
  static setContext(context: any): void {
    if (ApiClient.instance) {
      ApiClient.instance.context = context
    }
  }

  // 获取Token
  private getToken(): string | null {
    return AppStorage.get<string>('auth_token')
  }

  // 通用请求方法
  async request<T>(options: RequestOptions): Promise<ApiResponse<T>> {
    const config = this.config
    const token = this.getToken()

    // 构建完整URL
    const fullUrl = options.url?.startsWith('http') ? options.url : `${config.baseUrl}${options.url}`

    // 创建HTTP请求
    const httpRequest = http.createHttp()
    
    // 构建请求头
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...options.header
    }
    
    // 添加Token
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }

    try {
      const response = await httpRequest.request(
        fullUrl,
        {
          method: options.method || http.RequestMethod.GET,
          header: headers,
          extraData: options.extraData ? JSON.stringify(options.extraData) : undefined,
          connectTimeout: options.timeout || config.timeout,
          readTimeout: options.timeout || config.timeout
        }
      )

      // 解析响应
      const result = JSON.parse(response.result as string) as ApiResponse<T>

      // 检查业务错误
      if (result.code !== 0 && result.code !== 200) {
        throw new ApiError(result.code, result.message, result)
      }

      return result
    } catch (error) {
      if (error instanceof ApiError) {
        throw error
      }
      throw new ApiError(-1, error instanceof Error ? error.message : 'Network error', error)
    } finally {
      httpRequest.destroy()
    }
  }

  // GET请求
  async get<T>(url: string, params?: Record<string, any>, headers?: Record<string, string>): Promise<ApiResponse<T>> {
    let queryString = ''
    if (params) {
      queryString = '?' + Object.entries(params)
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
        .join('&')
    }
    return this.request<T>({
      url: url + queryString,
      method: http.RequestMethod.GET,
      header: headers
    })
  }

  // POST请求
  async post<T>(url: string, data?: any, headers?: Record<string, string>): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: http.RequestMethod.POST,
      extraData: data,
      header: headers
    })
  }

  // PUT请求
  async put<T>(url: string, data?: any, headers?: Record<string, string>): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: http.RequestMethod.PUT,
      extraData: data,
      header: headers
    })
  }

  // DELETE请求
  async delete<T>(url: string, headers?: Record<string, string>): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: http.RequestMethod.DELETE,
      header: headers
    })
  }
}

// 导出默认实例
export default ApiClient
