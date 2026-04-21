package com.aioa.system.service;

import com.aioa.system.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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
}
