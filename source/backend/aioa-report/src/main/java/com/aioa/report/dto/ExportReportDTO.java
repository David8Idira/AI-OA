package com.aioa.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Export Report DTO - Request DTO for report export
 */
@Data
@Schema(name = "ExportReportDTO", description = "Report export request")
public class ExportReportDTO {

    @Schema(description = "Export format: PDF, EXCEL, HTML")
    @NotBlank(message = "导出格式不能为空")
    private String format = "PDF";

    @Schema(description = "Include charts in export (default true)")
    private Boolean includeCharts = true;

    @Schema(description = "Include raw data (default false)")
    private Boolean includeRawData = false;

    @Schema(description = "Page orientation for PDF: PORTRAIT, LANDSCAPE")
    private String orientation = "PORTRAIT";

    @Schema(description = "Custom file name (without extension)")
    private String fileName;
}
