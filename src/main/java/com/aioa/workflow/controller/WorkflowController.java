package com.aioa.workflow.controller;

import com.aioa.common.ApiResponse;
import com.aioa.workflow.dto.WorkflowDefinitionDTO;
import com.aioa.workflow.dto.WorkflowStartDTO;
import com.aioa.workflow.dto.WorkflowTaskActionDTO;
import com.aioa.workflow.entity.WorkflowDefinition;
import com.aioa.workflow.entity.WorkflowInstance;
import com.aioa.workflow.entity.WorkflowTask;
import com.aioa.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflows")
@Tag(name = "工作流模块", description = "工作流引擎API")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/definitions")
    @Operation(summary = "创建工作流定义")
    public ApiResponse<WorkflowDefinition> createDefinition(@Valid @RequestBody WorkflowDefinitionDTO dto) {
        WorkflowDefinition def = new WorkflowDefinition();
        def.setName(dto.getName());
        def.setWorkflowKey(dto.getWorkflowKey());
        def.setDescription(dto.getDescription());
        def.setDefinitionJson(dto.getDefinitionJson());
        def.setActive(dto.getActive() != null ? dto.getActive() : true);
        return ApiResponse.success(workflowService.createDefinition(def));
    }

    @PutMapping("/definitions/{id}")
    @Operation(summary = "更新工作流定义")
    public ApiResponse<WorkflowDefinition> updateDefinition(
            @Parameter(description = "定义ID") @PathVariable Long id,
            @Valid @RequestBody WorkflowDefinitionDTO dto) {
        WorkflowDefinition def = new WorkflowDefinition();
        def.setName(dto.getName());
        def.setDescription(dto.getDescription());
        def.setDefinitionJson(dto.getDefinitionJson());
        def.setActive(dto.getActive());
        return ApiResponse.success(workflowService.updateDefinition(id, def));
    }

    @DeleteMapping("/definitions/{id}")
    @Operation(summary = "删除工作流定义")
    public ApiResponse<Void> deleteDefinition(@Parameter(description = "定义ID") @PathVariable Long id) {
        workflowService.deleteDefinition(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/definitions")
    @Operation(summary = "获取工作流定义列表")
    public ApiResponse<List<WorkflowDefinition>> listDefinitions() {
        return ApiResponse.success(workflowService.listDefinitions());
    }

    @GetMapping("/definitions/{id}")
    @Operation(summary = "获取单个工作流定义")
    public ApiResponse<WorkflowDefinition> getDefinition(@Parameter(description = "定义ID") @PathVariable Long id) {
        return ApiResponse.success(workflowService.getDefinition(id));
    }

    @PostMapping("/instances")
    @Operation(summary = "启动工作流实例")
    public ApiResponse<WorkflowInstance> startWorkflow(@Valid @RequestBody WorkflowStartDTO dto) {
        return ApiResponse.created(workflowService.startWorkflow(dto));
    }

    @GetMapping("/instances/{id}")
    @Operation(summary = "获取流程实例详情")
    public ApiResponse<WorkflowInstance> getInstance(@Parameter(description = "实例ID") @PathVariable Long id) {
        return ApiResponse.success(workflowService.getInstance(id));
    }

    @GetMapping("/instances/{id}/tasks")
    @Operation(summary = "获取流程实例的任务列表")
    public ApiResponse<List<WorkflowTask>> getInstanceTasks(@Parameter(description = "实例ID") @PathVariable Long id) {
        return ApiResponse.success(workflowService.getInstanceTasks(id));
    }

    @PostMapping("/tasks/action")
    @Operation(summary = "执行任务操作")
    public ApiResponse<WorkflowTask> executeTask(@Valid @RequestBody WorkflowTaskActionDTO dto) {
        return ApiResponse.success(workflowService.executeTask(dto));
    }

    @GetMapping("/tasks/pending")
    @Operation(summary = "获取待办任务列表")
    public ApiResponse<List<WorkflowTask>> getPendingTasks(@Parameter(description = "办理人ID") @RequestParam Long assigneeId) {
        return ApiResponse.success(workflowService.getPendingTasks(assigneeId));
    }

    @GetMapping("/instances")
    @Operation(summary = "查询流程实例列表")
    public ApiResponse<List<WorkflowInstance>> listInstances(
            @Parameter(description = "定义ID") @RequestParam(required = false) Long definitionId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return ApiResponse.success(workflowService.listInstances(definitionId, status));
    }
}
