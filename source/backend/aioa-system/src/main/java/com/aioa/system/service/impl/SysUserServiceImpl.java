package com.aioa.system.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.aioa.system.entity.SysUser;
import com.aioa.system.mapper.SysUserMapper;
import com.aioa.system.service.SysUserService;
import com.aioa.system.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User Service Implementation
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_PREFIX = "aioa:token:";
    private static final long TOKEN_EXPIRY = 7 * 24 * 60 * 60; // 7 days

    @Override
    public UserVO login(String username, String password) {
        SysUser user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        // Verify password
        String encryptedPassword = SecureUtil.md5(password);
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        // Check status
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // Generate token
        String token = IdUtil.fastSimpleUUID();
        
        // Save to Redis
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, user.getId(), TOKEN_EXPIRY, TimeUnit.SECONDS);

        // Build UserVO
        return buildUserVO(user, token);
    }

    @Override
    public UserVO getUserInfo(String userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return buildUserVO(user, null);
    }

    @Override
    public SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return this.getOne(wrapper);
    }

    @Override
    public SysUser register(String username, String password, String nickname) {
        // Check if username exists
        SysUser existUser = getUserByUsername(username);
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(SecureUtil.md5(password));
        user.setNickname(StrUtil.isBlank(nickname) ? username : nickname);
        user.setStatus(1);
        this.save(user);
        return user;
    }

    @Override
    public boolean updatePassword(String userId, String oldPassword, String newPassword) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // Verify old password
        String encryptedOldPassword = SecureUtil.md5(oldPassword);
        if (!encryptedOldPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        // Update new password
        user.setPassword(SecureUtil.md5(newPassword));
        return this.updateById(user);
    }

    @Override
    public List<String> getUserPermissions(String userId) {
        // TODO: Implement permission query from menu table
        return Collections.singletonList("*:*:*");
    }

    @Override
    public List<Object> getUserMenus(String userId) {
        // TODO: Implement menu tree query
        return Collections.emptyList();
    }

    /**
     * Build UserVO from SysUser
     */
    private UserVO buildUserVO(SysUser user, String token) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setMobile(user.getMobile());
        vo.setAvatar(user.getAvatar());
        vo.setDeptId(user.getDeptId());
        vo.setPosition(user.getPosition());
        vo.setPermissions(getUserPermissions(user.getId()));
        
        if (token != null) {
            vo.setToken(token);
            vo.setExpiresIn(TOKEN_EXPIRY);
        }
        return vo;
    }
}
