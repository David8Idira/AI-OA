package com.aioa.system.service;

import com.aioa.system.entity.SysRole;
import com.aioa.system.entity.SysUserRole;
import com.aioa.system.mapper.SysRoleMapper;
import com.aioa.system.mapper.SysUserMapper;
import com.aioa.system.service.impl.SysRoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysRoleServiceImpl单元测试
 * 
 * 测试角色服务的核心功能：
 * 1. 角色列表查询
 * 2. 用户角色管理
 * 3. 角色分配功能
 * 4. 角色树构建
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysRoleServiceImpl 单元测试")
class SysRoleServiceImplTest {

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private SysRoleServiceImpl sysRoleService;

    private SysRole adminRole;
    private SysRole userRole;
    private SysRole guestRole;
    private List<SysRole> allRoles;

    @BeforeEach
    void setUp() {
        // 创建测试角色
        adminRole = new SysRole();
        adminRole.setId("role-001");
        adminRole.setRoleName("系统管理员");
        adminRole.setRoleCode("admin");
        adminRole.setSortOrder(1);
        adminRole.setStatus(1);
        adminRole.setDescription("系统超级管理员");
        adminRole.setCreateTime(LocalDateTime.now());

        userRole = new SysRole();
        userRole.setId("role-002");
        userRole.setRoleName("普通用户");
        userRole.setRoleCode("user");
        userRole.setSortOrder(2);
        userRole.setStatus(1);
        userRole.setDescription("普通业务用户");
        userRole.setCreateTime(LocalDateTime.now());

        guestRole = new SysRole();
        guestRole.setId("role-003");
        guestRole.setRoleName("访客");
        guestRole.setRoleCode("guest");
        guestRole.setSortOrder(3);
        guestRole.setStatus(0); // 禁用状态
        guestRole.setDescription("临时访客角色");
        guestRole.setCreateTime(LocalDateTime.now());

        allRoles = Arrays.asList(adminRole, userRole, guestRole);
    }

    @Test
    @DisplayName("测试获取角色列表 - 无过滤条件")
    void testGetRoleList_NoFilter() {
        // 准备测试数据
        List<SysRole> expectedRoles = Arrays.asList(adminRole, userRole, guestRole);
        
        // 模拟数据库查询
        when(sysRoleService.list(any())).thenReturn(expectedRoles);

        // 执行查询
        List<SysRole> result = sysRoleService.getRoleList(null, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("admin", result.get(0).getRoleCode());
        assertEquals("user", result.get(1).getRoleCode());

        // 验证方法调用
        verify(sysRoleService).list(argThat(wrapper -> 
            wrapper.getSqlSelect() == null && // 默认查询所有字段
            wrapper.getOrderByAsc() != null
        ));
    }

    @Test
    @DisplayName("测试获取角色列表 - 带关键字过滤")
    void testGetRoleList_WithKeyword() {
        String keyword = "管理";
        List<SysRole> expectedRoles = Arrays.asList(adminRole);
        
        // 模拟数据库查询
        when(sysRoleService.list(any())).thenReturn(expectedRoles);

        // 执行查询
        List<SysRole> result = sysRoleService.getRoleList(keyword, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("系统管理员", result.get(0).getRoleName());
        assertEquals("admin", result.get(0).getRoleCode());

        // 验证方法调用
        verify(sysRoleService).list(argThat(wrapper -> 
            wrapper.getSqlSelect() != null
        ));
    }

    @Test
    @DisplayName("测试获取角色列表 - 带状态过滤")
    void testGetRoleList_WithStatusFilter() {
        Integer enabledStatus = 1; // 启用状态
        List<SysRole> expectedRoles = Arrays.asList(adminRole, userRole);
        
        // 模拟数据库查询
        when(sysRoleService.list(any())).thenReturn(expectedRoles);

        // 执行查询
        List<SysRole> result = sysRoleService.getRoleList(null, enabledStatus);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(role -> role.getStatus() == 1));

        // 验证方法调用
        verify(sysRoleService).list(argThat(wrapper -> 
            wrapper.getSqlSelect() != null
        ));
    }

    @Test
    @DisplayName("测试获取角色列表 - 关键字和状态组合过滤")
    void testGetRoleList_WithKeywordAndStatus() {
        String keyword = "用户";
        Integer enabledStatus = 1;
        List<SysRole> expectedRoles = Arrays.asList(userRole);
        
        // 模拟数据库查询
        when(sysRoleService.list(any())).thenReturn(expectedRoles);

        // 执行查询
        List<SysRole> result = sysRoleService.getRoleList(keyword, enabledStatus);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("普通用户", result.get(0).getRoleName());
        assertEquals(1, result.get(0).getStatus());

        // 验证方法调用
        verify(sysRoleService).list(argThat(wrapper -> 
            wrapper.getSqlSelect() != null
        ));
    }

    @Test
    @DisplayName("测试根据用户ID获取角色 - 成功场景")
    void testGetRolesByUserId_Success() {
        String userId = "user-123";
        List<SysRole> expectedRoles = Arrays.asList(userRole);
        
        // 模拟数据库查询
        when(sysUserMapper.getRolesByUserId(userId)).thenReturn(expectedRoles);

        // 执行查询
        List<SysRole> result = sysRoleService.getRolesByUserId(userId);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("普通用户", result.get(0).getRoleName());

        // 验证方法调用
        verify(sysUserMapper).getRolesByUserId(userId);
    }

    @Test
    @DisplayName("测试根据用户ID获取角色 - 用户无角色")
    void testGetRolesByUserId_NoRoles() {
        String userId = "user-no-roles";
        
        // 模拟用户无角色
        when(sysUserMapper.getRolesByUserId(userId)).thenReturn(new ArrayList<>());

        // 执行查询
        List<SysRole> result = sysRoleService.getRolesByUserId(userId);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证方法调用
        verify(sysUserMapper).getRolesByUserId(userId);
    }

    @Test
    @DisplayName("测试为用户分配角色 - 成功场景")
    void testAssignRoles_Success() {
        String userId = "user-123";
        List<String> roleIds = Arrays.asList("role-001", "role-002");
        
        // 模拟删除现有角色分配
        when(sysRoleMapper.deleteUserRolesByUserId(userId)).thenReturn(2);

        // 模拟插入新角色分配
        when(sysRoleMapper.insertUserRole(any())).thenReturn(1);

        // 执行分配
        boolean result = sysRoleService.assignRoles(userId, roleIds);

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(sysRoleMapper).deleteUserRolesByUserId(userId);
        verify(sysRoleMapper, times(2)).insertUserRole(argThat(userRole ->
            userRole.getUserId().equals(userId) &&
            (userRole.getRoleId().equals("role-001") || userRole.getRoleId().equals("role-002"))
        ));
    }

    @Test
    @DisplayName("测试为用户分配角色 - 删除现有角色失败")
    void testAssignRoles_DeleteFailure() {
        String userId = "user-123";
        List<String> roleIds = Arrays.asList("role-001");
        
        // 模拟删除失败
        when(sysRoleMapper.deleteUserRolesByUserId(userId)).thenThrow(new RuntimeException("数据库错误"));

        // 执行分配 - 应抛出异常
        assertThrows(RuntimeException.class, () -> 
            sysRoleService.assignRoles(userId, roleIds)
        );

        // 验证方法调用
        verify(sysRoleMapper).deleteUserRolesByUserId(userId);
        verify(sysRoleMapper, never()).insertUserRole(any());
    }

    @Test
    @DisplayName("测试为用户分配角色 - 插入新角色失败")
    void testAssignRoles_InsertFailure() {
        String userId = "user-123";
        List<String> roleIds = Arrays.asList("role-001");
        
        // 模拟删除成功
        when(sysRoleMapper.deleteUserRolesByUserId(userId)).thenReturn(1);
        
        // 模拟插入失败
        when(sysRoleMapper.insertUserRole(any())).thenThrow(new RuntimeException("插入失败"));

        // 执行分配 - 应抛出异常
        assertThrows(RuntimeException.class, () -> 
            sysRoleService.assignRoles(userId, roleIds)
        );

        // 验证方法调用
        verify(sysRoleMapper).deleteUserRolesByUserId(userId);
        verify(sysRoleMapper).insertUserRole(any());
    }

    @Test
    @DisplayName("测试为用户分配角色 - 空角色列表")
    void testAssignRoles_EmptyRoleList() {
        String userId = "user-123";
        List<String> roleIds = new ArrayList<>();
        
        // 模拟删除现有角色分配
        when(sysRoleMapper.deleteUserRolesByUserId(userId)).thenReturn(0);

        // 执行分配（清空用户角色）
        boolean result = sysRoleService.assignRoles(userId, roleIds);

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(sysRoleMapper).deleteUserRolesByUserId(userId);
        verify(sysRoleMapper, never()).insertUserRole(any());
    }

    @Test
    @DisplayName("测试构建角色树 - 成功场景")
    void testGetRoleTree_Success() {
        // 创建带有父子关系的角色
        SysRole parentRole = new SysRole();
        parentRole.setId("role-parent");
        parentRole.setRoleName("管理角色");
        parentRole.setParentId("0");
        parentRole.setSortOrder(1);

        SysRole childRole1 = new SysRole();
        childRole1.setId("role-child1");
        childRole1.setRoleName("子角色1");
        childRole1.setParentId("role-parent");
        childRole1.setSortOrder(1);

        SysRole childRole2 = new SysRole();
        childRole2.setId("role-child2");
        childRole2.setRoleName("子角色2");
        childRole2.setParentId("role-parent");
        childRole2.setSortOrder(2);

        List<SysRole> allRoles = Arrays.asList(parentRole, childRole1, childRole2);
        
        // 模拟数据库查询
        when(sysRoleService.list(any())).thenReturn(allRoles);

        // 执行构建角色树
        List<SysRole> result = sysRoleService.getRoleTree();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size()); // 只有一个根节点
        assertEquals("管理角色", result.get(0).getRoleName());

        // 验证子节点（检查是否已构建树形结构）
        // 注意：getRoleTree实现可能返回包含子节点的树结构
        verify(sysRoleService).list(argThat(wrapper -> 
            wrapper.getSqlSelect() != null &&
            wrapper.getOrderByAsc() != null
        ));
    }

    @Test
    @DisplayName("测试构建角色树 - 无角色数据")
    void testGetRoleTree_NoData() {
        // 模拟空角色列表
        when(sysRoleService.list(any())).thenReturn(new ArrayList<>());

        // 执行构建角色树
        List<SysRole> result = sysRoleService.getRoleTree();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证方法调用
        verify(sysRoleService).list(any());
    }

    @Test
    @DisplayName("测试角色排序 - 验证排序规则")
    void testRoleSorting() {
        // 创建无序角色列表
        SysRole role1 = new SysRole();
        role1.setId("role-1");
        role1.setRoleName("角色1");
        role1.setSortOrder(3);

        SysRole role2 = new SysRole();
        role2.setId("role-2");
        role2.setRoleName("角色2");
        role2.setSortOrder(1);

        SysRole role3 = new SysRole();
        role3.setId("role-3");
        role3.setRoleName("角色3");
        role3.setSortOrder(2);

        List<SysRole> roles = Arrays.asList(role1, role2, role3);
        
        // 模拟数据库查询
        when(sysRoleService.list(any())).thenReturn(roles);

        // 执行获取角色列表（应自动排序）
        List<SysRole> result = sysRoleService.getRoleList(null, null);

        // 验证结果
        assertNotNull(result);
        
        // 验证排序条件被应用
        verify(sysRoleService).list(argThat(wrapper -> 
            wrapper.getOrderByAsc() != null &&
            wrapper.getOrderByAsc().size() > 0
        ));
    }

    @Test
    @DisplayName("测试角色状态管理 - 启用和禁用角色")
    void testRoleStatusManagement() {
        // 测试获取启用角色
        Integer enabledStatus = 1;
        List<SysRole> enabledRoles = Arrays.asList(adminRole, userRole);
        
        when(sysRoleService.list(argThat(wrapper -> 
            wrapper.getSqlSelect() != null &&
            wrapper.getEntity() != null &&
            wrapper.getEntity().getStatus() == enabledStatus
        ))).thenReturn(enabledRoles);

        List<SysRole> result = sysRoleService.getRoleList(null, enabledStatus);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(role -> role.getStatus() == 1));
    }
}