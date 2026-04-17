package com.aioa.ai.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * AI User Quota Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_user_quota")
public class AiUserQuota extends BaseEntity {
    
    /**
     * User ID
     */
    private String userId;
    
    /**
     * Model code
     */
    private String modelCode;
    
    /**
     * Daily token limit
     */
    private Integer dailyLimit;
    
    /**
     * Today used tokens
     */
    private Integer todayUsed;
    
    /**
     * Monthly token limit
     */
    private Integer monthlyLimit;
    
    /**
     * Monthly used tokens
     */
    private Integer monthlyUsed;
    
    /**
     * Total used tokens
     */
    private Long totalUsed;
    
    /**
     * Last reset date
     */
    private LocalDate lastResetDate;
    
    /**
     * Remaining daily tokens (calculated field)
     */
    @TableField(exist = false)
    private Integer remainingDaily;
    
    /**
     * Remaining monthly tokens (calculated field)
     */
    @TableField(exist = false)
    private Integer remainingMonthly;
    
    /**
     * Daily usage percentage (calculated field)
     */
    @TableField(exist = false)
    private Double dailyUsagePercentage;
    
    /**
     * Monthly usage percentage (calculated field)
     */
    @TableField(exist = false)
    private Double monthlyUsagePercentage;
    
    /**
     * Calculate remaining tokens
     */
    public void calculateRemaining() {
        if (dailyLimit != null && dailyLimit > 0) {
            this.remainingDaily = Math.max(0, dailyLimit - (todayUsed != null ? todayUsed : 0));
            this.dailyUsagePercentage = todayUsed != null ? (double) todayUsed / dailyLimit * 100 : 0.0;
        } else {
            this.remainingDaily = Integer.MAX_VALUE;
            this.dailyUsagePercentage = 0.0;
        }
        
        if (monthlyLimit != null && monthlyLimit > 0) {
            this.remainingMonthly = Math.max(0, monthlyLimit - (monthlyUsed != null ? monthlyUsed : 0));
            this.monthlyUsagePercentage = monthlyUsed != null ? (double) monthlyUsed / monthlyLimit * 100 : 0.0;
        } else {
            this.remainingMonthly = Integer.MAX_VALUE;
            this.monthlyUsagePercentage = 0.0;
        }
    }
    
    /**
     * Check if daily quota is exceeded
     */
    public boolean isDailyExceeded() {
        if (dailyLimit == null || dailyLimit <= 0) {
            return false; // No limit
        }
        return todayUsed != null && todayUsed >= dailyLimit;
    }
    
    /**
     * Check if monthly quota is exceeded
     */
    public boolean isMonthlyExceeded() {
        if (monthlyLimit == null || monthlyLimit <= 0) {
            return false; // No limit
        }
        return monthlyUsed != null && monthlyUsed >= monthlyLimit;
    }
    
    /**
     * Check if daily quota warning threshold is reached (80%)
     */
    public boolean isDailyWarning() {
        if (dailyLimit == null || dailyLimit <= 0) {
            return false; // No limit
        }
        return todayUsed != null && dailyLimit > 0 && 
               (double) todayUsed / dailyLimit >= 0.8;
    }
    
    /**
     * Add token usage
     */
    public void addUsage(int tokens) {
        if (tokens <= 0) {
            return;
        }
        
        // Update today usage
        if (todayUsed == null) {
            todayUsed = 0;
        }
        todayUsed += tokens;
        
        // Update monthly usage
        if (monthlyUsed == null) {
            monthlyUsed = 0;
        }
        monthlyUsed += tokens;
        
        // Update total usage
        if (totalUsed == null) {
            totalUsed = 0L;
        }
        totalUsed += tokens;
        
        // Recalculate remaining
        calculateRemaining();
    }
    
    /**
     * Reset daily usage
     */
    public void resetDaily() {
        this.todayUsed = 0;
        this.lastResetDate = LocalDate.now();
        calculateRemaining();
    }
    
    /**
     * Reset monthly usage
     */
    public void resetMonthly() {
        this.monthlyUsed = 0;
        calculateRemaining();
    }
}