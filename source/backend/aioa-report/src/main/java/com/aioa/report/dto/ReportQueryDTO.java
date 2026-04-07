package com.aioa.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Report Query DTO - Query parameters for report list
 */
@Data
@Schema(name = "ReportQueryDTO", description = "Report query parameters")
public class ReportQueryDTO {

    @Schema(description = "Keyword search (title, content, tags)")
    private String keyword;

    @Schema(description = "Report type filter: DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUAL, CUSTOM")
    private String type;

    @Schema(description = "Report status filter: 0-Draft, 1-Generating, 2-Generated, 3-Failed, 4-Archived")
    private Integer status;

    @Schema(description = "Template ID filter")
    private String templateId;

    @Schema(description = "Creator user ID filter")
    private String creatorId;

    @Schema(description = "Department ID filter")
    private String deptId;

    @Schema(description = "Start date filter (create time)")
    private String startDate;

    @Schema(description = "End date filter (create time)")
    private String endDate;

    @Schema(description = "Page number, default 1")
    private Integer pageNum = 1;

    @Schema(description = "Page size, default 10")
    private Integer pageSize = 10;

    @Schema(description = "Sort field, default createTime")
    private String sortField = "createTime";

    @Schema(description = "Sort order: asc, desc (default desc)")
    private String sortOrder = "desc";
}
