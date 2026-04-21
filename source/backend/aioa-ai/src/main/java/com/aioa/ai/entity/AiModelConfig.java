package com.aioa.ai.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI Model Configuration Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model_config")
public class AiModelConfig extends BaseEntity {
    
    /**
     * Model code (gpt-4o, claude-3.5, kimi-pro)
     */
    private String modelCode;
    
    /**
     * Model name
     */
    private String modelName;
    
    /**
     * Provider (openai, anthropic, moonshot)
     */
    private String provider;
    
    /**
     * API endpoint
     */
    private String endpoint;
    
    /**
     * API Key (encrypted)
     */
    private String apiKey;
    
    /**
     * Default functions: CHAT, REPORT, IMAGE
     */
    private String defaultFor;
    
    /**
     * Enabled: 0-disabled, 1-enabled
     */
    private Integer enabled;
    
    /**
     * Daily limit (-1 for unlimited)
     */
    private Integer dailyLimit;
    
    /**
     * Today usage count
     */
    private Integer todayUsage;
    
    /**
     * Model type: gpt4, claude, kimi
     */
    private String modelType;
    
    /**
     * Sort order
     */
    private Integer sortOrder;
    
    /**
     * Remark
     */
    private String remark;
}
