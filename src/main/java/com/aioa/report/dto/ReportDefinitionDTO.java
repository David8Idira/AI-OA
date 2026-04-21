package com.aioa.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ReportDefinitionDTO", description = "报表定义DTO")
public class ReportDefinitionDTO {
    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "报表名称不能为空")
    @Schema(description = "报表名称")
    private String name;

    @NotBlank(message = "报表标识不能为空")
    @Schema(description = "报表唯一标识")
    private String reportKey;

    @Schema(description = "报表描述")
    private String description;

    @Schema(description = "SQL模板")
    private String sqlTemplate;

    @Schema(description = "图表配置JSON")
    private String chartConfig;

    @Schema(description = "是否启用")
    private Boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getReportKey() { return reportKey; }
    public void setReportKey(String reportKey) { this.reportKey = reportKey; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSqlTemplate() { return sqlTemplate; }
    public void setSqlTemplate(String sqlTemplate) { this.sqlTemplate = sqlTemplate; }
    public String getChartConfig() { return chartConfig; }
    public void setChartConfig(String chartConfig) { this.chartConfig = chartConfig; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
