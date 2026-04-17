package com.aioa.ai.mapper;

import com.aioa.ai.entity.AiUserQuota;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * AI User Quota Mapper
 */
@Mapper
public interface AiUserQuotaMapper extends BaseMapper<AiUserQuota> {
    
    /**
     * Get user quota by user ID and model code
     */
    @Select("SELECT * FROM ai_user_quota WHERE user_id = #{userId} AND model_code = #{modelCode}")
    AiUserQuota selectByUserAndModel(@Param("userId") String userId, @Param("modelCode") String modelCode);
    
    /**
     * Get all quotas for a user
     */
    @Select("SELECT * FROM ai_user_quota WHERE user_id = #{userId}")
    List<AiUserQuota> selectByUser(@Param("userId") String userId);
    
    /**
     * Get all quotas for a model
     */
    @Select("SELECT * FROM ai_user_quota WHERE model_code = #{modelCode}")
    List<AiUserQuota> selectByModel(@Param("modelCode") String modelCode);
    
    /**
     * Increment today usage
     */
    @Update("UPDATE ai_user_quota SET today_used = today_used + #{tokens}, monthly_used = monthly_used + #{tokens}, total_used = total_used + #{tokens}, update_time = NOW() WHERE user_id = #{userId} AND model_code = #{modelCode}")
    int incrementUsage(@Param("userId") String userId, @Param("modelCode") String modelCode, @Param("tokens") int tokens);
    
    /**
     * Reset daily usage for all users
     */
    @Update("UPDATE ai_user_quota SET today_used = 0, last_reset_date = #{resetDate}")
    int resetAllDailyUsage(@Param("resetDate") LocalDate resetDate);
    
    /**
     * Reset monthly usage for all users
     */
    @Update("UPDATE ai_user_quota SET monthly_used = 0")
    int resetAllMonthlyUsage();
    
    /**
     * Get total usage statistics
     */
    @Select("SELECT model_code, SUM(today_used) as daily_total, SUM(monthly_used) as monthly_total, SUM(total_used) as total_total FROM ai_user_quota GROUP BY model_code")
    List<UsageStats> getUsageStatistics();
    
    /**
     * Get users exceeding daily limit
     */
    @Select("SELECT * FROM ai_user_quota WHERE daily_limit > 0 AND today_used >= daily_limit")
    List<AiUserQuota> selectExceededDailyLimit();
    
    /**
     * Get users exceeding monthly limit
     */
    @Select("SELECT * FROM ai_user_quota WHERE monthly_limit > 0 AND monthly_used >= monthly_limit")
    List<AiUserQuota> selectExceededMonthlyLimit();
    
    /**
     * Get users near daily limit (>= 80%)
     */
    @Select("SELECT * FROM ai_user_quota WHERE daily_limit > 0 AND today_used >= daily_limit * 0.8")
    List<AiUserQuota> selectNearDailyLimit();
    
    /**
     * Usage statistics DTO
     */
    interface UsageStats {
        String getModelCode();
        Long getDailyTotal();
        Long getMonthlyTotal();
        Long getTotalTotal();
    }
}