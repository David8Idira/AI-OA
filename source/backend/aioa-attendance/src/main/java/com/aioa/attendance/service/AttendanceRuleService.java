package com.aioa.attendance.service;

import com.aioa.attendance.dto.AttendanceRuleDTO;
import com.aioa.attendance.entity.AttendanceRule;
import com.aioa.common.vo.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Attendance Rule Service Interface
 */
public interface AttendanceRuleService extends IService<AttendanceRule> {

    /**
     * Create attendance rule
     */
    AttendanceRule createRule(AttendanceRuleDTO dto);

    /**
     * Update attendance rule
     */
    AttendanceRule updateRule(Long id, AttendanceRuleDTO dto);

    /**
     * Delete attendance rule
     */
    boolean deleteRule(Long id);

    /**
     * Get rule by ID
     */
    AttendanceRule getRuleById(Long id);

    /**
     * Get rule by code
     */
    AttendanceRule getRuleByCode(String ruleCode);

    /**
     * List rules by conditions
     */
    PageResult<AttendanceRule> listRules(Integer pageNum, Integer pageSize, String keyword, Integer status);

    /**
     * Get applicable rules for user
     */
    List<AttendanceRule> getApplicableRules(String userId, String deptId, String positionId);

    /**
     * Enable/disable rule
     */
    boolean toggleRuleStatus(Long id, Integer status);
}