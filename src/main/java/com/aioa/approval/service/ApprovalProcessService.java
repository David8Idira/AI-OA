package com.aioa.approval.service;

import com.aioa.approval.dto.*;
import com.aioa.approval.entity.ApprovalProcess;
import com.aioa.approval.repository.ApprovalProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批流程Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalProcessService {

    private final ApprovalProcessRepository processRepository;

    public ApprovalProcessResponseDTO create(ApprovalProcessDTO dto) {
        if (dto.getCode() != null && processRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("流程编码已存在");
        }

        ApprovalProcess process = ApprovalProcess.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .type(dto.getType())
                .formTemplate(dto.getFormTemplate())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        ApprovalProcess saved = processRepository.save(process);
        return convertToResponseDTO(saved);
    }

    public ApprovalProcessResponseDTO update(Long id, ApprovalProcessDTO dto) {
        ApprovalProcess process = processRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("流程不存在"));

        if (dto.getName() != null) process.setName(dto.getName());
        if (dto.getCode() != null) {
            if (processRepository.existsByCode(dto.getCode()) &&
                !processRepository.findByCode(dto.getCode()).get().getId().equals(id)) {
                throw new IllegalArgumentException("流程编码已存在");
            }
            process.setCode(dto.getCode());
        }
        if (dto.getType() != null) process.setType(dto.getType());
        if (dto.getFormTemplate() != null) process.setFormTemplate(dto.getFormTemplate());
        if (dto.getDescription() != null) process.setDescription(dto.getDescription());
        if (dto.getStatus() != null) process.setStatus(dto.getStatus());

        ApprovalProcess saved = processRepository.save(process);
        return convertToResponseDTO(saved);
    }

    public void delete(Long id) {
        if (!processRepository.existsById(id)) {
            throw new IllegalArgumentException("流程不存在");
        }
        processRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ApprovalProcessResponseDTO getById(Long id) {
        ApprovalProcess process = processRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("流程不存在"));
        return convertToResponseDTO(process);
    }

    @Transactional(readOnly = true)
    public List<ApprovalProcessResponseDTO> getAll() {
        return processRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApprovalProcessResponseDTO> getByType(String type) {
        return processRepository.findByType(type).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private ApprovalProcessResponseDTO convertToResponseDTO(ApprovalProcess process) {
        ApprovalProcessResponseDTO dto = new ApprovalProcessResponseDTO();
        dto.setId(process.getId());
        dto.setName(process.getName());
        dto.setCode(process.getCode());
        dto.setType(process.getType());
        dto.setFormTemplate(process.getFormTemplate());
        dto.setDescription(process.getDescription());
        dto.setStatus(process.getStatus());
        dto.setCreateTime(process.getCreateTime() != null ? process.getCreateTime().toString() : null);
        dto.setUpdateTime(process.getUpdateTime() != null ? process.getUpdateTime().toString() : null);
        return dto;
    }
}
