package com.aioa.approval.service;

import com.aioa.approval.dto.*;
import com.aioa.approval.entity.ApprovalInstance;
import com.aioa.approval.entity.ApprovalProcess;
import com.aioa.approval.entity.ApprovalTask;
import com.aioa.approval.repository.ApprovalInstanceRepository;
import com.aioa.approval.repository.ApprovalProcessRepository;
import com.aioa.approval.repository.ApprovalTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批实例Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalInstanceService {

    private final ApprovalInstanceRepository instanceRepository;
    private final ApprovalProcessRepository processRepository;
    private final ApprovalTaskRepository taskRepository;

    public ApprovalInstanceResponseDTO create(ApprovalInstanceDTO dto) {
        ApprovalProcess process = processRepository.findById(dto.getProcessId())
                .orElseThrow(() -> new IllegalArgumentException("流程不存在"));

        ApprovalInstance instance = ApprovalInstance.builder()
                .processId(dto.getProcessId())
                .applicantId(dto.getApplicantId())
                .title(dto.getTitle())
                .formData(dto.getFormData())
                .currentNode(0)
                .totalNodes(dto.getTotalNodes() != null ? dto.getTotalNodes() : 1)
                .status(0) // 草稿状态
                .build();

        ApprovalInstance saved = instanceRepository.save(instance);
        return convertToResponseDTO(saved, process);
    }

    public ApprovalInstanceResponseDTO update(Long id, ApprovalInstanceDTO dto) {
        ApprovalInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审批实例不存在"));

        if (dto.getTitle() != null) instance.setTitle(dto.getTitle());
        if (dto.getFormData() != null) instance.setFormData(dto.getFormData());
        if (dto.getCurrentNode() != null) instance.setCurrentNode(dto.getCurrentNode());
        if (dto.getTotalNodes() != null) instance.setTotalNodes(dto.getTotalNodes());
        if (dto.getStatus() != null) instance.setStatus(dto.getStatus());

        ApprovalInstance saved = instanceRepository.save(instance);
        ApprovalProcess process = processRepository.findById(saved.getProcessId()).orElse(null);
        return convertToResponseDTO(saved, process);
    }

    public void delete(Long id) {
        if (!instanceRepository.existsById(id)) {
            throw new IllegalArgumentException("审批实例不存在");
        }
        instanceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ApprovalInstanceResponseDTO getById(Long id) {
        ApprovalInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审批实例不存在"));
        ApprovalProcess process = processRepository.findById(instance.getProcessId()).orElse(null);
        return convertToResponseDTO(instance, process);
    }

    @Transactional(readOnly = true)
    public List<ApprovalInstanceResponseDTO> getAll() {
        return instanceRepository.findAll().stream()
                .map(i -> {
                    ApprovalProcess process = processRepository.findById(i.getProcessId()).orElse(null);
                    return convertToResponseDTO(i, process);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApprovalInstanceResponseDTO> getByApplicant(Long applicantId) {
        return instanceRepository.findByApplicantId(applicantId).stream()
                .map(i -> {
                    ApprovalProcess process = processRepository.findById(i.getProcessId()).orElse(null);
                    return convertToResponseDTO(i, process);
                })
                .collect(Collectors.toList());
    }

    /**
     * 提交审批（从草稿变为审批中）
     */
    public ApprovalInstanceResponseDTO submit(Long id) {
        ApprovalInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("审批实例不存在"));

        if (instance.getStatus() != 0) {
            throw new IllegalArgumentException("只有草稿状态可以提交");
        }

        instance.setStatus(1); // 审批中
        instance.setCurrentNode(1);
        ApprovalInstance saved = instanceRepository.save(instance);

        ApprovalProcess process = processRepository.findById(saved.getProcessId()).orElse(null);
        return convertToResponseDTO(saved, process);
    }

    /**
     * 执行审批
     */
    public ApprovalInstanceResponseDTO approve(Long instanceId, Long approverId, Integer result, String comment) {
        ApprovalInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("审批实例不存在"));

        // 创建审批任务记录
        ApprovalTask task = ApprovalTask.builder()
                .instanceId(instanceId)
                .approverId(approverId)
                .nodeOrder(instance.getCurrentNode())
                .comment(comment)
                .result(result)
                .status(1) // 已审批
                .approveTime(LocalDateTime.now())
                .build();
        taskRepository.save(task);

        // 更新实例状态
        if (result == 1) {
            // 通过
            if (instance.getCurrentNode() >= instance.getTotalNodes()) {
                instance.setStatus(2); // 已通过
                instance.setFinishTime(LocalDateTime.now());
            } else {
                instance.setCurrentNode(instance.getCurrentNode() + 1);
            }
        } else if (result == 2) {
            // 拒绝
            instance.setStatus(3); // 已拒绝
            instance.setFinishTime(LocalDateTime.now());
        }

        ApprovalInstance saved = instanceRepository.save(instance);
        ApprovalProcess process = processRepository.findById(saved.getProcessId()).orElse(null);
        return convertToResponseDTO(saved, process);
    }

    private ApprovalInstanceResponseDTO convertToResponseDTO(ApprovalInstance instance, ApprovalProcess process) {
        ApprovalInstanceResponseDTO dto = new ApprovalInstanceResponseDTO();
        dto.setId(instance.getId());
        dto.setProcessId(instance.getProcessId());
        dto.setProcessName(process != null ? process.getName() : null);
        dto.setApplicantId(instance.getApplicantId());
        dto.setTitle(instance.getTitle());
        dto.setFormData(instance.getFormData());
        dto.setCurrentNode(instance.getCurrentNode());
        dto.setTotalNodes(instance.getTotalNodes());
        dto.setStatus(instance.getStatus());
        dto.setStatusName(getStatusName(instance.getStatus()));
        dto.setCreateTime(instance.getCreateTime() != null ? instance.getCreateTime().toString() : null);
        dto.setUpdateTime(instance.getUpdateTime() != null ? instance.getUpdateTime().toString() : null);
        dto.setFinishTime(instance.getFinishTime() != null ? instance.getFinishTime().toString() : null);
        return dto;
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "审批中";
            case 2 -> "已通过";
            case 3 -> "已拒绝";
            case 4 -> "已撤回";
            default -> "未知";
        };
    }
}
