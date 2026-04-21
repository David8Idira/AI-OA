package com.aioa.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "WorkflowTaskActionDTO", description = "工作流任务操作DTO")
public class WorkflowTaskActionDTO {
    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "操作类型：approve/reject/transfer")
    private String action;

    @Schema(description = "审批意见")
    private String comment;

    @Schema(description = "转派人ID(transfer时使用)")
    private Long assigneeId;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
}
