package com.aioa.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 向量化配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.embedding")
public class EmbeddingConfig {
    
    /**
     * OpenAI API配置
     */
    private OpenaiConfig openai = new OpenaiConfig();
    
    /**
     * 本地模型配置
     */
    private LocalModelConfig local = new LocalModelConfig();
    
    /**
     * 向量化模式：openai, local
     */
    private String mode = "openai";
    
    /**
     * 向量维度
     */
    private int dimension = 1536;
    
    /**
     * 批量处理大小
     */
    private int batchSize = 10;
    
    /**
     * 超时时间（秒）
     */
    private int timeoutSeconds = 30;
    
    @Data
    public static class OpenaiConfig {
        private String apiKey = "";
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "text-embedding-ada-002";
        private int maxTokens = 8191;
    }
    
    @Data
    public static class LocalModelConfig {
        private String modelPath = "";
        private String modelType = "bge-m3";
        private boolean gpuAccelerated = false;
        private int maxLength = 512;
    }
}