package com.aioa.attendance.controller;

import com.aioa.attendance.dto.AttendanceRuleDTO;
import com.aioa.attendance.entity.AttendanceRule;
import com.aioa.attendance.service.AttendanceRuleService;
import com.aioa.common.vo.PageResult;
import com.aioa.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Attendance Rule Controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/attendance/rules")
@Tag(name = "Attendance Rule Management", description = "Attendance rule related APIs")
public class AttendanceRuleController {

    @Autowired
    private AttendanceRuleService attendanceRuleService;

    @PostMapping
    @Operation(summary = "Create attendance rule", description = "Create a new attendance rule")
    public Result<AttendanceRule> createRule(@Valid @RequestBody AttendanceRuleDTO dto) {
        try {
            AttendanceRule rule = attendanceRuleService.createRule(dto);
            return Result.success(rule);
        } catch (Exception e) {
            log.error("Create rule error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update attendance rule", description = "Update an existing attendance rule")
    public Result<AttendanceRule> updateRule(@PathVariable Long id, @Valid @RequestBody AttendanceRuleDTO dto) {
        try {
            AttendanceRule rule = attendanceRuleService.updateRule(id, dto);
            return Result.success(rule);
        } catch (Exception e) {
            log.error("Update rule error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attendance rule", description = "Delete an attendance rule")
    public Result<Boolean> deleteRule(@PathVariable Long id) {
        try {
            boolean success = attendanceRuleService.deleteRule(id);
            return Result.success(success);
        } catch (Exception e) {
            log.error("Delete rule error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get attendance rule by ID", description = "Get attendance rule by ID")
    public Result<AttendanceRule> getRuleById(@PathVariable Long id) {
        try {
            AttendanceRule rule = attendanceRuleService.getRuleById(id);
            if (rule == null) {
                return Result.error("Rule not found");
            }
            return Result.success(rule);
        } catch (Exception e) {
            log.error("Get rule by ID error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/code/{ruleCode}")
    @Operation(summary = "Get attendance rule by code", description = "Get attendance rule by code")
    public Result<AttendanceRule> getRuleByCode(@PathVariable String ruleCode) {
        try {
            AttendanceRule rule = attendanceRuleService.getRuleByCode(ruleCode);
            if (rule == null) {
                return Result.error("Rule not found");
            }
            return Result.success(rule);
        } catch (Exception e) {
            log.error("Get rule by code error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "List attendance rules", description = "List attendance rules with pagination")
    public Result<PageResult<AttendanceRule>> listRules(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            PageResult<AttendanceRule> result = attendanceRuleService.listRules(pageNum, pageSize, keyword, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("List rules error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/applicable")
    @Operation(summary = "Get applicable rules", description = "Get applicable rules for user")
    public Result<List<AttendanceRule>> getApplicableRules(
            @RequestParam String userId,
            @RequestParam(required = false) String deptId,
            @RequestParam(required = false) String positionId) {
        try {
            List<AttendanceRule> rules = attendanceRuleService.getApplicableRules(userId, deptId, positionId);
            return Result.success(rules);
        } catch (Exception e) {
            log.error("Get applicable rules error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Toggle rule status", description = "Enable or disable an attendance rule")
    public Result<Boolean> toggleRuleStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            if (status != 0 && status != 1) {
                return Result.error("Invalid status, must be 0 or 1");
            }
            boolean success = attendanceRuleService.toggleRuleStatus(id, status);
            return Result.success(success);
        } catch (Exception e) {
            log.error("Toggle rule status error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}