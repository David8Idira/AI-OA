package com.aioa.system.service.impl;

import com.aioa.system.entity.SysRole;
import com.aioa.system.entity.SysUserRole;
import com.aioa.system.mapper.SysRoleMapper;
import com.aioa.system.mapper.SysUserMapper;
import com.aioa.system.service.SysRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Role Service Implementation
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<SysRole> getRoleList(String keyword, Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysRole::getRoleName, keyword)
                   .or()
                   .like(SysRole::getRoleCode, keyword);
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByAsc(SysRole::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<SysRole> getRolesByUserId(String userId) {
        return sysUserMapper.getRolesByUserId(userId);
    }

    @Override
    @Transactional
    public boolean assignRoles(String userId, List<String> roleIds) {
        // Delete existing role assignments
        sysUserMapper.deleteUserRoles(userId);
        
        // Insert new role assignments
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoles = new ArrayList<>();
            for (String roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoles.add(userRole);
            }
            sysUserMapper.batchInsertUserRoles(userRoles);
        }
        return true;
    }

    @Override
    public List<SysRole> getRoleTree() {
        List<SysRole> allRoles = list();
        return allRoles.stream()
                .sorted((r1, r2) -> r1.getSortOrder().compareTo(r2.getSortOrder()))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean updateKnowledgeAccess(Long roleId, Integer knowledgeAccessLevel, String allowedSecurityLevels) {
        SysRole role = getById(roleId);
        if (role == null) {
            return false;
        }
        role.setKnowledgeAccessLevel(knowledgeAccessLevel);
        role.setAllowedSecurityLevels(allowedSecurityLevels);
        return updateById(role);
    }
    
    @Override
    public Map<String, Object> getKnowledgeAccess(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) {
            return null;
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("roleId", role.getId());
        result.put("roleName", role.getRoleName() != null ? role.getRoleName() : "");
        result.put("roleCode", role.getRoleCode() != null ? role.getRoleCode() : "");
        result.put("knowledgeAccessLevel", role.getKnowledgeAccessLevel() != null ? role.getKnowledgeAccessLevel() : 6);
        result.put("allowedSecurityLevels", role.getAllowedSecurityLevels() != null ? role.getAllowedSecurityLevels() : "[]");
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getRoleKnowledgeConfig() {
        List<SysRole> roles = list();
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysRole role : roles) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("roleId", role.getId());
            item.put("roleName", role.getRoleName() != null ? role.getRoleName() : "");
            item.put("roleCode", role.getRoleCode() != null ? role.getRoleCode() : "");
            item.put("roleType", role.getRoleType() != null ? role.getRoleType() : "");
            item.put("status", role.getStatus() != null ? role.getStatus() : 1);
            item.put("knowledgeAccessLevel", role.getKnowledgeAccessLevel() != null ? role.getKnowledgeAccessLevel() : 6);
            item.put("allowedSecurityLevels", role.getAllowedSecurityLevels() != null ? role.getAllowedSecurityLevels() : "[]");
            item.put("sortOrder", role.getSortOrder() != null ? role.getSortOrder() : 0);
            result.add(item);
        }
        return result;
    }
}
