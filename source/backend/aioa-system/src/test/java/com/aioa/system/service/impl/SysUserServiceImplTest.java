package com.aioa.system.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.aioa.common.exception.BusinessException;
import com.aioa.system.entity.SysMenu;
import com.aioa.system.entity.SysUser;
import com.aioa.system.mapper.SysMenuMapper;
import com.aioa.system.mapper.SysUserMapper;
import com.aioa.system.service.SysUserService;
import com.aioa.system.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * SysUserServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试用户服务
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SysUserServiceImplTest 单元测试")
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysMenuMapper sysMenuMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    // Service under test - use a spy to allow partial mocking of inherited methods
    private SysUserServiceImpl sysUserService;

    @BeforeEach
    void setUp() {
        // Create service and inject dependencies
        sysUserService = new SysUserServiceImpl();
        
        // Inject mapper via parent class baseMapper
        ReflectionTestUtils.setField(sysUserService, "baseMapper", sysUserMapper);
        ReflectionTestUtils.setField(sysUserService, "sysMenuMapper", sysMenuMapper);
        ReflectionTestUtils.setField(sysUserService, "redisTemplate", redisTemplate);
    }

    private SysUser createTestUser() {
        SysUser user = new SysUser();
        user.setId("user-001");
        user.setUsername("testuser");
        user.setPassword(SecureUtil.md5("password123"));
        user.setNickname("测试用户");
        user.setEmail("test@example.com");
        user.setMobile("13800138000");
        user.setStatus(1);
        user.setDeptId("dept-001");
        user.setPosition("工程师");
        return user;
    }

    @Test
    @DisplayName("登录 - 用户名不存在抛出异常")
    void login_withNonExistingUsername_shouldThrowException() {
        // given
        when(sysUserMapper.selectOne(any())).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> sysUserService.login("nonexist", "password"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("登录 - 密码错误抛出异常")
    void login_withWrongPassword_shouldThrowException() {
        // given
        SysUser user = createTestUser();
        when(sysUserMapper.selectOne(any())).thenReturn(user);

        // when & then
        assertThatThrownBy(() -> sysUserService.login("testuser", "wrongpassword"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("登录 - 账户被禁用抛出异常")
    void login_withDisabledAccount_shouldThrowException() {
        // given
        SysUser user = createTestUser();
        user.setStatus(0);
        when(sysUserMapper.selectOne(any())).thenReturn(user);

        // when & then
        assertThatThrownBy(() -> sysUserService.login("testuser", "password123"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("登录 - 成功返回UserVO")
    void login_withValidCredentials_shouldReturnUserVO() {
        // given
        SysUser user = createTestUser();
        when(sysUserMapper.selectOne(any())).thenReturn(user);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any());
        when(redisTemplate.delete(anyString())).thenReturn(false);
        when(sysMenuMapper.getPermissionsByUserId(anyString())).thenReturn(List.of("user:read"));
        when(sysMenuMapper.getMenusByUserId(anyString())).thenReturn(List.of());

        // when
        UserVO result = sysUserService.login("testuser", "password123");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getToken()).isNotNull();
    }

    @Test
    @DisplayName("获取用户信息 - 用户不存在抛出异常")
    void getUserInfo_withNonExistingUser_shouldThrowException() {
        // given
        when(sysUserMapper.selectById("nonexist")).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> sysUserService.getUserInfo("nonexist"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("获取用户信息 - 成功返回UserVO")
    void getUserInfo_withExistingUser_shouldReturnUserVO() {
        // given
        SysUser user = createTestUser();
        when(sysUserMapper.selectById("user-001")).thenReturn(user);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(sysMenuMapper.getPermissionsByUserId(anyString())).thenReturn(List.of());
        when(sysMenuMapper.getMenusByUserId(anyString())).thenReturn(List.of());

        // when
        UserVO result = sysUserService.getUserInfo("user-001");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("注册 - 用户名已存在抛出异常")
    void register_withExistingUsername_shouldThrowException() {
        // given
        SysUser existingUser = createTestUser();
        // Use lenient stubbing to avoid strict stubbing issues
        lenient().when(sysUserMapper.selectOne(any())).thenReturn(existingUser);

        // when & then
        assertThatThrownBy(() -> sysUserService.register("testuser", "password", "nickname"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("注册 - 成功创建用户")
    void register_withNewUsername_shouldCreateUser() {
        // given
        lenient().when(sysUserMapper.selectOne(any())).thenReturn(null);
        when(sysUserMapper.insert(any(SysUser.class))).thenReturn(1);

        // when
        SysUser result = sysUserService.register("newuser", "password123", "新用户");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
    }

    @Test
    @DisplayName("注册 - 空昵称使用用户名")
    void register_withEmptyNickname_shouldUseUsername() {
        // given
        when(sysUserMapper.selectOne(any())).thenReturn(null);
        when(sysUserMapper.insert(any(SysUser.class))).thenReturn(1);

        // when
        SysUser result = sysUserService.register("newuser", "password123", "");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("newuser");
    }

    @Test
    @DisplayName("更新密码 - 用户不存在抛出异常")
    void updatePassword_withNonExistingUser_shouldThrowException() {
        // given
        when(sysUserMapper.selectById("nonexist")).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> sysUserService.updatePassword("nonexist", "old", "new"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("更新密码 - 旧密码错误抛出异常")
    void updatePassword_withWrongOldPassword_shouldThrowException() {
        // given
        SysUser user = createTestUser();
        when(sysUserMapper.selectById("user-001")).thenReturn(user);

        // when & then
        assertThatThrownBy(() -> sysUserService.updatePassword("user-001", "wrongold", "newpass"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("更新密码 - 成功更新")
    void updatePassword_withCorrectOldPassword_shouldUpdate() {
        // given
        SysUser user = createTestUser();
        when(sysUserMapper.selectById("user-001")).thenReturn(user);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        // when
        boolean result = sysUserService.updatePassword("user-001", "password123", "newpassword");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("获取用户权限 - 缓存命中")
    void getUserPermissions_withCacheHit_shouldReturnFromCache() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("aioa:permission:user-001")).thenReturn("user:read,user:write");

        // when
        List<String> result = sysUserService.getUserPermissions("user-001");

        // then
        assertThat(result).containsExactly("user:read", "user:write");
    }

    @Test
    @DisplayName("获取用户权限 - 缓存未命中从数据库查询")
    void getUserPermissions_withCacheMiss_shouldQueryDatabase() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("aioa:permission:user-001")).thenReturn(null);
        when(sysMenuMapper.getPermissionsByUserId("user-001")).thenReturn(List.of("user:read"));

        // when
        List<String> result = sysUserService.getUserPermissions("user-001");

        // then
        assertThat(result).containsExactly("user:read");
    }

    @Test
    @DisplayName("获取用户菜单 - 缓存命中")
    void getUserMenus_withCacheHit_shouldReturnFromCache() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("aioa:menu:user-001")).thenReturn("[{\"name\":\"Dashboard\"}]");

        // when
        List<Object> result = sysUserService.getUserMenus("user-001");

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("获取用户菜单 - 无权限返回空列表")
    void getUserMenus_withNoMenus_shouldReturnEmptyList() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(sysMenuMapper.getMenusByUserId("user-001")).thenReturn(List.of());

        // when
        List<Object> result = sysUserService.getUserMenus("user-001");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("获取用户权限 - 无权限返回空列表")
    void getUserPermissions_withNoPermissions_shouldReturnEmptyList() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(sysMenuMapper.getPermissionsByUserId("user-001")).thenReturn(List.of());

        // when
        List<String> result = sysUserService.getUserPermissions("user-001");

        // then
        assertThat(result).isEmpty();
    }
}