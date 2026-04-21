package com.aioa.ai.controller;

import com.aioa.ai.entity.AiUsageHistory;
import com.aioa.ai.service.AiUsageHistoryService;
import com.aioa.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI Usage History Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/usage")
public class AiUsageHistoryController {
    
    @Autowired
    private AiUsageHistoryService aiUsageHistoryService;
    
    @PostMapping("/record")
    public Result<Object> recordUsage(@RequestBody AiUsageHistory usage) {
        log.debug("Recording AI usage: user={}, model={}", usage.getUserId(), usage.getModelCode());
        aiUsageHistoryService.recordUsage(usage);
        return Result.success("AI usage recorded successfully");
    }
    
    @GetMapping("/user/{userId}")
    public Result<List<AiUsageHistory>> getUserHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "20") int limit) {
        log.debug("Getting usage history for user: {}, limit: {}", userId, limit);
        List<AiUsageHistory> history = aiUsageHistoryService.getUserHistory(userId, limit);
        return Result.success("User usage history retrieved successfully", history);
    }
    
    @GetMapping("/model/{modelCode}")
    public Result<List<AiUsageHistory>> getModelHistory(
            @PathVariable String modelCode,
            @RequestParam(defaultValue = "20") int limit) {
        log.debug("Getting usage history for model: {}, limit: {}", modelCode, limit);
        List<AiUsageHistory> history = aiUsageHistoryService.getModelHistory(modelCode, limit);
        return Result.success("Model usage history retrieved successfully", history);
    }
    
    @GetMapping("/daily-stats")
    public Result<List<Map<String, Object>>> getDailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("Getting daily stats from {} to {}", startDate, endDate);
        List<Map<String, Object>> stats = aiUsageHistoryService.getDailyStats(startDate, endDate);
        return Result.success("Daily usage statistics retrieved successfully", stats);
    }
    
    @GetMapping("/user-summary/{userId}")
    public Result<Map<String, Object>> getUserSummary(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("Getting user summary for: {}, from {} to {}", userId, startDate, endDate);
        Map<String, Object> summary = aiUsageHistoryService.getUserSummary(userId, startDate, endDate);
        return Result.success("User usage summary retrieved successfully", summary);
    }
    
    @GetMapping("/model-cost-stats")
    public Result<List<Map<String, Object>>> getModelCostStats() {
        log.debug("Getting model cost statistics");
        List<Map<String, Object>> stats = aiUsageHistoryService.getModelCostStats();
        return Result.success("Model cost statistics retrieved successfully", stats);
    }
    
    @GetMapping("/failed")
    public Result<List<AiUsageHistory>> getFailedRequests(
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Getting failed requests, limit: {}", limit);
        List<AiUsageHistory> failed = aiUsageHistoryService.getFailedRequests(limit);
        return Result.success("Failed requests retrieved successfully", failed);
    }
    
    @GetMapping("/slow")
    public Result<List<AiUsageHistory>> getSlowRequests(
            @RequestParam(defaultValue = "5000") int thresholdMs,
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("Getting slow requests (>{}ms), limit: {}", thresholdMs, limit);
        List<AiUsageHistory> slow = aiUsageHistoryService.getSlowRequests(thresholdMs, limit);
        return Result.success("Slow requests retrieved successfully", slow);
    }
    
    @GetMapping("/today")
    public Result<List<AiUsageHistory>> getTodayUsage() {
        log.debug("Getting today's usage");
        List<AiUsageHistory> today = aiUsageHistoryService.getTodayUsage();
        return Result.success("Today's usage retrieved successfully", today);
    }
    
    @PostMapping("/clean-old/{daysToKeep}")
    public Result<Object> cleanOldRecords(@PathVariable int daysToKeep) {
        log.info("Cleaning old records older than {} days", daysToKeep);
        
        if (daysToKeep < 7) {
            return Result.error("Days to keep must be at least 7 for data retention");
        }
        
        int deleted = aiUsageHistoryService.cleanOldRecords(daysToKeep);
        return Result.success("Cleaned " + deleted + " old records");
    }
    
    @GetMapping("/system-stats")
    public Result<Map<String, Object>> getSystemStats() {
        log.debug("Getting system-wide usage statistics");
        try {
            // This method is implemented in AiUsageHistoryServiceImpl
            // We'll need to cast the service to get access to this method
            // For now, return a simplified response
            Map<String, Object> stats = Map.of(
                "message", "System stats available in service implementation",
                "suggestion", "Direct service call recommended"
            );
            return Result.success("System statistics placeholder", stats);
        } catch (Exception e) {
            log.error("Error getting system stats", e);
            return Result.error("Failed to get system statistics: " + e.getMessage());
        }
    }
}