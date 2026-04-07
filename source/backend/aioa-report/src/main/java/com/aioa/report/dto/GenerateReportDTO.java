package com.aioa.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Generate Report DTO - Request DTO for AI-powered report generation
 */
@Data
@Schema(name = "GenerateReportDTO", description = "Report generation request")
public class GenerateReportDTO {

    @Schema(description = "Report title")
    @NotBlank(message = "报表标题不能为空")
    private String title;

    @Schema(description = "Report type: DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUAL, CUSTOM")
    @NotBlank(message = "报表类型不能为空")
    private String type;

    @Schema(description = "Template ID (optional, if using a template)")
    private String templateId;

    @Schema(description = "Report period start time")
    @NotNull(message = "统计周期开始时间不能为空")
    private LocalDateTime periodStart;

    @Schema(description = "Report period end time")
    @NotNull(message = "统计周期结束时间不能为空")
    private LocalDateTime periodEnd;

    @Schema(description = "Data source description or SQL query")
    private String dataSource;

    @Schema(description = "Chart configurations - list of chart settings")
    private List<ChartConfigDTO> charts;

    @Schema(description = "Additional parameters for AI generation")
    private Map<String, Object> parameters;

    @Schema(description = "Report tags (comma-separated)")
    private String tags;

    @Schema(description = "Remark/comment")
    private String remark;

    @Schema(description = "AI model to use (default: gpt-4o)")
    private String aiModel;

    @Schema(description = "Share scope: PRIVATE, DEPARTMENT, COMPANY, PUBLIC")
    private String shareScope;

    /**
     * Chart configuration DTO
     */
    @Data
    public static class ChartConfigDTO {
        @Schema(description = "Chart title")
        private String title;

        @Schema(description = "Chart type: BAR, LINE, PIE, AREA, SCATTER, TABLE, TEXT")
        private String chartType;

        @Schema(description = "Data source key or query")
        private String dataSource;

        @Schema(description = "X-axis field name")
        private String xField;

        @Schema(description = "Y-axis field name(s), comma-separated for multiple")
        private String yFields;

        @Schema(description = "Chart color scheme")
        private String colorScheme;

        @Schema(description = "Additional chart options (JSON)")
        private String options;
    }
}
