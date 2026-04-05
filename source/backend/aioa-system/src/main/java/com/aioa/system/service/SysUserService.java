package com.aioa.system.service;

import com.aioa.system.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aioa.system.vo.UserVO;
import java.util.List;

/**
 * User Service Interface
 */
public interface SysUserService extends IService<SysUser> {
    
    /**
     * User login
     * @param username username
     * @param password password
     * @return UserVO with token
     */
    UserVO login(String username, String password);
    
    /**
     * Get current user info
     * @param userId user ID
     * @return UserVO
     */
    UserVO getUserInfo(String userId);
    
    /**
     * Get user by username
     * @param username username
     * @return SysUser
     */
    SysUser getUserByUsername(String username);
    
    /**
     * Register new user
     * @param username username
     * @param password password
     * @param nickname nickname
     * @return SysUser
     */
    SysUser register(String username, String password, String nickname);
    
    /**
     * Update password
     * @param userId user ID
     * @param oldPassword old password
     * @param newPassword new password
     * @return true/false
     */
    boolean updatePassword(String userId, String oldPassword, String newPassword);
    
    /**
     * Get user permissions
     * @param userId user ID
     * @return permission list
     */
    List<String> getUserPermissions(String userId);
    
    /**
     * Get user menus
     * @param userId user ID
     * @return menu tree
     */
    List<Object> getUserMenus(String userId);
}
