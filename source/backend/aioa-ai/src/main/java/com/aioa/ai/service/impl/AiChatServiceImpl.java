package com.aioa.ai.service.impl;

import com.aioa.ai.client.MimoApiClient;
import com.aioa.ai.config.AiModelProperties;
import com.aioa.ai.dto.ChatRequestDTO;
import com.aioa.ai.dto.ChatResponseDTO;
import com.aioa.ai.entity.AiModelConfig;
import com.aioa.ai.service.AiChatService;
import com.aioa.ai.service.AiModelConfigService;
import com.aioa.ai.service.AiModelDispatcher;
import com.aioa.ai.service.AiQuotaService;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI聊天服务实现
 * 整合真实的AI API调用（MiniMax Mimo、OpenAI、Claude、Moonshot等）
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {
    
    @Autowired
    private AiQuotaService aiQuotaService;
    
    @Autowired
    private AiModelDispatcher aiModelDispatcher;
    
    @Autowired
    private MimoApiClient mimoApiClient;
    
    @Autowired
    private AiModelConfigService aiModelConfigService;
    
    @Autowired
    private AiModelProperties aiModelProperties;
    
    // 对话历史存储（生产环境应使用Redis）
    private final ConcurrentHashMap<String, List<Map<String, String>>> conversationHistory = new ConcurrentHashMap<>();
    
    // 模型代码到提供商的映射
    private static final Map<String, String> MODEL_PROVIDER_MAP = new HashMap<>();
    static {
        // OpenAI 系列
        MODEL_PROVIDER_MAP.put("gpt-4o", "openai");
        MODEL_PROVIDER_MAP.put("gpt-4o-mini", "openai");
        MODEL_PROVIDER_MAP.put("gpt-3.5-turbo", "openai");
        
        // Anthropic Claude
        MODEL_PROVIDER_MAP.put("claude-3.5", "anthropic");
        MODEL_PROVIDER_MAP.put("claude-3-sonnet", "anthropic");
        MODEL_PROVIDER_MAP.put("claude-3-opus", "anthropic");
        
        // Moonshot Kimi
        MODEL_PROVIDER_MAP.put("kimi-pro", "moonshot");
        MODEL_PROVIDER_MAP.put("moonshot-v1", "moonshot");
        
        // MiniMax Mimo
        MODEL_PROVIDER_MAP.put("mimo-v2-flash", "minimax");
        MODEL_PROVIDER_MAP.put("mimo-v2", "minimax");
    }
    
    @Override
    public ChatResponseDTO chat(ChatRequestDTO request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 参数校验
            validateRequest(request);
            
            // 1.5 检查配额
            String userId = resolveUserId(request);
            String modelCode = resolveModelCode(request);
            
            if (!aiQuotaService.checkQuota(userId, modelCode)) {
                throw new BusinessException(ResultCode.AI_MODEL_QUOTA_EXCEEDED, "AI配额已用尽，请明天再试");
            }
            
            // 2. 获取提供商类型
            String provider = resolveProvider(modelCode);
            
            // 3. 调用AI模型
            String reply;
            if ("minimax".equalsIgnoreCase(provider)) {
                // 使用 Mimo 客户端
                reply = callMimoModel(request, modelCode);
            } else {
                // 使用 AiModelDispatcher 调用其他模型（OpenAI/Claude/Moonshot）
                reply = callDispatcherModel(modelCode, request.getMessage());
            }
            
            // 4. 保存对话历史
            saveConversation(request.getConversationId(), request.getMessage(), reply, request.getSystemPrompt());
            
            // 5. 使用配额
            int tokens = estimateTokens(reply);
            aiQuotaService.useQuota(userId, modelCode, tokens);
            
            // 6. 构建响应
            ChatResponseDTO response = new ChatResponseDTO();
            response.setConversationId(request.getConversationId());
            response.setReply(reply);
            response.setModelCode(modelCode);
            response.setTokens(tokens);
            response.setTimeUsed(System.currentTimeMillis() - startTime);
            
            log.info("AI聊天完成 - 用户: {}, 模型: {}, Token: {}, 耗时: {}ms", 
                    userId, modelCode, tokens, response.getTimeUsed());
            
            return response;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI聊天失败", e);
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE, "AI服务调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析用户ID（生产环境应从SecurityContext或Token中获取）
     */
    private String resolveUserId(ChatRequestDTO request) {
        // TODO: 从Spring Security Context或JWT Token中获取真实用户ID
        if (request.getConversationId() != null && !request.getConversationId().isEmpty()) {
            return "user_" + request.getConversationId();
        }
        return "default";
    }
    
    /**
     * 解析模型代码
     */
    private String resolveModelCode(ChatRequestDTO request) {
        if (request.getModelCode() != null && !request.getModelCode().trim().isEmpty()) {
            return request.getModelCode();
        }
        // 使用默认模型
        return aiModelProperties.getDefaultModel();
    }
    
    /**
     * 解析提供商类型
     */
    private String resolveProvider(String modelCode) {
        // 先从数据库配置获取
        AiModelConfig config = aiModelConfigService.getConfigByCode(modelCode);
        if (config != null && config.getProvider() != null) {
            return config.getProvider();
        }
        
        // 从静态映射获取
        return MODEL_PROVIDER_MAP.getOrDefault(modelCode, "openai");
    }
    
    /**
     * 调用 Mimo 模型（MiniMax）
     */
    private String callMimoModel(ChatRequestDTO request, String modelCode) {
        // 构建完整的消息（包含系统提示和历史）
        StringBuilder fullMessage = new StringBuilder();
        
        if (request.getSystemPrompt() != null) {
            fullMessage.append("[系统提示]: ").append(request.getSystemPrompt()).append("\n\n");
        }
        
        // 添加历史对话
        String convId = request.getConversationId();
        if (convId != null && conversationHistory.containsKey(convId)) {
            List<Map<String, String>> history = conversationHistory.get(convId);
            for (Map<String, String> msg : history) {
                String role = msg.get("role");
                String content = msg.get("content");
                if ("user".equals(role)) {
                    fullMessage.append("[用户]: ").append(content).append("\n");
                } else if ("assistant".equals(role)) {
                    fullMessage.append("[AI]: ").append(content).append("\n");
                }
            }
        }
        
        fullMessage.append("[用户当前问题]: ").append(request.getMessage());
        
        return mimoApiClient.chat(fullMessage.toString(), modelCode);
    }
    
    /**
     * 通过 AiModelDispatcher 调用其他模型
     */
    private String callDispatcherModel(String modelCode, String message) {
        try {
            return aiModelDispatcher.chat(modelCode, message);
        } catch (Exception e) {
            log.error("AiModelDispatcher调用失败，模型: {}", modelCode, e);
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE, 
                    "模型 " + modelCode + " 调用失败: " + e.getMessage());
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
     * 保存对话历史
     */
    private void saveConversation(String conversationId, String userMessage, String aiMessage, String systemPrompt) {
        if (conversationId == null) return;
        
        List<Map<String, String>> history = conversationHistory.computeIfAbsent(conversationId, k -> new java.util.ArrayList<>());
        
        // 如果是新对话，先添加系统提示
        if (history.isEmpty() && systemPrompt != null) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            history.add(systemMsg);
        }
        
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        history.add(userMsg);
        
        Map<String, String> aiMsg = new HashMap<>();
        aiMsg.put("role", "assistant");
        aiMsg.put("content", aiMessage);
        history.add(aiMsg);
        
        // 保留最近N条对话（防止内存溢出）
        int maxHistory = 40; // 20轮对话
        while (history.size() > maxHistory) {
            // 移除最早的轮次（system prompt保留）
            boolean hasSystemPrompt = !history.isEmpty() && "system".equals(history.get(0).get("role"));
            int startIndex = hasSystemPrompt ? 1 : 0;
            if (history.size() > startIndex) {
                history.remove(startIndex);
                history.remove(startIndex);
            } else {
                break;
            }
        }
    }
    
    /**
     * 估算Token数
     */
    private int estimateTokens(String text) {
        if (text == null) return 0;
        // 估算: 1个中文 ≈ 2个Token, 1个英文单词 ≈ 1.3个Token
        int chineseChars = 0;
        int englishChars = 0;
        for (char c : text.toCharArray()) {
            if (c >= 0x4e00 && c <= 0x9fa5) {
                chineseChars++;
            } else {
                englishChars++;
            }
        }
        return (chineseChars * 2) + (englishChars / 4); // 粗略估算
    }
    
    @Override
    public Map<String, Object> getUserQuota(String userId) {
        return aiQuotaService.getUserQuota(userId);
    }
    
    @Override
    public List<Map<String, String>> getAvailableModels() {
        // 从数据库获取启用的模型配置
        List<AiModelConfig> configs = aiModelConfigService.getEnabledConfigs();
        
        if (configs != null && !configs.isEmpty()) {
            return configs.stream()
                .map(config -> Map.of(
                    "code", config.getModelCode(),
                    "name", config.getModelName(),
                    "provider", config.getProvider()
                ))
                .toList();
        }
        
        // 如果数据库没有配置，返回默认列表
        return List.of(
            Map.of("code", "mimo-v2-flash", "name", "MiniMax Mimo", "provider", "MiniMax"),
            Map.of("code", "gpt-4o", "name", "GPT-4o", "provider", "OpenAI"),
            Map.of("code", "kimi-pro", "name", "Kimi Pro", "provider", "Moonshot"),
            Map.of("code", "claude-3.5", "name", "Claude 3.5", "provider", "Anthropic")
        );
    }
}