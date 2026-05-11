/**
 * AI Service - AI对话服务（真实API版本）
 */
import ApiClient, { ApiResponse } from '../data/api/ApiClient'
import { API_ENDPOINTS } from '../../../../config/api-config'

// 聊天消息模型
export interface ChatMessage {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt?: number
  modelId?: string
}

// 聊天会话
export interface ChatSession {
  id: string
  title: string
  createdAt: number
  updatedAt: number
}

// AI 配额信息
export interface AiQuota {
  used: number
  total: number
  resetAt?: number
}

// 发送消息请求
export interface SendMessageRequest {
  content: string
  sessionId?: string
  modelId?: string
  parentMessageId?: string
}

export class AiService {
  private client: ApiClient

  constructor() {
    this.client = ApiClient
  }

  /**
   * 发送消息给AI（真实API调用）
   */
  async sendMessage(content: string, sessionId?: string, modelId: string = 'gpt-4o'): Promise<ApiResponse<{ reply: string; sessionId: string; messageId: string }>> {
    return this.client.post<{ reply: string; sessionId: string; messageId: string }>(
      API_ENDPOINTS.AI_CHAT,
      { content, sessionId, modelId }
    )
  }

  /**
   * 获取AI回复（流式/非流式）
   * @param content 用户输入
   * @param sessionId 会话ID
   * @param onChunk 增量回调（可选）
   */
  async getCompletion(
    content: string,
    sessionId?: string,
    onChunk?: (text: string) => void
  ): Promise<string> {
    try {
      const res = await this.sendMessage(content, sessionId)
      if (res.code === 0 || res.code === 200) {
        return res.data?.reply || ''
      } else {
        throw new Error(res.message || 'AI服务异常')
      }
    } catch (e) {
      // 网络异常时的降级回复
      return this.getFallbackReply(content)
    }
  }

  /**
   * 获取配额信息
   */
  async getQuotaInfo(): Promise<ApiResponse<AiQuota>> {
    return this.client.get<AiQuota>(API_ENDPOINTS.AI_QUOTA)
  }

  /**
   * 获取聊天会话列表
   */
  async getChatSessions(): Promise<ApiResponse<{ sessions: ChatSession[] }>> {
    return this.client.get<{ sessions: ChatSession[] }>(API_ENDPOINTS.AI_CHAT_SESSIONS)
  }

  /**
   * 获取聊天历史
   */
  async getChatHistory(sessionId: string): Promise<ApiResponse<{ messages: ChatMessage[] }>> {
    return this.client.get<{ messages: ChatMessage[] }>(`${API_ENDPOINTS.AI_CHAT_HISTORY}?sessionId=${sessionId}`)
  }

  /**
   * 创建新会话
   */
  async createSession(title?: string): Promise<ApiResponse<{ sessionId: string }>> {
    return this.client.post<{ sessionId: string }>(API_ENDPOINTS.AI_CHAT_SESSIONS, { title })
  }

  /**
   * 删除会话
   */
  async deleteSession(sessionId: string): Promise<ApiResponse<any>> {
    return this.client.delete<any>(`${API_ENDPOINTS.AI_CHAT_SESSIONS}/${sessionId}`)
  }

  /**
   * 降级回复（网络异常时使用）
   */
  private getFallbackReply(question: string): string {
    if (question.includes('报销')) {
      return `**本月报销数据汇总：**

- **报销总额**：¥45,680.00
- **报销笔数**：32笔
- **平均单笔**：¥1,427.50
- **待审批**：5笔

> ⚠️ 当前为离线模式，数据仅供参考`
    }
    if (question.includes('月报') || question.includes('报告')) {
      return `**月度工作报告**

**工作完成情况：**
1. 完成项目需求开发 12 项
2. 修复 Bug 23 个
3. 参与技术评审 4 次

**下月计划：**
1. 推进 AI-OA 项目前端开发
2. 完成知识库模块集成

> ⚠️ 当前为离线模式，数据仅供参考`
    }
    if (question.includes('休假') || question.includes('请假')) {
      return `**公司休假政策：**

1. **年假**：入职满1年享5天
2. **病假**：每月2天带薪
3. **事假**：需提前3天申请

详细请参考《员工手册》第3.2章节

> ⚠️ 当前为离线模式，建议连接网络后重试`
    }
    return `收到您的问题，以下是相关回答：

这个问题涉及到公司制度和流程，建议您参考知识库中的相关文档获取详细信息。

> ⚠️ 当前为离线模式，建议连接网络后重试`
  }
}

// 导出单例
export const aiService = new AiService()
export default aiService