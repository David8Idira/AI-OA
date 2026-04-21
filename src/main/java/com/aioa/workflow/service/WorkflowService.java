package com.aioa.workflow.service;

import com.aioa.common.BusinessException;
import com.aioa.workflow.dto.WorkflowStartDTO;
import com.aioa.workflow.dto.WorkflowTaskActionDTO;
import com.aioa.workflow.entity.WorkflowDefinition;
import com.aioa.workflow.entity.WorkflowInstance;
import com.aioa.workflow.entity.WorkflowTask;
import com.aioa.workflow.repository.WorkflowDefinitionRepository;
import com.aioa.workflow.repository.WorkflowInstanceRepository;
import com.aioa.workflow.repository.WorkflowTaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Tag(name = "工作流服务", description = "工作流引擎服务")
public class WorkflowService {

    private final WorkflowDefinitionRepository definitionRepo;
    private final WorkflowInstanceRepository instanceRepo;
    private final WorkflowTaskRepository taskRepo;

    public WorkflowService(WorkflowDefinitionRepository definitionRepo,
                           WorkflowInstanceRepository instanceRepo,
                           WorkflowTaskRepository taskRepo) {
        this.definitionRepo = definitionRepo;
        this.instanceRepo = instanceRepo;
        this.taskRepo = taskRepo;
    }

    @Operation(summary = "创建工作流定义")
    @Transactional
    public WorkflowDefinition createDefinition(WorkflowDefinition definition) {
        if (definitionRepo.existsByWorkflowKey(definition.getWorkflowKey())) {
            throw new BusinessException(409, "工作流标识已存在");
        }
        return definitionRepo.save(definition);
    }

    @Operation(summary = "更新工作流定义")
    @Transactional
    public WorkflowDefinition updateDefinition(Long id, WorkflowDefinition updated) {
        WorkflowDefinition def = definitionRepo.findById(id)
                .orElseThrow(() -> new BusinessException(404, "工作流定义不存在"));
        def.setName(updated.getName());
        def.setDescription(updated.getDescription());
        def.setDefinitionJson(updated.getDefinitionJson());
        if (updated.getActive() != null) def.setActive(updated.getActive());
        return definitionRepo.save(def);
    }

    @Operation(summary = "删除工作流定义")
    @Transactional
    public void deleteDefinition(Long id) {
        if (!definitionRepo.existsById(id)) {
            throw new BusinessException(404, "工作流定义不存在");
        }
        definitionRepo.deleteById(id);
    }

    @Operation(summary = "获取工作流定义列表")
    public List<WorkflowDefinition> listDefinitions() {
        return definitionRepo.findByActiveTrue();
    }

    @Operation(summary = "获取单个工作流定义")
    public WorkflowDefinition getDefinition(Long id) {
        return definitionRepo.findById(id)
                .orElseThrow(() -> new BusinessException(404, "工作流定义不存在"));
    }

    @Operation(summary = "启动工作流实例")
    @Transactional
    public WorkflowInstance startWorkflow(WorkflowStartDTO dto) {
        WorkflowDefinition definition = definitionRepo.findById(dto.getDefinitionId())
                .orElseThrow(() -> new BusinessException(404, "工作流定义不存在"));

        WorkflowInstance instance = new WorkflowInstance();
        instance.setDefinitionId(dto.getDefinitionId());
        instance.setInstanceName(dto.getInstanceName());
        instance.setVariables(dto.getVariables());
        instance.setStatus(1);
        instance.setCurrentNode("start");
        instance = instanceRepo.save(instance);

        WorkflowTask startTask = new WorkflowTask();
        startTask.setInstanceId(instance.getId());
        startTask.setTaskKey("start");
        startTask.setName("开始");
        startTask.setStatus(2);
        startTask.setCompletedBy(instance.getStartedBy());
        startTask.setCompletedAt(LocalDateTime.now());
        taskRepo.save(startTask);

        return instance;
    }

    @Operation(summary = "执行任务操作")
    @Transactional
    public WorkflowTask executeTask(WorkflowTaskActionDTO dto) {
        WorkflowTask task = taskRepo.findById(dto.getTaskId())
                .orElseThrow(() -> new BusinessException(404, "任务不存在"));

        if (!task.getStatus().equals(1)) {
            throw new BusinessException(400, "任务状态不可操作");
        }

        switch (dto.getAction()) {
            case "approve" -> task.setStatus(2);
            case "reject" -> task.setStatus(-1);
            case "transfer" -> {
                task.setStatus(3);
                task.setAssigneeId(dto.getAssigneeId());
            }
            default -> throw new BusinessException(400, "无效的操作类型");
        }

        task.setCompletedBy(dto.getAssigneeId());
        task.setCompletedAt(LocalDateTime.now());
        task.setComment(dto.getComment());

        return taskRepo.save(task);
    }

    @Operation(summary = "获取流程实例详情")
    public WorkflowInstance getInstance(Long id) {
        return instanceRepo.findById(id)
                .orElseThrow(() -> new BusinessException(404, "流程实例不存在"));
    }

    @Operation(summary = "获取流程实例的任务列表")
    public List<WorkflowTask> getInstanceTasks(Long instanceId) {
        return taskRepo.findByInstanceIdOrderByCreatedAtAsc(instanceId);
    }

    @Operation(summary = "获取待办任务列表")
    public List<WorkflowTask> getPendingTasks(Long assigneeId) {
        return taskRepo.findByAssigneeIdAndStatusOrderByCreatedAtAsc(assigneeId, 1);
    }

    @Operation(summary = "查询实例列表")
    public List<WorkflowInstance> listInstances(Long definitionId, Integer status) {
        if (definitionId != null) {
            return instanceRepo.findByDefinitionIdOrderByStartedAtDesc(definitionId);
        }
        if (status != null) {
            return instanceRepo.findByStatusOrderByStartedAtDesc(status);
        }
        return instanceRepo.findAll().stream()
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .toList();
    }
}
