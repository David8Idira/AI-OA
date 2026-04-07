package com.aioa.report.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Report Entity - Main report table
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report")
public class Report extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Report title
     */
    private String title;

    /**
     * Report type: DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUAL, CUSTOM
     */
    private String type;

    /**
     * Report status: 0-Draft, 1-Generating, 2-Generated, 3-Failed, 4-Archived
     */
    private Integer status;

    /**
     * Report content (JSON format with chart data and text)
     */
    private String content;

    /**
     * Report template ID (if generated from template)
     */
    private String templateId;

    /**
     * Template name (denormalized for display)
     */
    private String templateName;

    /**
     * Data source description (what data was used to generate the report)
     */
    private String dataSource;

    /**
     * Report period start date
     */
    private LocalDateTime periodStart;

    /**
     * Report period end date
     */
    private LocalDateTime periodEnd;

    /**
     * Creator user ID
     */
    private String creatorId;

    /**
     * Creator name (denormalized for display)
     */
    private String creatorName;

    /**
     * Department ID
     */
    private String deptId;

    /**
     * Department name (denormalized for display)
     */
    private String deptName;

    /**
     * AI model used for generation (e.g., gpt-4o, gpt-4o-mini)
     */
    private String aiModel;

    /**
     * Chart configurations JSON (bar, line, pie chart data)
     */
    private String chartConfig;

    /**
     * File path for exported file (PDF/Excel)
     */
    private String exportPath;

    /**
     * Export format: PDF, EXCEL, HTML
     */
    private String exportFormat;

    /**
     * Report summary (auto-generated or AI-generated summary)
     */
    private String summary;

    /**
     * Tags (comma-separated)
     */
    private String tags;

    /**
     * View count
     */
    private Integer viewCount;

    /**
     * Is pinned: 0-No, 1-Yes
     */
    private Integer isPinned;

    /**
     * Sharing scope: PRIVATE, DEPARTMENT, COMPANY, PUBLIC
     */
    private String shareScope;

    /**
     * Remark/comment
     */
    private String remark;
}
