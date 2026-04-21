package com.aioa.ai.service;

import com.aioa.ai.dto.ChatRequestDTO;
import com.aioa.ai.dto.ChatResponseDTO;
import com.aioa.ai.entity.AiModelConfig;

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
     * 获取可用模型列表（从数据库配置获取）
     */
    List<Map<String, String>> getAvailableModels();
    
    /**
     * 获取用户配额信息
     */
    Map<String, Object> getUserQuota(String userId);
}