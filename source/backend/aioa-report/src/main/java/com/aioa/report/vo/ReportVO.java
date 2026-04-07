package com.aioa.report.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Report VO - View object for report details
 */
@Data
@Schema(name = "ReportVO", description = "Report details view object")
public class ReportVO {

    @Schema(description = "Report ID")
    private String id;

    @Schema(description = "Report title")
    private String title;

    @Schema(description = "Report type")
    private String type;

    @Schema(description = "Report type description")
    private String typeDesc;

    @Schema(description = "Report status: 0-Draft, 1-Generating, 2-Generated, 3-Failed, 4-Archived")
    private Integer status;

    @Schema(description = "Report status description")
    private String statusDesc;

    @Schema(description = "Report content (JSON)")
    private String content;

    @Schema(description = "Report template ID")
    private String templateId;

    @Schema(description = "Report template name")
    private String templateName;

    @Schema(description = "Data source description")
    private String dataSource;

    @Schema(description = "Report period start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodStart;

    @Schema(description = "Report period end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;

    @Schema(description = "Creator user ID")
    private String creatorId;

    @Schema(description = "Creator name")
    private String creatorName;

    @Schema(description = "Department ID")
    private String deptId;

    @Schema(description = "Department name")
    private String deptName;

    @Schema(description = "AI model used")
    private String aiModel;

    @Schema(description = "Chart configurations (JSON)")
    private String chartConfig;

    @Schema(description = "Export path")
    private String exportPath;

    @Schema(description = "Export format")
    private String exportFormat;

    @Schema(description = "Report summary")
    private String summary;

    @Schema(description = "Tags")
    private String tags;

    @Schema(description = "View count")
    private Integer viewCount;

    @Schema(description = "Is pinned")
    private Integer isPinned;

    @Schema(description = "Share scope")
    private String shareScope;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Create time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "Update time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "Create by")
    private String createBy;

    @Schema(description = "Update by")
    private String updateBy;
}
