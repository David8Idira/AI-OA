package com.aioa.ai.controller;

import com.aioa.ai.service.AiQuotaService;
import com.aioa.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI Quota Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/quota")
public class AiQuotaController {
    
    @Autowired
    private AiQuotaService aiQuotaService;
    
    @GetMapping("/check/{userId}/{modelCode}")
    public Result<Object> checkQuota(@PathVariable String userId, @PathVariable String modelCode) {
        log.debug("Checking quota for user: {}, model: {}", userId, modelCode);
        boolean hasQuota = aiQuotaService.checkQuota(userId, modelCode);
        if (hasQuota) {
            return Result.<Object>success("User has quota available");
        } else {
            return Result.<Object>error("User quota exceeded or not available");
        }
    }
    
    @PostMapping("/use/{userId}/{modelCode}/{tokens}")
    public Result<Object> useQuota(
            @PathVariable String userId,
            @PathVariable String modelCode,
            @PathVariable int tokens) {
        log.debug("Using quota: user={}, model={}, tokens={}", userId, modelCode, tokens);
        
        if (tokens <= 0) {
            return Result.<Object>error("Token count must be positive");
        }
        
        boolean success = aiQuotaService.useQuota(userId, modelCode, tokens);
        if (success) {
            return Result.<Object>success(tokens + " tokens used successfully");
        } else {
            return Result.<Object>error("Failed to use quota: insufficient quota or quota exceeded");
        }
    }
    
    @GetMapping("/user/{userId}")
    public Result<Object> getUserQuota(@PathVariable String userId) {
        log.debug("Getting quota for user: {}", userId);
        Map<String, Object> quotaInfo = aiQuotaService.getUserQuota(userId);
        return Result.<Object>success("User quota information retrieved successfully", quotaInfo);
    }
    
    @PostMapping("/reset-daily")
    public Result<Object> resetDailyQuota() {
        log.info("Resetting daily quota for all users");
        aiQuotaService.resetDailyQuota();
        return Result.<Object>success("Daily quota reset successfully");
    }
}
