package com.aioa.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "WorkflowDefinitionDTO", description = "工作流定义DTO")
public class WorkflowDefinitionDTO {
    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "工作流名称不能为空")
    @Schema(description = "工作流名称")
    private String name;

    @NotBlank(message = "工作流标识不能为空")
    @Schema(description = "工作流唯一标识")
    private String workflowKey;

    @Schema(description = "工作流描述")
    private String description;

    @Schema(description = "定义JSON")
    private String definitionJson;

    @Schema(description = "是否启用")
    private Boolean active;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWorkflowKey() { return workflowKey; }
    public void setWorkflowKey(String workflowKey) { this.workflowKey = workflowKey; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDefinitionJson() { return definitionJson; }
    public void setDefinitionJson(String definitionJson) { this.definitionJson = definitionJson; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
