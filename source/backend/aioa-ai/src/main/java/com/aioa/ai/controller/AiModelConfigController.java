package com.aioa.ai.controller;

import com.aioa.ai.entity.AiModelConfig;
import com.aioa.ai.service.AiModelConfigService;
import com.aioa.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI Model Config Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/config")
public class AiModelConfigController {
    
    @Autowired
    private AiModelConfigService aiModelConfigService;
    
    @GetMapping("/list")
    public Result<Object> getAllConfigs() {
        log.debug("Getting all AI model configurations");
        List<AiModelConfig> configs = aiModelConfigService.getAllConfigs();
        return Result.success("AI model configurations retrieved successfully", configs);
    }
    
    @GetMapping("/enabled")
    public Result<Object> getEnabledConfigs() {
        log.debug("Getting enabled AI model configurations");
        List<AiModelConfig> configs = aiModelConfigService.getEnabledConfigs();
        return Result.success("Enabled AI model configurations retrieved successfully", configs);
    }
    
    @GetMapping("/{modelCode}")
    public Result<Object> getConfigByCode(@PathVariable String modelCode) {
        log.debug("Getting AI model configuration: {}", modelCode);
        AiModelConfig config = aiModelConfigService.getConfigByCode(modelCode);
        if (config == null) {
            return Result.error("AI model configuration not found: " + modelCode);
        }
        return Result.success("AI model configuration retrieved successfully", config);
    }
    
    @GetMapping("/function/{functionType}")
    public Result<Object> getConfigsByFunction(@PathVariable String functionType) {
        log.debug("Getting AI model configurations for function: {}", functionType);
        List<AiModelConfig> configs = aiModelConfigService.getConfigsByFunction(functionType);
        return Result.success("AI model configurations for function retrieved successfully", configs);
    }
    
    @PostMapping
    public Result<Object> saveConfig(@RequestBody AiModelConfig config) {
        log.debug("Saving AI model configuration: {}", config.getModelCode());
        try {
            AiModelConfig saved = aiModelConfigService.saveConfig(config);
            return Result.success("AI model configuration saved successfully", saved);
        } catch (Exception e) {
            log.error("Error saving AI model configuration: {}", config.getModelCode(), e);
            return Result.error("Failed to save AI model configuration: " + e.getMessage());
        }
    }
    
    @PutMapping("/toggle/{modelCode}")
    public Result<Object> toggleModel(@PathVariable String modelCode) {
        log.debug("Toggling AI model: {}", modelCode);
        try {
            boolean success = aiModelConfigService.toggleModelEnabled(modelCode);
            if (success) {
                return Result.success("AI model toggled successfully");
            } else {
                return Result.error("AI model not found: " + modelCode);
            }
        } catch (Exception e) {
            log.error("Error toggling AI model: {}", modelCode, e);
            return Result.error("Failed to toggle AI model: " + e.getMessage());
        }
    }
    
    @GetMapping("/check-quota/{modelCode}/{userId}")
    public Result<Object> checkQuota(@PathVariable String modelCode, @PathVariable String userId) {
        log.debug("Checking quota for model: {}, user: {}", modelCode, userId);
        try {
            boolean hasQuota = aiModelConfigService.checkUserQuota(userId, modelCode);
            if (hasQuota) {
                return Result.success("User has quota available");
            } else {
                return Result.error("User quota exceeded or not available");
            }
        } catch (Exception e) {
            log.error("Error checking quota: model={}, user={}", modelCode, userId, e);
            return Result.error("Failed to check quota: " + e.getMessage());
        }
    }
}
