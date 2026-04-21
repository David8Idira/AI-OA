package com.aioa.ai.service.impl;

import com.aioa.ai.entity.AiUsageHistory;
import com.aioa.ai.mapper.AiUsageHistoryMapper;
import com.aioa.ai.service.AiUsageHistoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Usage History Service Implementation
 */
@Slf4j
@Service
public class AiUsageHistoryServiceImpl implements AiUsageHistoryService {
    
    @Autowired
    private AiUsageHistoryMapper aiUsageHistoryMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordUsage(AiUsageHistory usage) {
        log.debug("Recording AI usage: user={}, model={}, tokens={}", 
            usage.getUserId(), usage.getModelCode(), usage.getTotalTokens());
        
        try {
            // Ensure required fields
            if (usage.getRequestTime() == null) {
                usage.setRequestTime(LocalDateTime.now());
            }
            
            if (usage.getSuccess() == null) {
                usage.setSuccess(1); // Default to success
            }
            
            // Calculate total tokens if not provided
            if (usage.getTotalTokens() == null) {
                int prompt = usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;
                int completion = usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
                usage.setTotalTokens(prompt + completion);
            }
            
            // Calculate duration if response time is set
            if (usage.getResponseTime() != null) {
                usage.calculateDuration();
            }
            
            // Calculate derived fields
            usage.calculateDerivedFields();
            
            // Insert into database
            int inserted = aiUsageHistoryMapper.insert(usage);
            if (inserted <= 0) {
                log.error("Failed to record AI usage for user: {}, model: {}", 
                    usage.getUserId(), usage.getModelCode());
            } else {
                log.info("Recorded AI usage: id={}, user={}, model={}, tokens={}, cost={}", 
                    usage.getId(), usage.getUserId(), usage.getModelCode(), 
                    usage.getTotalTokens(), usage.getFormattedCost());
            }
        } catch (Exception e) {
            log.error("Error recording AI usage for user: {}, model: {}", 
                usage.getUserId(), usage.getModelCode(), e);
            // Don't throw exception to avoid breaking main flow
        }
    }
    
    @Override
    public List<AiUsageHistory> getUserHistory(String userId, int limit) {
        log.debug("Getting usage history for user: {}, limit: {}", userId, limit);
        
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Invalid user ID");
            return new ArrayList<>();
        }
        
        try {
            List<AiUsageHistory> history = aiUsageHistoryMapper.selectByUser(userId, Math.min(limit, 100));
            
            // Calculate derived fields for each record
            history.forEach(AiUsageHistory::calculateDerivedFields);
            
            log.debug("Found {} usage records for user: {}", history.size(), userId);
            return history;
        } catch (Exception e) {
            log.error("Error getting usage history for user: {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<AiUsageHistory> getModelHistory(String modelCode, int limit) {
        log.debug("Getting usage history for model: {}, limit: {}", modelCode, limit);
        
        if (modelCode == null || modelCode.trim().isEmpty()) {
            log.warn("Invalid model code");
            return new ArrayList<>();
        }
        
        try {
            List<AiUsageHistory> history = aiUsageHistoryMapper.selectByModel(modelCode, Math.min(limit, 100));
            
            // Calculate derived fields for each record
            history.forEach(AiUsageHistory::calculateDerivedFields);
            
            log.debug("Found {} usage records for model: {}", history.size(), modelCode);
            return history;
        } catch (Exception e) {
            log.error("Error getting usage history for model: {}", modelCode, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getDailyStats(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting daily stats from {} to {}", startDate, endDate);
        
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            log.warn("Invalid date range");
            return new ArrayList<>();
        }
        
        try {
            List<AiUsageHistoryMapper.DailyUsageStats> stats = 
                aiUsageHistoryMapper.getDailyUsageStats(startDate, endDate);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (AiUsageHistoryMapper.DailyUsageStats stat : stats) {
                Map<String, Object> statMap = new HashMap<>();
                statMap.put("date", stat.getDate());
                statMap.put("modelCode", stat.getModelCode());
                statMap.put("requestCount", stat.getRequestCount());
                statMap.put("successCount", stat.getSuccessCount());
                statMap.put("successRate", stat.getRequestCount() > 0 ? 
                    String.format("%.1f%%", (double) stat.getSuccessCount() / stat.getRequestCount() * 100) : "0.0%");
                statMap.put("totalPromptTokens", stat.getTotalPromptTokens());
                statMap.put("totalCompletionTokens", stat.getTotalCompletionTokens());
                statMap.put("totalTokens", stat.getTotalTokens());
                statMap.put("totalCost", stat.getTotalCost() != null ? 
                    stat.getTotalCost().setScale(6, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                statMap.put("avgDuration", stat.getAvgDuration() != null ? 
                    String.format("%.0fms", stat.getAvgDuration()) : "N/A");
                
                result.add(statMap);
            }
            
            log.debug("Found {} daily stats records", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error getting daily stats", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> getUserSummary(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting user summary for: {}, from {} to {}", userId, startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("period", Map.of("start", startDate, "end", endDate));
        
        try {
            // Get user's usage history for the period
            List<AiUsageHistory> history = aiUsageHistoryMapper.selectByDateRange(startDate, endDate);
            history = history.stream()
                .filter(h -> userId.equals(h.getUserId()))
                .toList();
            
            // Calculate summary
            int totalRequests = history.size();
            int successRequests = (int) history.stream().filter(AiUsageHistory::isSuccessful).count();
            int totalTokens = history.stream()
                .mapToInt(h -> h.getTotalTokens() != null ? h.getTotalTokens() : 0)
                .sum();
            BigDecimal totalCost = history.stream()
                .filter(AiUsageHistory::hasCost)
                .map(AiUsageHistory::getCostUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate by model
            Map<String, Map<String, Object>> byModel = new HashMap<>();
            for (AiUsageHistory h : history) {
                String modelCode = h.getModelCode();
                Map<String, Object> modelStats = byModel.computeIfAbsent(modelCode, k -> new HashMap<>());
                
                modelStats.put("requests", (int) modelStats.getOrDefault("requests", 0) + 1);
                modelStats.put("tokens", (int) modelStats.getOrDefault("tokens", 0) + 
                    (h.getTotalTokens() != null ? h.getTotalTokens() : 0));
                if (h.hasCost()) {
                    BigDecimal currentCost = (BigDecimal) modelStats.getOrDefault("cost", BigDecimal.ZERO);
                    modelStats.put("cost", currentCost.add(h.getCostUsd()));
                }
            }
            
            // Prepare result
            result.put("totalRequests", totalRequests);
            result.put("successRequests", successRequests);
            result.put("successRate", totalRequests > 0 ? 
                String.format("%.1f%%", (double) successRequests / totalRequests * 100) : "0.0%");
            result.put("totalTokens", totalTokens);
            result.put("totalCost", totalCost.setScale(6, RoundingMode.HALF_UP));
            result.put("usageByModel", byModel);
            
            // Average cost per token
            if (totalTokens > 0 && totalCost.compareTo(BigDecimal.ZERO) > 0) {
                result.put("avgCostPerToken", totalCost.divide(
                    BigDecimal.valueOf(totalTokens), 8, RoundingMode.HALF_UP));
            }
            
            log.debug("User summary calculated: {} requests, {} tokens", totalRequests, totalTokens);
        } catch (Exception e) {
            log.error("Error calculating user summary for: {}", userId, e);
            result.put("error", "Failed to calculate summary");
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getModelCostStats() {
        log.debug("Getting model cost statistics");
        
        try {
            List<AiUsageHistoryMapper.ModelCostStats> stats = aiUsageHistoryMapper.getModelCostStats();
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (AiUsageHistoryMapper.ModelCostStats stat : stats) {
                Map<String, Object> statMap = new HashMap<>();
                statMap.put("modelCode", stat.getModelCode());
                statMap.put("requestCount", stat.getRequestCount());
                statMap.put("totalTokens", stat.getTotalTokens());
                statMap.put("totalCost", stat.getTotalCost() != null ? 
                    stat.getTotalCost().setScale(6, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                statMap.put("avgCostPerRequest", stat.getAvgCostPerRequest() != null ? 
                    stat.getAvgCostPerRequest().setScale(6, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                statMap.put("avgCostPerToken", stat.getAvgCostPerToken() != null ? 
                    stat.getAvgCostPerToken().setScale(8, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                
                result.add(statMap);
            }
            
            log.debug("Found {} model cost statistics", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error getting model cost statistics", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<AiUsageHistory> getFailedRequests(int limit) {
        log.debug("Getting failed requests, limit: {}", limit);
        
        try {
            List<AiUsageHistory> failed = aiUsageHistoryMapper.getFailedRequests(Math.min(limit, 50));
            failed.forEach(AiUsageHistory::calculateDerivedFields);
            
            log.debug("Found {} failed requests", failed.size());
            return failed;
        } catch (Exception e) {
            log.error("Error getting failed requests", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<AiUsageHistory> getSlowRequests(int thresholdMs, int limit) {
        log.debug("Getting slow requests (>{}ms), limit: {}", thresholdMs, limit);
        
        try {
            List<AiUsageHistory> slow = aiUsageHistoryMapper.getSlowRequests(thresholdMs, Math.min(limit, 50));
            slow.forEach(AiUsageHistory::calculateDerivedFields);
            
            log.debug("Found {} slow requests", slow.size());
            return slow;
        } catch (Exception e) {
            log.error("Error getting slow requests", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<AiUsageHistory> getTodayUsage() {
        log.debug("Getting today's usage");
        
        try {
            List<AiUsageHistory> today = aiUsageHistoryMapper.getTodayUsage();
            today.forEach(AiUsageHistory::calculateDerivedFields);
            
            log.debug("Found {} usage records for today", today.size());
            return today;
        } catch (Exception e) {
            log.error("Error getting today's usage", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanOldRecords(int daysToKeep) {
        log.info("Cleaning old records older than {} days", daysToKeep);
        
        if (daysToKeep < 1) {
            log.warn("Invalid days to keep: {}", daysToKeep);
            return 0;
        }
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            int deleted = aiUsageHistoryMapper.cleanOldRecords(cutoffDate);
            
            log.info("Cleaned {} old records older than {}", deleted, cutoffDate);
            return deleted;
        } catch (Exception e) {
            log.error("Error cleaning old records", e);
            return 0;
        }
    }
    
    /**
     * Scheduled task to clean old records (keep 90 days)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void scheduledCleanOldRecords() {
        log.info("Starting scheduled cleanup of old AI usage records");
        int deleted = cleanOldRecords(90); // Keep 90 days
        log.info("Scheduled cleanup completed, deleted {} records", deleted);
    }
    
    /**
     * Get system-wide usage statistics
     */
    public Map<String, Object> getSystemStats() {
        log.debug("Getting system-wide usage statistics");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Today's date range
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            
            // Get today's usage
            List<AiUsageHistory> todayUsage = getTodayUsage();
            
            // Calculate today's stats
            int todayRequests = todayUsage.size();
            int todaySuccess = (int) todayUsage.stream().filter(AiUsageHistory::isSuccessful).count();
            int todayTokens = todayUsage.stream()
                .mapToInt(h -> h.getTotalTokens() != null ? h.getTotalTokens() : 0)
                .sum();
            BigDecimal todayCost = todayUsage.stream()
                .filter(AiUsageHistory::hasCost)
                .map(AiUsageHistory::getCostUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Get last 7 days stats
            LocalDateTime weekStart = todayStart.minusDays(7);
            List<Map<String, Object>> weeklyStats = getDailyStats(weekStart, todayEnd);
            
            // Get total records count
            LambdaQueryWrapper<AiUsageHistory> queryWrapper = new LambdaQueryWrapper<>();
            long totalRecords = aiUsageHistoryMapper.selectCount(queryWrapper);
            
            // Prepare result
            result.put("today", Map.of(
                "requests", todayRequests,
                "successRate", todayRequests > 0 ? 
                    String.format("%.1f%%", (double) todaySuccess / todayRequests * 100) : "0.0%",
                "tokens", todayTokens,
                "cost", todayCost.setScale(6, RoundingMode.HALF_UP)
            ));
            
            result.put("weeklyStats", weeklyStats);
            result.put("totalRecords", totalRecords);
            result.put("lastUpdated", LocalDateTime.now());
            
            log.debug("System stats calculated: {} requests today, {} total records", todayRequests, totalRecords);
        } catch (Exception e) {
            log.error("Error getting system stats", e);
            result.put("error", "Failed to calculate system statistics");
        }
        
        return result;
    }
}