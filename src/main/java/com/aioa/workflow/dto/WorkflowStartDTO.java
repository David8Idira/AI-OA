package com.aioa.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "WorkflowStartDTO", description = "启动工作流DTO")
public class WorkflowStartDTO {
    @NotBlank(message = "工作流定义ID不能为空")
    @Schema(description = "工作流定义ID")
    private Long definitionId;

    @Schema(description = "流程实例名称")
    private String instanceName;

    @Schema(description = "流程变量JSON")
    private String variables;

    public Long getDefinitionId() { return definitionId; }
    public void setDefinitionId(Long definitionId) { this.definitionId = definitionId; }
    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String instanceName) { this.instanceName = instanceName; }
    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }
}
