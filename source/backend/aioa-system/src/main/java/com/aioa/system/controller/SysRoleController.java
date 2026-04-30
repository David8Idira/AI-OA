package com.aioa.system.controller;

import com.aioa.common.result.Result;
import com.aioa.system.entity.SysRole;
import com.aioa.system.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Role Controller
 */
@RestController
@RequestMapping("/api/v1/roles")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * Get role list
     */
    @GetMapping
    public Result<List<SysRole>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(sysRoleService.getRoleList(keyword, status));
    }

    /**
     * Get role detail
     */
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable String id) {
        return Result.success(sysRoleService.getById(id));
    }

    /**
     * Create role
     */
    @PostMapping
    public Result<String> create(@RequestBody SysRole role) {
        sysRoleService.save(role);
        return Result.success(role.getId().toString());
    }

    /**
     * Update role
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String id, @RequestBody SysRole role) {
        role.setId(id);
        sysRoleService.updateById(role);
        return Result.success();
    }

    /**
     * Delete role
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        sysRoleService.removeById(id);
        return Result.success();
    }

    /**
     * Get role tree
     */
    @GetMapping("/tree")
    public Result<List<SysRole>> getTree() {
        return Result.success(sysRoleService.getRoleTree());
    }

    /**
     * Get roles by user ID
     */
    @GetMapping("/user/{userId}")
    public Result<List<SysRole>> getByUserId(@PathVariable String userId) {
        return Result.success(sysRoleService.getRolesByUserId(userId));
    }

    /**
     * Assign roles to user
     */
    @PostMapping("/user/{userId}/assign")
    public Result<Void> assignRoles(@PathVariable String userId, @RequestBody List<String> roleIds) {
        sysRoleService.assignRoles(userId, roleIds);
        return Result.success();
    }

    /**
     * Get all roles with knowledge base access config (for role permission page)
     */
    @GetMapping("/knowledge-config")
    public Result<List<Map<String, Object>>> getKnowledgeConfig() {
        return Result.success(sysRoleService.getRoleKnowledgeConfig());
    }

    /**
     * Get single role's knowledge base access settings
     */
    @GetMapping("/{id}/knowledge-access")
    public Result<Map<String, Object>> getKnowledgeAccess(@PathVariable String id) {
        return Result.success(sysRoleService.getKnowledgeAccess(Long.parseLong(id)));
    }

    /**
     * Update role's knowledge base access settings
     */
    @PutMapping("/{id}/knowledge-access")
    public Result<Void> updateKnowledgeAccess(
            @PathVariable String id,
            @RequestParam Integer knowledgeAccessLevel,
            @RequestParam(required = false) String allowedSecurityLevels) {
        sysRoleService.updateKnowledgeAccess(Long.parseLong(id), knowledgeAccessLevel, allowedSecurityLevels);
        return Result.success();
    }
}
