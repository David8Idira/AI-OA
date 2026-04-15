package com.aioa.ai.service.impl;

import com.aioa.ai.service.AiQuotaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI配额服务实现
 */
@Slf4j
@Service
public class AiQuotaServiceImpl implements AiQuotaService {
    
    // 用户配额存储 (生产环境应存储到数据库)
    private final ConcurrentHashMap<String, UserQuota> userQuotas = new ConcurrentHashMap<>();
    
    // 默认配额配置
    private static final int DEFAULT_DAILY_TOKENS = 100000; // 每日10万Token
    private static final double WARNING_THRESHOLD = 0.8; // 预警阈值80%
    
    public AiQuotaServiceImpl() {
        // 初始化默认用户配额
        initializeDefaultQuotas();
    }
    
    private void initializeDefaultQuotas() {
        // 添加默认用户admin
        userQuotas.put("admin", new UserQuota("admin", DEFAULT_DAILY_TOKENS));
        userQuotas.put("default", new UserQuota("default", DEFAULT_DAILY_TOKENS));
    }
    
    @Override
    public boolean checkQuota(String userId, String modelCode) {
        UserQuota quota = userQuotas.get(userId);
        if (quota == null) {
            // 新用户使用默认配额
            quota = new UserQuota(userId, DEFAULT_DAILY_TOKENS);
            userQuotas.put(userId, quota);
        }
        
        int remaining = quota.getRemainingTokens();
        log.info("用户 {} 剩余配额: {}", userId, remaining);
        
        // 检查是否需要预警
        if (remaining < DEFAULT_DAILY_TOKENS * WARNING_THRESHOLD) {
            log.warn("用户 {} 配额低于预警阈值，当前: {}, 阈值: {}", 
                userId, remaining, DEFAULT_DAILY_TOKENS * WARNING_THRESHOLD);
        }
        
        return remaining > 0;
    }
    
    @Override
    public boolean useQuota(String userId, String modelCode, int tokens) {
        UserQuota quota = userQuotas.get(userId);
        if (quota == null) {
            quota = new UserQuota(userId, DEFAULT_DAILY_TOKENS);
            userQuotas.put(userId, quota);
        }
        
        boolean success = quota.useTokens(tokens);
        if (success) {
            log.info("用户 {} 使用 {} tokens，剩余: {}", userId, tokens, quota.getRemainingTokens());
        } else {
            log.warn("用户 {} 配额不足，使用失败", userId);
        }
        return success;
    }
    
    @Override
    public Map<String, Object> getUserQuota(String userId) {
        UserQuota quota = userQuotas.get(userId);
        if (quota == null) {
            quota = new UserQuota(userId, DEFAULT_DAILY_TOKENS);
            userQuotas.put(userId, quota);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("dailyLimit", quota.getDailyLimit());
        result.put("used", quota.getUsedTokens());
        result.put("remaining", quota.getRemainingTokens());
        result.put("usagePercent", quota.getUsagePercent());
        result.put("warning", quota.isWarning());
        result.put("resetTime", quota.getResetTime());
        
        return result;
    }
    
    @Override
    public void resetDailyQuota() {
        log.info("重置所有用户每日配额");
        for (UserQuota quota : userQuotas.values()) {
            quota.reset();
        }
    }
    
    @Override
    public Map<String, Object> getDepartmentQuota(String deptId) {
        // 简单实现：获取该部门所有用户的配额总和
        Map<String, Object> result = new HashMap<>();
        result.put("departmentId", deptId);
        result.put("totalLimit", DEFAULT_DAILY_TOKENS * 10); // 假设10人部门
        result.put("note", "部门配额计算待完善");
        return result;
    }
    
    /**
     * 用户配额内部类
     */
    private static class UserQuota {
        private final String userId;
        private final int dailyLimit;
        private int usedTokens;
        private long lastResetTime;
        
        UserQuota(String userId, int dailyLimit) {
            this.userId = userId;
            this.dailyLimit = dailyLimit;
            this.usedTokens = 0;
            this.lastResetTime = System.currentTimeMillis();
        }
        
        boolean useTokens(int tokens) {
            if (usedTokens + tokens > dailyLimit) {
                return false;
            }
            usedTokens += tokens;
            return true;
        }
        
        void reset() {
            usedTokens = 0;
            lastResetTime = System.currentTimeMillis();
        }
        
        int getRemainingTokens() {
            return dailyLimit - usedTokens;
        }
        
        int getUsedTokens() {
            return usedTokens;
        }
        
        int getDailyLimit() {
            return dailyLimit;
        }
        
        double getUsagePercent() {
            return (double) usedTokens / dailyLimit * 100;
        }
        
        boolean isWarning() {
            return usedTokens >= dailyLimit * WARNING_THRESHOLD;
        }
        
        long getResetTime() {
            return lastResetTime;
        }
    }
}