/**
 * AI Chat Data Models
 */
export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
  references?: Array<{
    id: string;
    title: string;
    url: string;
    source: string;
  }>;
}

export interface AIModel {
  id: string;
  name: string;
  provider: string;
  capability: string;
}

export const AI_MODEL_OPTIONS: AIModel[] = [
  { id: 'gpt-4o', name: 'GPT-4o', provider: 'OpenAI', capability: '通用' },
  { id: 'kimi-pro', name: 'Kimi Pro', provider: '月之暗面', capability: '长文本' },
  { id: 'claude-3.5', name: 'Claude 3.5', provider: 'Anthropic', capability: '推理' }
];