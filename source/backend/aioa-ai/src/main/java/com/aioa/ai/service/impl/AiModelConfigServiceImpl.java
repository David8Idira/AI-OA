package com.aioa.ai.service.impl;

import com.aioa.ai.entity.AiModelConfig;
import com.aioa.ai.mapper.AiModelConfigMapper;
import com.aioa.ai.service.AiModelConfigService;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Model Configuration Service Implementation
 */
@Slf4j
@Service
public class AiModelConfigServiceImpl implements AiModelConfigService {
    
    @Autowired
    private AiModelConfigMapper aiModelConfigMapper;
    
    @Override
    public List<AiModelConfig> getAllConfigs() {
        log.debug("Getting all AI model configurations");
        return aiModelConfigMapper.selectList(null);
    }
    
    @Override
    public List<AiModelConfig> getEnabledConfigs() {
        log.debug("Getting enabled AI model configurations");
        LambdaQueryWrapper<AiModelConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiModelConfig::getEnabled, 1);
        return aiModelConfigMapper.selectList(queryWrapper);
    }
    
    @Override
    public AiModelConfig getConfigByCode(String modelCode) {
        log.debug("Getting AI model configuration: {}", modelCode);
        
        if (modelCode == null || modelCode.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Model code cannot be empty");
        }
        
        LambdaQueryWrapper<AiModelConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiModelConfig::getModelCode, modelCode);
        
        AiModelConfig config = aiModelConfigMapper.selectOne(queryWrapper);
        if (config == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "AI model config not found: " + modelCode);
        }
        
        if (config.getEnabled() != 1) {
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, "AI model is disabled: " + modelCode);
        }
        
        return config;
    }
    
    @Override
    public List<AiModelConfig> getConfigsByFunction(String functionType) {
        log.debug("Getting AI model configurations for function: {}", functionType);
        
        if (functionType == null || functionType.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Function type cannot be empty");
        }
        
        LambdaQueryWrapper<AiModelConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiModelConfig::getDefaultFor, functionType)
                   .eq(AiModelConfig::getEnabled, 1);
        
        List<AiModelConfig> configs = aiModelConfigMapper.selectList(queryWrapper);
        if (configs.isEmpty()) {
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, "No enabled AI models available");
        }
        
        return configs;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiModelConfig saveConfig(AiModelConfig config) {
        log.debug("Saving AI model configuration: {}", config.getModelCode());
        
        if (config.getModelCode() == null || config.getModelCode().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Invalid AI model config");
        }
        
        try {
            if (config.getId() == null) {
                // New config
                int inserted = aiModelConfigMapper.insert(config);
                if (inserted <= 0) {
                    throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to insert AI model config");
                }
            } else {
                // Update existing config
                config.setUpdateTime(LocalDateTime.now());
                int updated = aiModelConfigMapper.updateById(config);
                if (updated <= 0) {
                    throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to update AI model config");
                }
            }
            
            log.info("AI model configuration saved: {}", config.getModelCode());
            return config;
        } catch (Exception e) {
            log.error("Error saving AI model configuration: {}", config.getModelCode(), e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to save AI model config: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleModelEnabled(String modelCode) {
        log.info("Toggling AI model: {}", modelCode);
        
        try {
            LambdaQueryWrapper<AiModelConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AiModelConfig::getModelCode, modelCode);
            
            AiModelConfig config = aiModelConfigMapper.selectOne(queryWrapper);
            if (config == null) {
                log.error("AI model not found: {}", modelCode);
                return false;
            }
            
            Integer currentEnabled = config.getEnabled();
            Integer newEnabled = (currentEnabled == 1) ? 0 : 1;
            config.setEnabled(newEnabled);
            config.setUpdateTime(LocalDateTime.now());
            
            int updated = aiModelConfigMapper.updateById(config);
            if (updated <= 0) {
                log.error("Failed to toggle AI model: {}", modelCode);
                return false;
            }
            
            log.info("AI model {} toggled to: {}", modelCode, newEnabled == 1 ? "enabled" : "disabled");
            return true;
        } catch (Exception e) {
            log.error("Error toggling AI model: {}", modelCode, e);
            return false;
        }
    }
    
    @Override
    public boolean checkUserQuota(String userId, String modelCode) {
        log.debug("Checking user quota: user={}, model={}", userId, modelCode);
        // This should check actual user quota
        // For now, return true (quota available)
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementUsage(String modelCode) {
        log.debug("Incrementing usage for model: {}", modelCode);
        
        try {
            // For now, just log the usage increment
            // In a real implementation, this would update a usage count field
            log.info("Usage incremented for model: {}", modelCode);
        } catch (Exception e) {
            log.error("Error incrementing usage for model: {}", modelCode, e);
            // Don't throw exception to avoid breaking main flow
        }
    }
    
    @Override
    public AiModelConfig getDefaultConfig(String functionType) {
        log.debug("Getting default config for function: {}", functionType);
        
        List<AiModelConfig> configs = getConfigsByFunction(functionType);
        if (configs.isEmpty()) {
            return null;
        }
        
        // Return first enabled config for the function
        return configs.get(0);
    }
}
