package com.aioa.ai.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aioa.ai.entity.AiModelConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI Model Dispatcher Service
 * Routes requests to different AI models based on configuration
 */
@Slf4j
@Service
public class AiModelDispatcher {

    @Autowired
    private AiModelConfigService configService;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Dispatch chat request to specified model
     */
    public String chat(String modelCode, String message) {
        var config = configService.getConfigByCode(modelCode);
        if (config == null || config.getEnabled() == 0) {
            // Fallback to default model
            config = configService.getDefaultConfig("CHAT");
        }

        return switch (config.getProvider().toLowerCase()) {
            case "openai" -> callOpenAI(config, message);
            case "anthropic" -> callAnthropic(config, message);
            case "moonshot" -> callMoonshot(config, message);
            default -> throw new RuntimeException("Unsupported provider: " + config.getProvider());
        };
    }

    /**
     * Call OpenAI API
     */
    private String callOpenAI(AiModelConfig config, String message) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelCode());
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", message)
            });

            Request request = new Request.Builder()
                .url(config.getEndpoint() + "/chat/completions")
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                    objectMapper.writeValueAsString(requestBody),
                    MediaType.parse("application/json")
                ))
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("OpenAI API error: {}", response);
                    throw new RuntimeException("OpenAI API error: " + response);
                }

                String body = response.body().string();
                JsonNode root = objectMapper.readTree(body);
                return root.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            throw new RuntimeException("AI request failed", e);
        }
    }

    /**
     * Call Anthropic Claude API
     */
    private String callAnthropic(AiModelConfig config, String message) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelCode());
            requestBody.put("max_tokens", 1024);
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", message)
            });

            Request request = new Request.Builder()
                .url(config.getEndpoint() + "/messages")
                .addHeader("x-api-key", config.getApiKey())
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                    objectMapper.writeValueAsString(requestBody),
                    MediaType.parse("application/json")
                ))
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Anthropic API error: {}", response);
                    throw new RuntimeException("Anthropic API error: " + response);
                }

                String body = response.body().string();
                JsonNode root = objectMapper.readTree(body);
                return root.path("content").get(0).path("text").asText();
            }
        } catch (Exception e) {
            log.error("Anthropic API call failed", e);
            throw new RuntimeException("AI request failed", e);
        }
    }

    /**
     * Call Moonshot Kimi API
     */
    private String callMoonshot(AiModelConfig config, String message) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelCode());
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", message)
            });

            Request request = new Request.Builder()
                .url(config.getEndpoint() + "/chat/completions")
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                    objectMapper.writeValueAsString(requestBody),
                    MediaType.parse("application/json")
                ))
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Moonshot API error: {}", response);
                    throw new RuntimeException("Moonshot API error: " + response);
                }

                String body = response.body().string();
                JsonNode root = objectMapper.readTree(body);
                return root.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.error("Moonshot API call failed", e);
            throw new RuntimeException("AI request failed", e);
        }
    }

    /**
     * Check if model is available and within quota
     */
    public boolean isModelAvailable(String modelCode) {
        var config = configService.getConfigByCode(modelCode);
        if (config == null || config.getEnabled() == 0) {
            return false;
        }
        if (config.getDailyLimit() > 0 && config.getTodayUsage() >= config.getDailyLimit()) {
            return false;
        }
        return true;
    }

    /**
     * Increment usage count
     */
    public void incrementUsage(String modelCode) {
        configService.incrementUsage(modelCode);
    }
}
