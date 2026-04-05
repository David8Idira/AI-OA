package com.aioa.ai.service;

import com.aioa.ai.entity.AiModelConfig;
import java.util.List;

/**
 * AI Model Configuration Service
 */
public interface AiModelConfigService {
    
    /**
     * Get all enabled models
     */
    List<AiModelConfig> getAllEnabled();
    
    /**
     * Get model config by code
     */
    AiModelConfig getConfigByCode(String modelCode);
    
    /**
     * Get default model for a function type
     */
    AiModelConfig getDefaultConfig(String functionType);
    
    /**
     * Increment usage count
     */
    void incrementUsage(String modelCode);
    
    /**
     * Save or update config
     */
    void saveConfig(AiModelConfig config);
    
    /**
     * Enable/disable model
     */
    void toggleModel(String modelCode, boolean enabled);
}
