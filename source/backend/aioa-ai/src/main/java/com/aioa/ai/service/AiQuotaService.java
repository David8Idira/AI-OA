package com.aioa.ai.service;

import com.aioa.ai.dto.ChatRequestDTO;
import com.aioa.ai.dto.ChatResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * AI配额服务接口
 */
public interface AiQuotaService {
    
    /**
     * 检查用户配额
     * @return 是否允许调用
     */
    boolean checkQuota(String userId, String modelCode);
    
    /**
     * 使用配额
     * @return 是否成功，false表示超限额
     */
    boolean useQuota(String userId, String modelCode, int tokens);
    
    /**
     * 获取用户配额信息
     */
    Map<String, Object> getUserQuota(String userId);
    
    /**
     * 重置每日配额
     */
    void resetDailyQuota();
    
    /**
     * 获取部门配额
     */
    Map<String, Object> getDepartmentQuota(String deptId);
}