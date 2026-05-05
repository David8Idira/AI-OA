/**
 * AI Service - AI对话服务
 */
import { ChatMessage } from '../model/ai/ChatModel';

export class AiService {
  private static instance: AiService = new AiService();

  static getInstance(): AiService {
    return this.instance;
  }

  /**
   * 发送消息给AI
   */
  sendMessage(content: string, modelId: string = 'gpt-4o'): Promise<string> {
    return new Promise((resolve, reject) => {
      // 模拟请求延迟
      setTimeout(() => {
        // 根据内容生成模拟回复
        const reply = this.generateReply(content);
        resolve(reply);
      }, 1500);
    });
  }

  /**
   * 生成AI回复
   */
  private generateReply(question: string): string {
    if (question.includes('报销')) {
      return `**本月报销数据汇总：**

- **报销总额**：¥45,680.00
- **报销笔数**：32笔
- **平均单笔**：¥1,427.50
- **待审批**：5笔

相比上月，报销总额增长了 8%。`;
    }
    if (question.includes('月报') || question.includes('报告')) {
      return `**月度工作报告**

**工作完成情况：**
1. 完成项目需求开发 12 项
2. 修复 Bug 23 个
3. 参与技术评审 4 次

**下月计划：**
1. 推进 AI-OA 项目前端开发
2. 完成知识库模块集成`;
    }
    if (question.includes('休假') || question.includes('请假')) {
      return `**公司休假政策：**

1. **年假**：入职满1年享5天
2. **病假**：每月2天带薪
3. **事假**：需提前3天申请

详细请参考《员工手册》第3.2章节`;
    }
    return `收到您的问题，以下是相关回答：

这个问题涉及到公司制度和流程，建议您参考知识库中的相关文档获取详细信息。`;
  }

  /**
   * 获取配额信息
   */
  getQuotaInfo(): Promise<{ used: number, total: number }> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ used: 156, total: 1000 });
      }, 500);
    });
  }

  /**
   * 获取会话历史
   */
  getChatHistory(chatId: string): Promise<ChatMessage[]> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([]);
      }, 500);
    });
  }

  /**
   * 创建新会话
   */
  createChat(): Promise<string> {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve('chat-' + Date.now());
      }, 300);
    });
  }
}

export const aiService = AiService.getInstance();