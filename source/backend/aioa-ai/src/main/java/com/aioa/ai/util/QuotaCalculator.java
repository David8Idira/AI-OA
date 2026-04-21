package com.aioa.ai.util;

import com.aioa.ai.entity.AiUserQuota;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;

/**
 * 配额计算工具类 - Karpathy风格：单一职责，纯函数
 * 参考micrograd的设计：清晰的数学计算，无副作用
 */
@UtilityClass
public class QuotaCalculator {
    
    // 默认配额配置
    private static final int DEFAULT_DAILY_TOKENS = 100000;
    private static final int DEFAULT_MONTHLY_TOKENS = 3000000;
    private static final double WARNING_THRESHOLD = 0.8;
    
    /**
     * 计算剩余配额（纯函数）
     */
    public static void calculateRemaining(AiUserQuota quota) {
        if (quota == null) return;
        
        // 使用实体类自带的方法
        quota.calculateRemaining();
    }
    
    /**
     * 检查是否超出配额（纯函数）
     */
    public static boolean isQuotaExceeded(AiUserQuota quota) {
        if (quota == null) return true;
        
        quota.calculateRemaining();
        return quota.isDailyExceeded() || quota.isMonthlyExceeded();
    }
    
    /**
     * 检查是否需要预警（纯函数）
     */
    public static boolean needsWarning(AiUserQuota quota) {
        if (quota == null) return false;
        
        quota.calculateRemaining();
        return quota.isDailyWarning();
    }
    
    /**
     * 使用配额（纯函数）
     */
    public static AiUserQuota useQuotaTokens(AiUserQuota quota, int tokens) {
        if (quota == null || tokens <= 0) return quota;
        
        // 使用实体类自带的方法
        quota.addUsage(tokens);
        return quota;
    }
    
    /**
     * 重置每日配额（纯函数）
     */
    public static AiUserQuota resetDailyQuota(AiUserQuota quota) {
        if (quota == null) return quota;
        
        quota.resetDaily();
        return quota;
    }
    
    /**
     * 重置月度配额（纯函数）
     */
    public static AiUserQuota resetMonthlyQuota(AiUserQuota quota) {
        if (quota == null) return quota;
        
        quota.resetMonthly();
        return quota;
    }
    
    /**
     * 创建默认配额（纯函数）
     */
    public static AiUserQuota createDefaultQuota(String userId, String modelCode) {
        AiUserQuota quota = new AiUserQuota();
        quota.setUserId(userId);
        quota.setModelCode(modelCode);
        quota.setDailyLimit(DEFAULT_DAILY_TOKENS);
        quota.setMonthlyLimit(DEFAULT_MONTHLY_TOKENS);
        quota.setTodayUsed(0);
        quota.setMonthlyUsed(0);
        quota.setTotalUsed(0L);
        quota.setLastResetDate(LocalDate.now());
        quota.calculateRemaining();
        
        return quota;
    }
}
