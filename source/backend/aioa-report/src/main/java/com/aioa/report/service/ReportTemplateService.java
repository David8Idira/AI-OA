package com.aioa.report.service;

import com.aioa.report.entity.ReportTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * ReportTemplate Service Interface
 */
public interface ReportTemplateService extends IService<ReportTemplate> {

    /**
     * Get template by code
     *
     * @param code template code
     * @return template entity
     */
    ReportTemplate getByCode(String code);

    /**
     * Get active templates by type
     *
     * @param type template type
     * @return list of active templates
     */
    java.util.List<ReportTemplate> getActiveByType(String type);

    /**
     * Increment usage count
     *
     * @param templateId template ID
     */
    void incrementUsageCount(String templateId);
}
