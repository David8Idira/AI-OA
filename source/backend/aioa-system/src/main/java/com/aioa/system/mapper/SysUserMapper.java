package com.aioa.system.mapper;

import com.aioa.system.entity.SysRole;
import com.aioa.system.entity.SysUserRole;
import com.aioa.system.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * User Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    /**
     * Get roles by user ID
     */
    List<SysRole> getRolesByUserId(@Param("userId") String userId);
    
    /**
     * Delete user roles
     */
    void deleteUserRoles(@Param("userId") String userId);
    
    /**
     * Batch insert user roles
     */
    void batchInsertUserRoles(@Param("list") List<SysUserRole> userRoles);
}
