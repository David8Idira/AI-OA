package com.aioa.ai.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI Usage History Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_usage_history")
public class AiUsageHistory extends BaseEntity {
    
    /**
     * User ID
     */
    private String userId;
    
    /**
     * Model code
     */
    private String modelCode;
    
    /**
     * Prompt tokens
     */
    private Integer promptTokens;
    
    /**
     * Completion tokens
     */
    private Integer completionTokens;
    
    /**
     * Total tokens
     */
    private Integer totalTokens;
    
    /**
     * Cost in USD
     */
    private BigDecimal costUsd;
    
    /**
     * Request time
     */
    private LocalDateTime requestTime;
    
    /**
     * Response time
     */
    private LocalDateTime responseTime;
    
    /**
     * Request duration in milliseconds
     */
    private Integer durationMs;
    
    /**
     * Success: 0-failed, 1-success
     */
    private Integer success;
    
    /**
     * Error message
     */
    private String errorMessage;
    
    /**
     * API endpoint
     */
    private String apiEndpoint;
    
    /**
     * Request ID
     */
    private String requestId;
    
    /**
     * Request content (truncated if too long)
     */
    private String requestContent;
    
    /**
     * Response content (truncated if too long)
     */
    private String responseContent;
    
    /**
     * Calculated cost per token (USD)
     */
    @TableField(exist = false)
    private BigDecimal costPerToken;
    
    /**
     * Response speed (tokens per second)
     */
    @TableField(exist = false)
    private Double tokensPerSecond;
    
    /**
     * Calculate derived fields
     */
    public void calculateDerivedFields() {
        // Calculate cost per token
        if (costUsd != null && totalTokens != null && totalTokens > 0) {
            this.costPerToken = costUsd.divide(BigDecimal.valueOf(totalTokens), 8, BigDecimal.ROUND_HALF_UP);
        }
        
        // Calculate tokens per second
        if (durationMs != null && durationMs > 0 && totalTokens != null && totalTokens > 0) {
            this.tokensPerSecond = totalTokens / (durationMs / 1000.0);
        }
    }
    
    /**
     * Set success status
     */
    public void setSuccessStatus(boolean success, String errorMessage) {
        this.success = success ? 1 : 0;
        this.errorMessage = errorMessage;
        
        // Set response time if not already set
        if (success && this.responseTime == null) {
            this.responseTime = LocalDateTime.now();
        }
    }
    
    /**
     * Calculate duration
     */
    public void calculateDuration() {
        if (requestTime != null && responseTime != null) {
            long durationSeconds = java.time.Duration.between(requestTime, responseTime).toMillis();
            this.durationMs = (int) durationSeconds;
        }
    }
    
    /**
     * Check if request was successful
     */
    public boolean isSuccessful() {
        return success != null && success == 1;
    }
    
    /**
     * Check if request has cost data
     */
    public boolean hasCost() {
        return costUsd != null && costUsd.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Check if request has duration data
     */
    public boolean hasDuration() {
        return durationMs != null && durationMs > 0;
    }
    
    /**
     * Get formatted duration
     */
    public String getFormattedDuration() {
        if (durationMs == null) return "N/A";
        
        if (durationMs < 1000) {
            return durationMs + "ms";
        } else if (durationMs < 60000) {
            return String.format("%.2fs", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return minutes + "m " + seconds + "s";
        }
    }
    
    /**
     * Get formatted cost
     */
    public String getFormattedCost() {
        if (costUsd == null) return "N/A";
        return "$" + String.format("%.6f", costUsd);
    }
    
    /**
     * Get token breakdown
     */
    public String getTokenBreakdown() {
        if (promptTokens == null || completionTokens == null) {
            return totalTokens != null ? totalTokens + " tokens" : "N/A";
        }
        return promptTokens + " + " + completionTokens + " = " + totalTokens + " tokens";
    }
}