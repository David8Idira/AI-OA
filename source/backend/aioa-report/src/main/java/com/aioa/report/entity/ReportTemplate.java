package com.aioa.report.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * ReportTemplate Entity - Report template table
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report_template")
public class ReportTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Template name
     */
    private String name;

    /**
     * Template code (unique identifier for programmatic access)
     */
    private String code;

    /**
     * Template description
     */
    private String description;

    /**
     * Template type: DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUAL, CUSTOM
     */
    private String type;

    /**
     * Template category: BUSINESS, FINANCIAL, HR, SALES, OPERATIONS, CUSTOM
     */
    private String category;

    /**
     * Template content (JSON structure with placeholders)
     * Example: {"sections": [{"title": "销售概览", "content": "{{sales_overview}}", "chartType": "BAR"}]}
     */
    private String content;

    /**
     * Default chart configurations JSON
     */
    private String defaultChartConfig;

    /**
     * Prompt template for AI generation
     */
    private String promptTemplate;

    /**
     * Variables schema (JSON array of variable definitions)
     * Example: [{"name": "period", "type": "DATE_RANGE", "required": true}]
     */
    private String variablesSchema;

    /**
     * Creator user ID
     */
    private String creatorId;

    /**
     * Creator name
     */
    private String creatorName;

    /**
     * Is system built-in template: 0-Custom, 1-System
     */
    private Integer isBuiltIn;

    /**
     * Is active: 0-Inactive, 1-Active
     */
    private Integer isActive;

    /**
     * Usage count
     */
    private Integer usageCount;

    /**
     * Sort order
     */
    private Integer sortOrder;

    /**
     * Tags (comma-separated)
     */
    private String tags;

    /**
     * Remark
     */
    private String remark;
}
