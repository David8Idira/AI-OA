package com.aioa.system.mapper;

import com.aioa.system.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Menu Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    
    /**
     * Get menu list by user ID
     */
    List<SysMenu> getMenusByUserId(@Param("userId") String userId);
    
    /**
     * Get permissions by user ID
     */
    List<String> getPermissionsByUserId(@Param("userId") String userId);
}
