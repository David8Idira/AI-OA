package com.aioa.ai.mapper;

import com.aioa.ai.entity.AiModelConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * AI Model Configuration Mapper
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfig> {
    
    /**
     * Get all enabled models
     */
    @Select("SELECT * FROM ai_model_config WHERE enabled = 1 ORDER BY sort_order ASC")
    List<AiModelConfig> selectAllEnabled();
    
    /**
     * Get default model for a function type
     */
    @Select("SELECT * FROM ai_model_config WHERE enabled = 1 AND default_for LIKE CONCAT('%', #{functionType}, '%') ORDER BY sort_order ASC LIMIT 1")
    AiModelConfig selectDefaultConfig(String functionType);
    
    /**
     * Increment today usage count
     */
    @Update("UPDATE ai_model_config SET today_usage = today_usage + 1 WHERE model_code = #{modelCode}")
    int incrementUsage(String modelCode);
    
    /**
     * Reset daily usage (should be called by scheduled task)
     */
    @Update("UPDATE ai_model_config SET today_usage = 0")
    int resetDailyUsage();
}