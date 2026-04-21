package com.aioa.ai.service;

import com.aioa.ai.entity.AiModelConfig;

import java.util.List;

/**
 * AI Model Configuration Service
 */
public interface AiModelConfigService {
    
    /**
     * Get all AI model configurations
     */
    List<AiModelConfig> getAllConfigs();
    
    /**
     * Get enabled AI model configurations
     */
    List<AiModelConfig> getEnabledConfigs();
    
    /**
     * Get AI model configuration by code
     */
    AiModelConfig getConfigByCode(String modelCode);
    
    /**
     * Get AI model configurations by function type
     */
    List<AiModelConfig> getConfigsByFunction(String functionType);
    
    /**
     * Save or update AI model configuration
     */
    AiModelConfig saveConfig(AiModelConfig config);
    
    /**
     * Toggle AI model enabled status
     */
    boolean toggleModelEnabled(String modelCode);
    
    /**
     * Check user quota for AI model
     */
    boolean checkUserQuota(String userId, String modelCode);
    
    /**
     * Increment usage count for AI model
     */
    void incrementUsage(String modelCode);
    
    /**
     * Get default model for a function type
     */
    AiModelConfig getDefaultConfig(String functionType);
}