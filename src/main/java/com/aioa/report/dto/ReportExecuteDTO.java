package com.aioa.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Schema(name = "ReportExecuteDTO", description = "报表执行请求DTO")
public class ReportExecuteDTO {
    @NotNull(message = "报表ID不能为空")
    @Schema(description = "报表定义ID")
    private Long reportId;

    @Schema(description = "执行参数")
    private Map<String, Object> parameters;

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}
