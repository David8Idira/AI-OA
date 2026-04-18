package com.aioa.system.service;

import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.aioa.system.entity.SysMenu;
import com.aioa.system.entity.SysUser;
import com.aioa.system.mapper.SysMenuMapper;
import com.aioa.system.mapper.SysUserMapper;
import com.aioa.system.service.impl.SysUserServiceImpl;
import com.aioa.system.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysUserServiceImpl单元测试
 * 
 * 测试用户服务的核心功能：
 * 1. 用户登录
 * 2. 用户信息获取
 * 3. 用户查询
 * 4. 用户管理
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysUserServiceImpl 单元测试")
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysMenuMapper sysMenuMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    private SysUser testUser;
    private SysUser adminUser;
    private List<SysMenu> testMenus;

    @BeforeEach
    void setUp() {
        // 配置模拟对象
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 创建测试用户
        testUser = new SysUser();
        testUser.setId("user-123");
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"); // 加密后的密码
        testUser.setNickname("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setStatus(1); // 启用状态
        testUser.setDeleted(0);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());

        adminUser = new SysUser();
        adminUser.setId("admin-001");
        adminUser.setUsername("admin");
        adminUser.setPassword("$2a$10$yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy"); // 加密后的密码
        adminUser.setNickname("系统管理员");
        adminUser.setStatus(1);
        adminUser.setDeleted(0);

        // 创建测试菜单
        SysMenu menu1 = new SysMenu();
        menu1.setId("menu-001");
        menu1.setMenuName("用户管理");
        menu1.setMenuType(1);
        menu1.setParentId("0");
        menu1.setOrderNum(1);
        menu1.setPath("/system/user");
        menu1.setComponent("system/user/index");
        menu1.setVisible(1);

        SysMenu menu2 = new SysMenu();
        menu2.setId("menu-002");
        menu2.setMenuName("角色管理");
        menu2.setMenuType(1);
        menu2.setParentId("0");
        menu2.setOrderNum(2);
        menu2.setPath("/system/role");
        menu2.setComponent("system/role/index");
        menu2.setVisible(1);

        testMenus = Arrays.asList(menu1, menu2);
    }

    @Test
    @DisplayName("测试用户登录 - 成功场景")
    void testLogin_Success() {
        // 准备测试数据
        String username = "testuser";
        String password = "password123";

        // 模拟数据库查询
        when(sysUserMapper.selectOne(any())).thenReturn(testUser);
        
        // 模拟Redis操作
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                .thenReturn(true);

        // 执行登录
        UserVO result = sysUserService.login(username, password);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getNickname(), result.getNickname());
        assertNotNull(result.getToken());

        // 验证方法调用
        verify(sysUserMapper).selectOne(any());
        verify(redisTemplate.opsForValue()).setIfAbsent(
                contains("aioa:token:"),
                eq(testUser.getId()),
                eq(7 * 24 * 60 * 60L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("测试用户登录 - 用户不存在")
    void testLogin_UserNotFound() {
        // 模拟用户不存在
        when(sysUserMapper.selectOne(any())).thenReturn(null);

        // 验证异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> sysUserService.login("nonexistent", "password123")
        );

        assertEquals(ResultCode.USERNAME_PASSWORD_ERROR, exception.getResultCode());
    }

    @Test
    @DisplayName("测试用户登录 - 密码错误")
    void testLogin_WrongPassword() {
        // 准备错误的密码
        String username = "testuser";
        String wrongPassword = "wrongpassword";

        // 模拟数据库查询返回用户
        when(sysUserMapper.selectOne(any())).thenReturn(testUser);

        // 验证异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> sysUserService.login(username, wrongPassword)
        );

        assertEquals(ResultCode.USERNAME_PASSWORD_ERROR, exception.getResultCode());
    }

    @Test
    @DisplayName("测试用户登录 - 用户被禁用")
    void testLogin_UserDisabled() {
        // 创建被禁用的用户
        SysUser disabledUser = new SysUser();
        disabledUser.setId("disabled-001");
        disabledUser.setUsername("disabled");
        disabledUser.setPassword("$2a$10$zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz");
        disabledUser.setStatus(0); // 禁用状态

        when(sysUserMapper.selectOne(any())).thenReturn(disabledUser);

        // 验证异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> sysUserService.login("disabled", "password123")
        );

        assertEquals(ResultCode.USER_DISABLED, exception.getResultCode());
    }

    @Test
    @DisplayName("测试获取用户信息 - 成功场景")
    void testGetUserInfo_Success() {
        String userId = "user-123";
        
        // 模拟数据库查询
        when(sysUserMapper.selectById(userId)).thenReturn(testUser);
        when(sysMenuMapper.selectUserMenus(userId)).thenReturn(testMenus);
        
        // 模拟Redis缓存
        when(valueOperations.get(eq("aioa:permission:user-123"))).thenReturn(null);
        when(valueOperations.get(eq("aioa:menu:user-123"))).thenReturn(null);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                .thenReturn(true);

        // 执行获取用户信息
        UserVO result = sysUserService.getUserInfo(userId);

        // 验证结果
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getNickname(), result.getNickname());
        assertNotNull(result.getPermissions());
        assertNotNull(result.getMenus());
        assertEquals(2, result.getMenus().size());

        // 验证方法调用
        verify(sysUserMapper).selectById(userId);
        verify(sysMenuMapper).selectUserMenus(userId);
    }

    @Test
    @DisplayName("测试获取用户信息 - 用户不存在")
    void testGetUserInfo_UserNotFound() {
        String userId = "nonexistent";
        
        // 模拟用户不存在
        when(sysUserMapper.selectById(userId)).thenReturn(null);

        // 验证异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> sysUserService.getUserInfo(userId)
        );

        assertEquals(ResultCode.USER_NOT_FOUND, exception.getResultCode());
    }

    @Test
    @DisplayName("测试根据用户名获取用户 - 成功场景")
    void testGetUserByUsername_Success() {
        String username = "testuser";
        
        // 模拟数据库查询
        when(sysUserMapper.selectOne(any())).thenReturn(testUser);

        // 执行查询
        SysUser result = sysUserService.getUserByUsername(username);

        // 验证结果
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(testUser.getId(), result.getId());

        // 验证方法调用
        verify(sysUserMapper).selectOne(argThat(wrapper ->
                wrapper.getSqlSelect().contains("username")
        ));
    }

    @Test
    @DisplayName("测试根据用户名获取用户 - 用户不存在")
    void testGetUserByUsername_NotFound() {
        String username = "nonexistent";
        
        // 模拟用户不存在
        when(sysUserMapper.selectOne(any())).thenReturn(null);

        // 执行查询
        SysUser result = sysUserService.getUserByUsername(username);

        // 验证结果
        assertNull(result);
    }

    @Test
    @DisplayName("测试用户退出登录")
    void testLogout_Success() {
        String token = "test-token-123";
        String userId = "user-123";
        
        // 模拟Redis查询
        when(valueOperations.get(eq("aioa:token:" + token))).thenReturn(userId);

        // 执行退出登录
        sysUserService.logout(token);

        // 验证方法调用
        verify(redisTemplate).delete(eq("aioa:token:" + token));
        verify(redisTemplate).delete(eq("aioa:permission:" + userId));
        verify(redisTemplate).delete(eq("aioa:menu:" + userId));
    }

    @Test
    @DisplayName("测试用户退出登录 - Token无效")
    void testLogout_InvalidToken() {
        String token = "invalid-token";
        
        // 模拟Token不存在
        when(valueOperations.get(eq("aioa:token:" + token))).thenReturn(null);

        // 执行退出登录
        sysUserService.logout(token);

        // 验证没有删除操作
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("测试获取用户权限 - 成功场景")
    void testGetUserPermissions_Success() {
        String userId = "user-123";
        Set<String> expectedPermissions = new HashSet<>(Arrays.asList(
                "system:user:view",
                "system:user:edit",
                "system:role:view"
        ));

        // 模拟数据库查询
        when(sysMenuMapper.selectUserPermissions(userId)).thenReturn(expectedPermissions);

        // 执行获取权限
        Set<String> result = sysUserService.getUserPermissions(userId);

        // 验证结果
        assertNotNull(result);
        assertEquals(expectedPermissions.size(), result.size());
        assertTrue(result.contains("system:user:view"));

        // 验证方法调用
        verify(sysMenuMapper).selectUserPermissions(userId);
    }

    @Test
    @DisplayName("测试缓存用户权限")
    void testCacheUserPermissions() {
        String userId = "user-123";
        Set<String> permissions = new HashSet<>(Arrays.asList("system:user:view", "system:user:edit"));

        // 执行缓存权限
        sysUserService.cacheUserPermissions(userId, permissions);

        // 验证Redis操作
        verify(valueOperations).setIfAbsent(
                eq("aioa:permission:" + userId),
                anyString(),
                eq(30 * 60L),
                eq(TimeUnit.SECONDS)
        );
    }
}