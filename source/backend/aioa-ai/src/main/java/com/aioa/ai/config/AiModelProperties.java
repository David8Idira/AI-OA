package com.aioa.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI模型配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "aioa.ai")
public class AiModelProperties {
    
    /**
     * 默认模型
     */
    private String defaultModel = "mimo-v2-flash";
    
    /**
     * API配置 - Mimo
     */
    private MimoConfig mimo = new MimoConfig();
    
    /**
     * API配置 - OpenAI
     */
    private OpenAIConfig openai = new OpenAIConfig();
    
    /**
     * API配置 - Claude
     */
    private ClaudeConfig claude = new ClaudeConfig();
    
    @Data
    public static class MimoConfig {
        private boolean enabled = true;
        private String apiKey = "tp-cl8gb60kvi9ieqdkwk7gfopb579cem3jw75dum8sfom2egu8"; // 测试key
        private String endpoint = "https://api.minimax.chat/v1/text/chatcompletion_pro";
        private String model = "mimo-v2-flash";
        private double temperature = 0.7;
        private int maxTokens = 2048;
    }
    
    @Data
    public static class OpenAIConfig {
        private boolean enabled = false;
        private String apiKey = "";
        private String endpoint = "https://api.openai.com/v1/chat/completions";
        private String model = "gpt-4o";
        private double temperature = 0.7;
        private int maxTokens = 2048;
    }
    
    @Data
    public static class ClaudeConfig {
        private boolean enabled = false;
        private String apiKey = "";
        private String endpoint = "https://api.anthropic.com/v1/messages";
        private String model = "claude-3-5-sonnet-20241022";
        private double temperature = 0.7;
        private int maxTokens = 2048;
    }
}