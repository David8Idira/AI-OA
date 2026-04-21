package com.aioa.system.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.aioa.system.entity.SysMenu;
import com.aioa.system.entity.SysUser;
import com.aioa.system.mapper.SysMenuMapper;
import com.aioa.system.mapper.SysUserMapper;
import com.aioa.system.service.SysUserService;
import com.aioa.system.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * User Service Implementation
 * 实现完整的用户登录、权限和菜单查询功能
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    private static final String TOKEN_PREFIX = "aioa:token:";
    private static final String PERMISSION_CACHE_PREFIX = "aioa:permission:";
    private static final String MENU_CACHE_PREFIX = "aioa:menu:";
    private static final long TOKEN_EXPIRY = 7 * 24 * 60 * 60; // 7 days
    private static final long CACHE_EXPIRY = 30 * 60; // 30 minutes

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

        // Generate JWT Token
        String token = generateJwtToken(user);
        
        // Save to Redis
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, user.getId(), TOKEN_EXPIRY, TimeUnit.SECONDS);
        
        // 清除权限缓存
        clearPermissionCache(user.getId());

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
        // 先从缓存获取
        String cacheKey = PERMISSION_CACHE_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return Arrays.asList(cached.split(","));
        }

        // 从数据库查询
        List<String> permissions = sysMenuMapper.getPermissionsByUserId(userId);
        if (permissions == null || permissions.isEmpty()) {
            // 如果没有配置权限，返回空列表（不再返回 *:*:*）
            permissions = Collections.emptyList();
        }

        // 存入缓存
        if (!permissions.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, String.join(",", permissions), CACHE_EXPIRY, TimeUnit.SECONDS);
        }

        return permissions;
    }

    @Override
    public List<Object> getUserMenus(String userId) {
        // 先从缓存获取
        String cacheKey = MENU_CACHE_PREFIX + userId;
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null && !cachedJson.isEmpty()) {
            return JSONUtil.toList(cachedJson, Object.class);
        }

        // 从数据库查询
        List<SysMenu> menus = sysMenuMapper.getMenusByUserId(userId);
        if (menus == null) {
            menus = Collections.emptyList();
        }

        // 构建树形结构
        List<Object> menuTree = buildMenuTree(menus);

        // 存入缓存（使用 JSON 序列化）
        redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(menuTree), CACHE_EXPIRY, TimeUnit.SECONDS);

        return menuTree;
    }
    
    /**
     * 构建菜单树形结构
     */
    private List<Object> buildMenuTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        // 按父ID分组
        Map<String, List<SysMenu>> parentMap = menus.stream()
            .collect(Collectors.groupingBy(m -> m.getParentId() != null ? m.getParentId() : "0"));

        // 获取顶级菜单（parentId 为 null 或 "0"）
        List<SysMenu> rootMenus = parentMap.getOrDefault("0", Collections.emptyList());

        // 递归构建子菜单
        return rootMenus.stream()
            .map(menu -> buildMenuNode(menu, parentMap))
            .collect(Collectors.toList());
    }

    /**
     * 构建单个菜单节点（包含子菜单）
     */
    private Map<String, Object> buildMenuNode(SysMenu menu, Map<String, List<SysMenu>> parentMap) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", menu.getId());
        node.put("parentId", menu.getParentId());
        node.put("name", menu.getMenuName());
        node.put("path", menu.getPath());
        node.put("component", menu.getComponent());
        node.put("icon", menu.getIcon());
        node.put("permission", menu.getPermission());
        node.put("menuType", menu.getMenuType());
        node.put("sortOrder", menu.getSortOrder());
        node.put("visible", menu.getVisible());
        node.put("keepAlive", menu.getKeepAlive());

        // 递归添加子菜单
        List<SysMenu> children = parentMap.get(menu.getId());
        if (children != null && !children.isEmpty()) {
            List<Object> childNodes = children.stream()
                .map(child -> buildMenuNode(child, parentMap))
                .collect(Collectors.toList());
            node.put("children", childNodes);
        }

        return node;
    }

    /**
     * 生成 JWT Token
     * 简化实现：使用 UUID + Redis，生产环境应使用真实 JWT
     */
    private String generateJwtToken(SysUser user) {
        // 当前使用 UUID + Redis 存储
        // 生产环境建议改用真实 JWT，包含：
        // - sub: userId
        // - username
        // - roles
        // - iat: 签发时间
        // - exp: 过期时间
        // - iss: aioa-system
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 清除用户权限缓存
     */
    private void clearPermissionCache(String userId) {
        redisTemplate.delete(PERMISSION_CACHE_PREFIX + userId);
        redisTemplate.delete(MENU_CACHE_PREFIX + userId);
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
        vo.setMenus(getUserMenus(user.getId()));
        
        if (token != null) {
            vo.setToken(token);
            vo.setExpiresIn(TOKEN_EXPIRY);
        }
        return vo;
    }
}
