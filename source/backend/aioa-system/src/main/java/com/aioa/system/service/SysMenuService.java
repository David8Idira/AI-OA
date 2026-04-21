package com.aioa.system.service;

import com.aioa.system.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Menu Service Interface
 */
public interface SysMenuService extends IService<SysMenu> {
    
    /**
     * Get menu tree
     */
    List<SysMenu> getMenuTree();
    
    /**
     * Get menu tree by user ID
     */
    List<SysMenu> getMenuTreeByUserId(String userId);
    
    /**
     * Get permissions by user ID
     */
    List<String> getPermissionsByUserId(String userId);
    
    /**
     * Build menu tree for router
     */
    List<Object> buildRouterMenus(String userId);
}
