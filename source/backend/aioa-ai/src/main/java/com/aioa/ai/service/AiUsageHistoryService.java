package com.aioa.ai.service;

import com.aioa.ai.entity.AiUsageHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI Usage History Service
 */
public interface AiUsageHistoryService {
    
    /**
     * Record AI usage
     */
    void recordUsage(AiUsageHistory usage);
    
    /**
     * Get user's recent usage history
     */
    List<AiUsageHistory> getUserHistory(String userId, int limit);
    
    /**
     * Get model's recent usage history
     */
    List<AiUsageHistory> getModelHistory(String modelCode, int limit);
    
    /**
     * Get daily usage statistics
     */
    List<Map<String, Object>> getDailyStats(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get user usage summary
     */
    Map<String, Object> getUserSummary(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get model cost statistics
     */
    List<Map<String, Object>> getModelCostStats();
    
    /**
     * Get failed requests
     */
    List<AiUsageHistory> getFailedRequests(int limit);
    
    /**
     * Get slow requests
     */
    List<AiUsageHistory> getSlowRequests(int thresholdMs, int limit);
    
    /**
     * Get today's usage
     */
    List<AiUsageHistory> getTodayUsage();
    
    /**
     * Clean old records
     */
    int cleanOldRecords(int daysToKeep);
}