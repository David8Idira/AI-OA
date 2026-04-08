package com.aioa.ai.service;

import com.aioa.ai.dto.ChatRequestDTO;
import com.aioa.ai.dto.ChatResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * AI聊天服务接口
 */
public interface AiChatService {
    
    /**
     * 发送聊天消息
     */
    ChatResponseDTO chat(ChatRequestDTO request);
    
    /**
     * 获取可用模型列表
     */
    default List<Map<String, String>> getAvailableModels() {
        return List.of(
            Map.of("code", "gpt-4o", "name", "GPT-4o", "provider", "OpenAI"),
            Map.of("code", "kimi-pro", "name", "Kimi Pro", "provider", "Moonshot"),
            Map.of("code", "claude-3.5", "name", "Claude 3.5", "provider", "Anthropic")
        );
    }
    
    /**
     * 获取用户配额信息
     */
    Map<String, Object> getUserQuota(String userId);
}