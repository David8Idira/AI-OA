package com.aioa.ai.service.impl;

import com.aioa.ai.entity.AiUserQuota;
import com.aioa.ai.mapper.AiUserQuotaMapper;
import com.aioa.ai.service.AiQuotaService;
import com.aioa.ai.util.QuotaCalculator;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI配额服务实现 - Karpathy风格：简洁、清晰、专注业务逻辑
 * 参考nanoGPT的设计：服务层只做业务协调，复杂计算交给工具类
 * 目标：保持<200行代码
 */
@Slf4j
@Service
public class AiQuotaServiceImpl implements AiQuotaService {
    
    @Autowired
    private AiUserQuotaMapper aiUserQuotaMapper;
    
    @Override
    public boolean checkQuota(String userId, String modelCode) {
        log.debug("Checking quota for user: {}, model: {}", userId, modelCode);
        
        AiUserQuota quota = getOrCreateQuota(userId, modelCode);
        boolean exceeded = QuotaCalculator.isQuotaExceeded(quota);
        
        if (exceeded) {
            log.warn("Quota exceeded - user: {}, model: {}, daily: {}/{}, monthly: {}/{}",
                userId, modelCode, quota.getTodayUsed(), quota.getDailyLimit(),
                quota.getMonthlyUsed(), quota.getMonthlyLimit());
        } else if (QuotaCalculator.needsWarning(quota)) {
            log.info("Quota warning - user: {}, model: {}, daily: {}% used",
                userId, modelCode, quota.getDailyUsagePercentage() != null ? 
                String.format("%.1f", quota.getDailyUsagePercentage()) : "0.0");
        }
        
        return !exceeded;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useQuota(String userId, String modelCode, int tokens) {
        log.debug("Using quota - user: {}, model: {}, tokens: {}", userId, modelCode, tokens);
        
        // 参数验证
        if (tokens <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Token count must be positive");
        }
        
        // 检查配额
        if (!checkQuota(userId, modelCode)) {
            log.error("Insufficient quota - user: {}, model: {}", userId, modelCode);
            return false;
        }
        
        // 获取并更新配额
        AiUserQuota quota = getOrCreateQuota(userId, modelCode);
        QuotaCalculator.useQuotaTokens(quota, tokens);
        quota.setUpdateTime(LocalDateTime.now());
        
        // 保存到数据库
        aiUserQuotaMapper.updateById(quota);
        log.debug("Quota updated - user: {}, model: {}, used: {}, remaining daily: {}",
            userId, modelCode, tokens, quota.getRemainingDaily());
        
        return true;
    }
    
    @Override
    public Map<String, Object> getUserQuota(String userId) {
        log.debug("Getting quota info for user: {}", userId);
        
        LambdaQueryWrapper<AiUserQuota> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiUserQuota::getUserId, userId);
        List<AiUserQuota> quotas = aiUserQuotaMapper.selectList(queryWrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("totalModels", quotas.size());
        
        // 计算总使用量
        int totalDailyUsed = quotas.stream()
            .mapToInt(quota -> quota.getTodayUsed() != null ? quota.getTodayUsed() : 0)
            .sum();
        int totalMonthlyUsed = quotas.stream()
            .mapToInt(quota -> quota.getMonthlyUsed() != null ? quota.getMonthlyUsed() : 0)
            .sum();
        result.put("totalDailyUsed", totalDailyUsed);
        result.put("totalMonthlyUsed", totalMonthlyUsed);
        
        // 模型详情
        List<Map<String, Object>> modelDetails = quotas.stream()
            .map(quota -> {
                quota.calculateRemaining();
                Map<String, Object> detail = new HashMap<>();
                detail.put("modelCode", quota.getModelCode());
                detail.put("dailyUsed", quota.getTodayUsed());
                detail.put("dailyLimit", quota.getDailyLimit());
                detail.put("dailyRemaining", quota.getRemainingDaily());
                detail.put("dailyUsagePercentage", quota.getDailyUsagePercentage());
                detail.put("monthlyUsed", quota.getMonthlyUsed());
                detail.put("monthlyLimit", quota.getMonthlyLimit());
                detail.put("monthlyRemaining", quota.getRemainingMonthly());
                detail.put("monthlyUsagePercentage", quota.getMonthlyUsagePercentage());
                detail.put("needsWarning", quota.isDailyWarning());
                detail.put("isExceeded", quota.isDailyExceeded() || quota.isMonthlyExceeded());
                return detail;
            })
            .toList();
        
        result.put("modelDetails", modelDetails);
        return result;
    }
    
    @Override
    public Map<String, Object> getDepartmentQuota(String deptId) {
        log.debug("Getting department quota for: {}", deptId);
        
        // 简化实现：返回基本结构
        Map<String, Object> result = new HashMap<>();
        result.put("deptId", deptId);
        result.put("message", "Department quota functionality to be implemented");
        result.put("timestamp", LocalDateTime.now());
        
        return result;
    }
    
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void resetDailyQuota() {
        log.info("Resetting daily quota for all users");
        
        List<AiUserQuota> allQuotas = aiUserQuotaMapper.selectList(null);
        int updated = 0;
        
        for (AiUserQuota quota : allQuotas) {
            QuotaCalculator.resetDailyQuota(quota);
            quota.setUpdateTime(LocalDateTime.now());
            aiUserQuotaMapper.updateById(quota);
            updated++;
        }
        
        log.info("Daily quota reset completed, updated {} records", updated);
    }
    
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional(rollbackFor = Exception.class)
    public void resetMonthlyQuota() {
        log.info("Resetting monthly quota for all users");
        
        List<AiUserQuota> allQuotas = aiUserQuotaMapper.selectList(null);
        int updated = 0;
        
        for (AiUserQuota quota : allQuotas) {
            QuotaCalculator.resetMonthlyQuota(quota);
            quota.setUpdateTime(LocalDateTime.now());
            aiUserQuotaMapper.updateById(quota);
            updated++;
        }
        
        log.info("Monthly quota reset completed, updated {} records", updated);
    }
    
    /**
     * 获取或创建用户配额（私有方法）
     */
    private AiUserQuota getOrCreateQuota(String userId, String modelCode) {
        LambdaQueryWrapper<AiUserQuota> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiUserQuota::getUserId, userId)
                   .eq(AiUserQuota::getModelCode, modelCode);
        
        AiUserQuota quota = aiUserQuotaMapper.selectOne(queryWrapper);
        
        if (quota == null) {
            // 创建默认配额
            quota = QuotaCalculator.createDefaultQuota(userId, modelCode);
            quota.setCreateTime(LocalDateTime.now());
            aiUserQuotaMapper.insert(quota);
            log.debug("Created default quota for user: {}, model: {}", userId, modelCode);
        }
        
        // 检查是否需要重置（日期变化）
        if (quota.getLastResetDate() == null || !quota.getLastResetDate().equals(LocalDate.now())) {
            QuotaCalculator.resetDailyQuota(quota);
            aiUserQuotaMapper.updateById(quota);
        }
        
        return quota;
    }
}
