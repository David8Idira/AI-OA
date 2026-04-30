package com.aioa.system.service;

import com.aioa.system.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * Role Service Interface
 */
public interface SysRoleService extends IService<SysRole> {
    
    /**
     * Get role list
     */
    List<SysRole> getRoleList(String keyword, Integer status);
    
    /**
     * Get roles by user ID
     */
    List<SysRole> getRolesByUserId(String userId);
    
    /**
     * Assign roles to user
     */
    boolean assignRoles(String userId, List<String> roleIds);
    
    /**
     * Build role tree
     */
    List<SysRole> getRoleTree();
    
    /**
     * Update role's knowledge base access settings
     * @param roleId role ID
     * @param knowledgeAccessLevel access level (1-6)
     * @param allowedSecurityLevels JSON array of allowed security levels
     */
    boolean updateKnowledgeAccess(Long roleId, Integer knowledgeAccessLevel, String allowedSecurityLevels);
    
    /**
     * Get role's knowledge base access settings
     * @param roleId role ID
     * @return map with knowledgeAccessLevel and allowedSecurityLevels
     */
    Map<String, Object> getKnowledgeAccess(Long roleId);
    
    /**
     * Get all roles with their knowledge access levels (for role permission config page)
     */
    List<Map<String, Object>> getRoleKnowledgeConfig();
}
