package com.aioa.approval.service;

import com.aioa.approval.dto.ApprovalTaskDTO;
import com.aioa.approval.dto.ApprovalTaskResponseDTO;
import com.aioa.approval.entity.ApprovalInstance;
import com.aioa.approval.entity.ApprovalTask;
import com.aioa.approval.repository.ApprovalInstanceRepository;
import com.aioa.approval.repository.ApprovalTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批任务Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalTaskService {

    private final ApprovalTaskRepository taskRepository;
    private final ApprovalInstanceRepository instanceRepository;

    @Transactional(readOnly = true)
    public List<ApprovalTaskResponseDTO> getByInstance(Long instanceId) {
        return taskRepository.findByInstanceId(instanceId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApprovalTaskResponseDTO> getPendingTasks(Long approverId) {
        return taskRepository.findByApproverIdAndStatus(approverId, 0).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private ApprovalTaskResponseDTO convertToResponseDTO(ApprovalTask task) {
        ApprovalTaskResponseDTO dto = new ApprovalTaskResponseDTO();
        dto.setId(task.getId());
        dto.setInstanceId(task.getInstanceId());

        ApprovalInstance instance = instanceRepository.findById(task.getInstanceId()).orElse(null);
        dto.setInstanceTitle(instance != null ? instance.getTitle() : null);

        dto.setApproverId(task.getApproverId());
        dto.setNodeOrder(task.getNodeOrder());
        dto.setComment(task.getComment());
        dto.setResult(task.getResult());
        dto.setResultName(getResultName(task.getResult()));
        dto.setStatus(task.getStatus());
        dto.setStatusName(task.getStatus() == 0 ? "待审批" : "已审批");
        dto.setCreateTime(task.getCreateTime() != null ? task.getCreateTime().toString() : null);
        dto.setApproveTime(task.getApproveTime() != null ? task.getApproveTime().toString() : null);
        return dto;
    }

    private String getResultName(Integer result) {
        if (result == null) return null;
        return switch (result) {
            case 1 -> "通过";
            case 2 -> "拒绝";
            case 3 -> "转交";
            default -> "未知";
        };
    }
}
