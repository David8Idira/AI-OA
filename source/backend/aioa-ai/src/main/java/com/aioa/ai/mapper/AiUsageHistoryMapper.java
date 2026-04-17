package com.aioa.ai.mapper;

import com.aioa.ai.entity.AiUsageHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Usage History Mapper
 */
@Mapper
public interface AiUsageHistoryMapper extends BaseMapper<AiUsageHistory> {
    
    /**
     * Get usage history by user ID
     */
    @Select("SELECT * FROM ai_usage_history WHERE user_id = #{userId} ORDER BY request_time DESC LIMIT #{limit}")
    List<AiUsageHistory> selectByUser(@Param("userId") String userId, @Param("limit") int limit);
    
    /**
     * Get usage history by model code
     */
    @Select("SELECT * FROM ai_usage_history WHERE model_code = #{modelCode} ORDER BY request_time DESC LIMIT #{limit}")
    List<AiUsageHistory> selectByModel(@Param("modelCode") String modelCode, @Param("limit") int limit);
    
    /**
     * Get usage history by date range
     */
    @Select("SELECT * FROM ai_usage_history WHERE request_time >= #{startDate} AND request_time < #{endDate} ORDER BY request_time DESC")
    List<AiUsageHistory> selectByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get daily usage statistics
     */
    @Select("SELECT DATE(request_time) as date, model_code, " +
            "COUNT(*) as request_count, " +
            "SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END) as success_count, " +
            "SUM(prompt_tokens) as total_prompt_tokens, " +
            "SUM(completion_tokens) as total_completion_tokens, " +
            "SUM(total_tokens) as total_tokens, " +
            "SUM(cost_usd) as total_cost, " +
            "AVG(duration_ms) as avg_duration " +
            "FROM ai_usage_history " +
            "WHERE request_time >= #{startDate} AND request_time < #{endDate} " +
            "GROUP BY DATE(request_time), model_code " +
            "ORDER BY date DESC")
    List<DailyUsageStats> getDailyUsageStats(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get user usage summary
     */
    @Select("SELECT user_id, " +
            "COUNT(*) as total_requests, " +
            "SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END) as success_requests, " +
            "SUM(total_tokens) as total_tokens, " +
            "SUM(cost_usd) as total_cost " +
            "FROM ai_usage_history " +
            "WHERE request_time >= #{startDate} AND request_time < #{endDate} " +
            "GROUP BY user_id " +
            "ORDER BY total_tokens DESC")
    List<UserUsageSummary> getUserUsageSummary(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get model cost statistics
     */
    @Select("SELECT model_code, " +
            "COUNT(*) as request_count, " +
            "SUM(total_tokens) as total_tokens, " +
            "SUM(cost_usd) as total_cost, " +
            "AVG(cost_usd) as avg_cost_per_request, " +
            "SUM(cost_usd) / NULLIF(SUM(total_tokens), 0) as avg_cost_per_token " +
            "FROM ai_usage_history " +
            "WHERE success = 1 AND cost_usd > 0 " +
            "GROUP BY model_code " +
            "ORDER BY total_cost DESC")
    List<ModelCostStats> getModelCostStats();
    
    /**
     * Get failed requests
     */
    @Select("SELECT * FROM ai_usage_history WHERE success = 0 ORDER BY request_time DESC LIMIT #{limit}")
    List<AiUsageHistory> getFailedRequests(@Param("limit") int limit);
    
    /**
     * Get slow requests (duration > threshold)
     */
    @Select("SELECT * FROM ai_usage_history WHERE duration_ms > #{thresholdMs} AND success = 1 ORDER BY duration_ms DESC LIMIT #{limit}")
    List<AiUsageHistory> getSlowRequests(@Param("thresholdMs") int thresholdMs, @Param("limit") int limit);
    
    /**
     * Get today's usage
     */
    @Select("SELECT * FROM ai_usage_history WHERE DATE(request_time) = CURDATE() ORDER BY request_time DESC")
    List<AiUsageHistory> getTodayUsage();
    
    /**
     * Clean old records (older than specified days)
     */
    @Select("DELETE FROM ai_usage_history WHERE request_time < #{cutoffDate}")
    int cleanOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Statistics DTO interfaces
    
    interface DailyUsageStats {
        LocalDate getDate();
        String getModelCode();
        Long getRequestCount();
        Long getSuccessCount();
        Long getTotalPromptTokens();
        Long getTotalCompletionTokens();
        Long getTotalTokens();
        BigDecimal getTotalCost();
        Double getAvgDuration();
    }
    
    interface UserUsageSummary {
        String getUserId();
        Long getTotalRequests();
        Long getSuccessRequests();
        Long getTotalTokens();
        BigDecimal getTotalCost();
    }
    
    interface ModelCostStats {
        String getModelCode();
        Long getRequestCount();
        Long getTotalTokens();
        BigDecimal getTotalCost();
        BigDecimal getAvgCostPerRequest();
        BigDecimal getAvgCostPerToken();
    }
}