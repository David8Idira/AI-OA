package com.aioa.system.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Menu/Permission Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    
    /**
     * Parent menu ID
     */
    private String parentId;
    
    /**
     * Tree path
     */
    private String treePath;
    
    /**
     * Menu type: menu, button
     */
    private String menuType;
    
    /**
     * Menu name
     */
    private String menuName;
    
    /**
     * Menu code/Permission
     */
    private String permission;
    
    /**
     * Route path
     */
    private String path;
    
    /**
     * Component
     */
    private String component;
    
    /**
     * Icon
     */
    private String icon;
    
    /**
     * Sort order
     */
    private Integer sortOrder;
    
    /**
     * Status: 0-disabled, 1-enabled
     */
    private Integer status;
    
    /**
     * Whether visible
     */
    private Integer visible;
    
    /**
     * Whether keep alive
     */
    private Integer keepAlive;
    
    /**
     * Child menus (not persisted)
     */
    @TableField(exist = false)
    private List<SysMenu> children;
}
