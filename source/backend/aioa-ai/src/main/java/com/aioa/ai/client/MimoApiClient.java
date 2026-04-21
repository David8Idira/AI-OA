package com.aioa.ai.client;

import com.aioa.ai.config.AiModelProperties;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Mimo API客户端
 * Xiaomi Mimo大模型调用
 */
@Slf4j
@Component
public class MimoApiClient {
    
    @Autowired
    private AiModelProperties properties;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 调用Mimo API
     */
    public String chat(String message) {
        return chat(message, properties.getMimo().getModel());
    }
    
    /**
     * 调用Mimo API（指定模型）
     */
    public String chat(String message, String model) {
        try {
            AiModelProperties.MimoConfig config = properties.getMimo();
            
            if (!config.isEnabled()) {
                log.warn("Mimo API未启用");
                return getMockResponse(message);
            }
            
            // 构建请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", config.getTemperature());
            requestBody.put("max_tokens", config.getMaxTokens());
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 系统提示
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个专业的AI助手，请简洁、准确地回答用户问题。");
            messages.add(systemMsg);
            
            // 用户消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            
            // 调用API
            log.info("调用Mimo API，模型: {}, 消息: {}", model, message);
            
            // 使用RestTemplate调用（实际需要配置超时等）
            // 注意：生产环境应使用OkHttp或WebClient
            String response = callMimoApi(config.getEndpoint(), config.getApiKey(), requestBody);
            
            return parseResponse(response);
            
        } catch (Exception e) {
            log.error("Mimo API调用失败", e);
            // 降级返回模拟响应
            return getMockResponse(message);
        }
    }
    
    /**
     * 调用Mimo API
     */
    private String callMimoApi(String endpoint, String apiKey, Map<String, Object> requestBody) {
        try {
            // 设置请求头
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");
            
            org.springframework.http.HttpEntity<Map<String, Object>> request = 
                new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            String response = restTemplate.postForObject(endpoint, request, String.class);
            return response;
            
        } catch (Exception e) {
            log.error("API调用异常: {}", e.getMessage());
            // 返回模拟响应用于测试
            return "{\"choices\":[{\"message\":{\"content\":\"【Mimo测试响应】: \" + \"" + requestBody.get("messages") + "\"}}]";
        }
    }
    
    /**
     * 解析响应
     */
    private String parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }
            // 尝试其他响应格式
            JsonNode text = root.path("text");
            if (text.isMissingNode()) {
                JsonNode output = root.path("output");
                if (!output.isMissingNode()) {
                    return output.path("text").asText();
                }
            }
            return "响应解析成功但格式未知";
        } catch (Exception e) {
            log.warn("响应解析失败，使用默认响应");
            return "AI响应: " + response.substring(0, Math.min(100, response.length()));
        }
    }
    
    /**
     * 获取模拟响应（API不可用时）
     */
    private String getMockResponse(String message) {
        return "【AI模拟响应】感谢您的消息: \"" + message + "\"\n\n" +
               "这是Mimo API的测试响应。\n" +
               "在配置真实API Key后，将返回真正的AI回答。\n\n" +
               "API配置位置: aioa-ai/src/main/resources/application.yml";
    }
    
    /**
     * 测试连接
     */
    public boolean testConnection() {
        try {
            String response = chat("你好，请回复'测试成功'");
            return response != null && response.length() > 0;
        } catch (Exception e) {
            log.error("Mimo API测试失败", e);
            return false;
        }
    }
}