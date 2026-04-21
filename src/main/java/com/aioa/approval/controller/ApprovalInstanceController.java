package com.aioa.approval.controller;

import com.aioa.approval.dto.ApprovalInstanceDTO;
import com.aioa.approval.dto.ApprovalInstanceResponseDTO;
import com.aioa.approval.service.ApprovalInstanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审批实例Controller
 */
@RestController
@RequestMapping("/approval/instances")
@RequiredArgsConstructor
public class ApprovalInstanceController {

    private final ApprovalInstanceService instanceService;

    @PostMapping
    public ResponseEntity<ApprovalInstanceResponseDTO> create(@Valid @RequestBody ApprovalInstanceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(instanceService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprovalInstanceResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ApprovalInstanceDTO dto) {
        return ResponseEntity.ok(instanceService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        instanceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalInstanceResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(instanceService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ApprovalInstanceResponseDTO>> getAll() {
        return ResponseEntity.ok(instanceService.getAll());
    }

    @GetMapping("/applicant/{applicantId}")
    public ResponseEntity<List<ApprovalInstanceResponseDTO>> getByApplicant(@PathVariable Long applicantId) {
        return ResponseEntity.ok(instanceService.getByApplicant(applicantId));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApprovalInstanceResponseDTO> submit(@PathVariable Long id) {
        return ResponseEntity.ok(instanceService.submit(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApprovalInstanceResponseDTO> approve(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Long approverId = Long.valueOf(params.get("approverId").toString());
        Integer result = Integer.valueOf(params.get("result").toString());
        String comment = params.get("comment") != null ? params.get("comment").toString() : null;
        return ResponseEntity.ok(instanceService.approve(id, approverId, result, comment));
    }
}
