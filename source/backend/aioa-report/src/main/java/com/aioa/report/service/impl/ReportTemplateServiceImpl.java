package com.aioa.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aioa.report.entity.ReportTemplate;
import com.aioa.report.mapper.ReportTemplateMapper;
import com.aioa.report.service.ReportTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ReportTemplate Service Implementation
 */
@Slf4j
@Service
public class ReportTemplateServiceImpl extends ServiceImpl<ReportTemplateMapper, ReportTemplate> implements ReportTemplateService {

    @Override
    public ReportTemplate getByCode(String code) {
        log.info("Getting template by code: {}", code);
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportTemplate::getCode, code)
               .eq(ReportTemplate::getIsActive, 1);
        return getOne(wrapper);
    }

    @Override
    public List<ReportTemplate> getActiveByType(String type) {
        log.info("Getting active templates by type: {}", type);
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportTemplate::getType, type)
               .eq(ReportTemplate::getIsActive, 1)
               .orderByAsc(ReportTemplate::getSortOrder);
        return list(wrapper);
    }

    @Override
    public void incrementUsageCount(String templateId) {
        log.info("Incrementing usage count for template: {}", templateId);
        ReportTemplate template = getById(templateId);
        if (template != null) {
            template.setUsageCount(template.getUsageCount() == null ? 1 : template.getUsageCount() + 1);
            updateById(template);
        }
    }
}
