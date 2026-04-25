package com.aioa.attendance.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aioa.attendance.dto.AttendanceRuleDTO;
import com.aioa.attendance.entity.AttendanceRule;
import com.aioa.attendance.mapper.AttendanceRuleMapper;
import com.aioa.attendance.service.AttendanceRuleService;
import com.aioa.common.vo.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Attendance Rule Service Implementation
 */
@Slf4j
@Service
public class AttendanceRuleServiceImpl extends ServiceImpl<AttendanceRuleMapper, AttendanceRule> implements AttendanceRuleService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceRule createRule(AttendanceRuleDTO dto) {
        // Check if rule code exists
        AttendanceRule existing = getRuleByCode(dto.getRuleCode());
        if (existing != null) {
            throw new RuntimeException("Rule code already exists: " + dto.getRuleCode());
        }

        AttendanceRule rule = new AttendanceRule();
        BeanUtils.copyProperties(dto, rule);
        
        // Convert lists to JSON
        if (dto.getWeekdays() != null) {
            rule.setWeekdays(JSONUtil.toJsonStr(dto.getWeekdays()));
        }
        if (dto.getExcludeHolidays() != null) {
            rule.setExcludeHolidays(JSONUtil.toJsonStr(dto.getExcludeHolidays()));
        }
        if (dto.getSpecialWorkDays() != null) {
            rule.setSpecialWorkDays(JSONUtil.toJsonStr(dto.getSpecialWorkDays()));
        }
        if (dto.getDeptIds() != null) {
            rule.setDeptIds(JSONUtil.toJsonStr(dto.getDeptIds()));
        }
        if (dto.getPositionIds() != null) {
            rule.setPositionIds(JSONUtil.toJsonStr(dto.getPositionIds()));
        }

        save(rule);
        log.info("Created attendance rule: {}", rule.getRuleCode());
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceRule updateRule(Long id, AttendanceRuleDTO dto) {
        AttendanceRule rule = getById(id);
        if (rule == null) {
            throw new RuntimeException("Rule not found: " + id);
        }

        // Check if rule code conflicts
        if (!rule.getRuleCode().equals(dto.getRuleCode())) {
            AttendanceRule existing = getRuleByCode(dto.getRuleCode());
            if (existing != null) {
                throw new RuntimeException("Rule code already exists: " + dto.getRuleCode());
            }
        }

        BeanUtils.copyProperties(dto, rule, "id", "createTime", "createBy");
        
        // Convert lists to JSON
        if (dto.getWeekdays() != null) {
            rule.setWeekdays(JSONUtil.toJsonStr(dto.getWeekdays()));
        }
        if (dto.getExcludeHolidays() != null) {
            rule.setExcludeHolidays(JSONUtil.toJsonStr(dto.getExcludeHolidays()));
        }
        if (dto.getSpecialWorkDays() != null) {
            rule.setSpecialWorkDays(JSONUtil.toJsonStr(dto.getSpecialWorkDays()));
        }
        if (dto.getDeptIds() != null) {
            rule.setDeptIds(JSONUtil.toJsonStr(dto.getDeptIds()));
        }
        if (dto.getPositionIds() != null) {
            rule.setPositionIds(JSONUtil.toJsonStr(dto.getPositionIds()));
        }

        updateById(rule);
        log.info("Updated attendance rule: {}", rule.getRuleCode());
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRule(Long id) {
        AttendanceRule rule = getById(id);
        if (rule == null) {
            return false;
        }
        
        // TODO: Check if rule is being used by groups
        
        removeById(id);
        log.info("Deleted attendance rule: {}", rule.getRuleCode());
        return true;
    }

    @Override
    public AttendanceRule getRuleById(Long id) {
        return getById(id);
    }

    @Override
    public AttendanceRule getRuleByCode(String ruleCode) {
        LambdaQueryWrapper<AttendanceRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRule::getRuleCode, ruleCode);
        return getOne(queryWrapper);
    }

    @Override
    public PageResult<AttendanceRule> listRules(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<AttendanceRule> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like(AttendanceRule::getRuleName, keyword)
                       .or()
                       .like(AttendanceRule::getRuleCode, keyword)
                       .or()
                       .like(AttendanceRule::getRemark, keyword);
        }
        
        if (status != null) {
            queryWrapper.eq(AttendanceRule::getStatus, status);
        }
        
        queryWrapper.orderByDesc(AttendanceRule::getCreateTime);
        
        Page<AttendanceRule> page = new Page<>(pageNum, pageSize);
        page(page, queryWrapper);
        
        return PageResult.success(page.getRecords(), page.getTotal());
    }

    @Override
    public List<AttendanceRule> getApplicableRules(String userId, String deptId, String positionId) {
        // TODO: Implement rule applicability logic
        // This should filter rules based on dept/position restrictions
        LambdaQueryWrapper<AttendanceRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRule::getStatus, 1);
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleRuleStatus(Long id, Integer status) {
        AttendanceRule rule = getById(id);
        if (rule == null) {
            return false;
        }
        
        rule.setStatus(status);
        updateById(rule);
        log.info("Toggled rule status: {} -> {}", rule.getRuleCode(), status);
        return true;
    }
}