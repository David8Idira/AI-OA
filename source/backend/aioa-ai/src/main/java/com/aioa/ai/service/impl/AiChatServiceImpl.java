package com.aioa.ai.service.impl;

import com.aioa.ai.dto.ChatRequestDTO;
import com.aioa.ai.dto.ChatResponseDTO;
import com.aioa.ai.service.AiChatService;
import com.aioa.ai.service.AiQuotaService;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI聊天服务实现
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {
    
    @Autowired
    private AiQuotaService aiQuotaService;
    
    // 模拟的对话历史存储
    private final ConcurrentHashMap<String, List<Map<String, String>>> conversationHistory = new ConcurrentHashMap<>();
    
    // 模型配置 - 生产环境应从数据库读取
    private static final Map<String, ModelConfig> MODEL_CONFIGS = new HashMap<>();
    static {
        MODEL_CONFIGS.put("gpt-4o", new ModelConfig("https://api.openai.com/v1/chat/completions", "gpt-4o"));
        MODEL_CONFIGS.put("kimi-pro", new ModelConfig("https://api.moonshot.cn/v1/chat/completions", "moonshot/kimi-pro-128k"));
        MODEL_CONFIGS.put("claude-3.5", new ModelConfig("https://api.anthropic.com/v1/messages", "claude-3-5-sonnet-20241022"));
    }
    
    @Override
    public ChatResponseDTO chat(ChatRequestDTO request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 参数校验
            validateRequest(request);
            
            // 1.5 检查配额
            String userId = "default";
            if (!aiQuotaService.checkQuota(userId, request.getModelCode())) {
                throw new BusinessException(ResultCode.AI_MODEL_QUOTA_EXCEEDED, "AI配额已用尽，请明天再试");
            }
            
            // 2. 获取模型配置
            String modelCode = request.getModelCode();
            ModelConfig config = MODEL_CONFIGS.get(modelCode);
            if (config == null) {
                // 默认使用GPT
                config = MODEL_CONFIGS.get("gpt-4o");
                modelCode = "gpt-4o";
            }
            
            // 3. 构建请求
            Map<String, Object> apiRequest = buildApiRequest(request);
            
            // 4. 调用AI模型 (这里使用模拟响应，实际应调用API)
            String reply = callAiModel(config, apiRequest);
            
            // 5. 保存对话历史
            saveConversation(request.getConversationId(), request.getMessage(), reply);
            
            // 6. 使用配额
            int tokens = estimateTokens(reply);
            aiQuotaService.useQuota(userId, modelCode, tokens);
            
            // 7. 构建响应
            ChatResponseDTO response = new ChatResponseDTO();
            response.setConversationId(request.getConversationId());
            response.setReply(reply);
            response.setModelCode(modelCode);
            response.setTokens(tokens);
            response.setTimeUsed(System.currentTimeMillis() - startTime);
            
            return response;
            
        } catch (Exception e) {
            log.error("AI聊天失败", e);
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE, "AI服务调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 校验请求参数
     */
    private void validateRequest(ChatRequestDTO request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "消息不能为空");
        }
    }
    
    /**
     * 构建API请求
     */
    private Map<String, Object> buildApiRequest(ChatRequestDTO request) {
        Map<String, Object> apiRequest = new HashMap<>();
        apiRequest.put("model", request.getModelCode());
        apiRequest.put("temperature", request.getTemperature());
        apiRequest.put("max_tokens", request.getMaxTokens());
        
        // 构建消息列表
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        
        // 添加系统提示词
        if (request.getSystemPrompt() != null) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", request.getSystemPrompt());
            messages.add(systemMsg);
        }
        
        // 添加历史消息
        String convId = request.getConversationId();
        if (convId != null && conversationHistory.containsKey(convId)) {
            messages.addAll(conversationHistory.get(convId));
        }
        
        // 添加用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", request.getMessage());
        messages.add(userMsg);
        
        apiRequest.put("messages", messages);
        return apiRequest;
    }
    
    /**
     * 调用AI模型
     * 生产环境应使用OkHttp/RestTemplate等调用真实API
     */
    private String callAiModel(ModelConfig config, Map<String, Object> apiRequest) {
        // TODO: 实现真实的API调用
        // 这里返回模拟响应
        String userMessage = (String) ((List<Map<String, String>>) apiRequest.get("messages"))
            .get(((List<Map<String, String>>) apiRequest.get("messages")).size() - 1).get("content");
        
        return getSimulatedResponse(userMessage);
    }
    
    /**
     * 模拟响应
     */
    private String getSimulatedResponse(String message) {
        return "【AI回复】感谢您的消息: \"" + message + "\"。\n\n" +
               "这是一个模拟的AI回复。\n" +
               "在实际环境中，这里会调用 " + 
               "OpenAI/Claude/Moonshot 等API返回真正的智能回复。\n\n" +
               "请在 .env 文件中配置 API_KEY 来启用真实AI功能。";
    }
    
    /**
     * 保存对话历史
     */
    private void saveConversation(String conversationId, String userMessage, String aiMessage) {
        if (conversationId == null) return;
        
        List<Map<String, String>> history = conversationHistory.computeIfAbsent(conversationId, k -> new java.util.ArrayList<>());
        
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        history.add(userMsg);
        
        Map<String, String> aiMsg = new HashMap<>();
        aiMsg.put("role", "assistant");
        aiMsg.put("content", aiMessage);
        history.add(aiMsg);
        
        // 保留最近N条
        int maxHistory = 20;
        while (history.size() > maxHistory) {
            history.remove(0);
            history.remove(0);
        }
    }
    
    /**
     * 估算Token数
     */
    private int estimateTokens(String text) {
        // 简单估算: 1个中文 ≈ 2个Token, 1个英文 ≈ 1.3个Token
        return text.length() / 2;
    }
    
    /**
     * 模型配置
     */
    private static class ModelConfig {
        String endpoint;
        String model;
        
        ModelConfig(String endpoint, String model) {
            this.endpoint = endpoint;
            this.model = model;
        }
    }
    
    @Override
    public Map<String, Object> getUserQuota(String userId) {
        return aiQuotaService.getUserQuota(userId);
    }
}