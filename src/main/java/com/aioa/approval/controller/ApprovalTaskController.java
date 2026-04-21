package com.aioa.approval.controller;

import com.aioa.approval.dto.ApprovalTaskResponseDTO;
import com.aioa.approval.service.ApprovalTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批任务Controller
 */
@RestController
@RequestMapping("/approval/tasks")
@RequiredArgsConstructor
public class ApprovalTaskController {

    private final ApprovalTaskService taskService;

    @GetMapping("/instance/{instanceId}")
    public ResponseEntity<List<ApprovalTaskResponseDTO>> getByInstance(@PathVariable Long instanceId) {
        return ResponseEntity.ok(taskService.getByInstance(instanceId));
    }

    @GetMapping("/pending/{approverId}")
    public ResponseEntity<List<ApprovalTaskResponseDTO>> getPendingTasks(@PathVariable Long approverId) {
        return ResponseEntity.ok(taskService.getPendingTasks(approverId));
    }
}
