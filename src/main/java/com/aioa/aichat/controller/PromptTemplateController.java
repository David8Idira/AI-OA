package com.aioa.aichat.controller;

import com.aioa.aichat.dto.PromptTemplateDTO;
import com.aioa.aichat.dto.PromptTemplateResponseDTO;
import com.aioa.aichat.service.PromptTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提示词模板Controller
 */
@RestController
@RequestMapping("/chat/templates")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final PromptTemplateService templateService;

    @PostMapping
    public ResponseEntity<PromptTemplateResponseDTO> create(@Valid @RequestBody PromptTemplateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromptTemplateResponseDTO> update(@PathVariable Long id, @Valid @RequestBody PromptTemplateDTO dto) {
        return ResponseEntity.ok(templateService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptTemplateResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PromptTemplateResponseDTO>> getAll() {
        return ResponseEntity.ok(templateService.getAll());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<PromptTemplateResponseDTO>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(templateService.getByType(type));
    }
}
