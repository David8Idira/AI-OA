package com.aioa.aichat.service;

import com.aioa.aichat.dto.PromptTemplateDTO;
import com.aioa.aichat.dto.PromptTemplateResponseDTO;
import com.aioa.aichat.entity.PromptTemplate;
import com.aioa.aichat.repository.PromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 提示词模板Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PromptTemplateService {

    private final PromptTemplateRepository templateRepository;

    public PromptTemplateResponseDTO create(PromptTemplateDTO dto) {
        if (dto.getCode() != null && templateRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("模板编码已存在");
        }

        PromptTemplate template = PromptTemplate.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .type(dto.getType())
                .template(dto.getTemplate())
                .variables(dto.getVariables())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        PromptTemplate saved = templateRepository.save(template);
        return convertToResponseDTO(saved);
    }

    public PromptTemplateResponseDTO update(Long id, PromptTemplateDTO dto) {
        PromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("模板不存在"));

        if (dto.getName() != null) template.setName(dto.getName());
        if (dto.getCode() != null) {
            if (templateRepository.existsByCode(dto.getCode()) &&
                !templateRepository.findByCode(dto.getCode()).get().getId().equals(id)) {
                throw new IllegalArgumentException("模板编码已存在");
            }
            template.setCode(dto.getCode());
        }
        if (dto.getType() != null) template.setType(dto.getType());
        if (dto.getTemplate() != null) template.setTemplate(dto.getTemplate());
        if (dto.getVariables() != null) template.setVariables(dto.getVariables());
        if (dto.getDescription() != null) template.setDescription(dto.getDescription());
        if (dto.getStatus() != null) template.setStatus(dto.getStatus());

        PromptTemplate saved = templateRepository.save(template);
        return convertToResponseDTO(saved);
    }

    public void delete(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new IllegalArgumentException("模板不存在");
        }
        templateRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PromptTemplateResponseDTO getById(Long id) {
        PromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("模板不存在"));
        return convertToResponseDTO(template);
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateResponseDTO> getAll() {
        return templateRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateResponseDTO> getByType(String type) {
        return templateRepository.findByType(type).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private PromptTemplateResponseDTO convertToResponseDTO(PromptTemplate template) {
        PromptTemplateResponseDTO dto = new PromptTemplateResponseDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setCode(template.getCode());
        dto.setType(template.getType());
        dto.setTemplate(template.getTemplate());
        dto.setVariables(template.getVariables());
        dto.setDescription(template.getDescription());
        dto.setStatus(template.getStatus());
        dto.setCreateTime(template.getCreateTime() != null ? template.getCreateTime().toString() : null);
        return dto;
    }
}
