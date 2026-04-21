package com.aioa.approval.controller;

import com.aioa.approval.dto.ApprovalProcessDTO;
import com.aioa.approval.dto.ApprovalProcessResponseDTO;
import com.aioa.approval.service.ApprovalProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批流程Controller
 */
@RestController
@RequestMapping("/approval/processes")
@RequiredArgsConstructor
public class ApprovalProcessController {

    private final ApprovalProcessService processService;

    @PostMapping
    public ResponseEntity<ApprovalProcessResponseDTO> create(@Valid @RequestBody ApprovalProcessDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(processService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprovalProcessResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ApprovalProcessDTO dto) {
        return ResponseEntity.ok(processService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        processService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalProcessResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(processService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ApprovalProcessResponseDTO>> getAll() {
        return ResponseEntity.ok(processService.getAll());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ApprovalProcessResponseDTO>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(processService.getByType(type));
    }
}
