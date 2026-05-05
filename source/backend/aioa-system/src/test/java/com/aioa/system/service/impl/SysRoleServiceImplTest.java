package com.aioa.system.service.impl;

import com.aioa.system.entity.SysRole;
import com.aioa.system.entity.SysUserRole;
import com.aioa.system.mapper.SysRoleMapper;
import com.aioa.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysRoleServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试角色服务
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysRoleServiceImplTest 单元测试")
class SysRoleServiceImplTest {

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    private SysRoleServiceImpl sysRoleService;

    @BeforeEach
    void setUp() {
        sysRoleService = new SysRoleServiceImpl();
        ReflectionTestUtils.setField(sysRoleService, "baseMapper", sysRoleMapper);
        ReflectionTestUtils.setField(sysRoleService, "sysUserMapper", sysUserMapper);
    }

    private SysRole createRole(Long id, String roleCode, String roleName, Integer status, Integer sortOrder) {
        SysRole role = new SysRole();
        role.setId(id.toString());
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setStatus(status);
        role.setSortOrder(sortOrder);
        return role;
    }

    @Test
    @DisplayName("获取角色列表 - 无条件查询")
    void getRoleList_withNoCondition_shouldReturnAll() {
        // given
        List<SysRole> roles = new ArrayList<>();
        roles.add(createRole(1L, "admin", "管理员", 1, 1));
        roles.add(createRole(2L, "user", "普通用户", 1, 2));
        when(sysRoleMapper.selectList(any())).thenReturn(roles);

        // when
        List<SysRole> result = sysRoleService.getRoleList(null, null);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("获取角色列表 - 按关键字查询")
    void getRoleList_withKeyword_shouldFilterByKeyword() {
        // given
        List<SysRole> roles = new ArrayList<>();
        roles.add(createRole(1L, "admin", "管理员", 1, 1));
        when(sysRoleMapper.selectList(any())).thenReturn(roles);

        // when
        List<SysRole> result = sysRoleService.getRoleList("admin", null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoleCode()).isEqualTo("admin");
    }

    @Test
    @DisplayName("获取角色列表 - 按状态过滤")
    void getRoleList_withStatusFilter_shouldReturnFiltered() {
        // given
        List<SysRole> roles = new ArrayList<>();
        roles.add(createRole(1L, "admin", "管理员", 1, 1));
        when(sysRoleMapper.selectList(any())).thenReturn(roles);

        // when
        List<SysRole> result = sysRoleService.getRoleList(null, 1);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("获取角色列表 - 关键字和状态同时过滤")
    void getRoleList_withKeywordAndStatus_shouldApplyBothFilters() {
        // given
        List<SysRole> roles = new ArrayList<>();
        roles.add(createRole(1L, "admin", "管理员", 1, 1));
        when(sysRoleMapper.selectList(any())).thenReturn(roles);

        // when
        List<SysRole> result = sysRoleService.getRoleList("admin", 1);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("获取用户角色 - 正常场景")
    void getRolesByUserId_shouldReturnUserRoles() {
        // given
        List<SysRole> roles = new ArrayList<>();
        roles.add(createRole(1L, "admin", "管理员", 1, 1));
        roles.add(createRole(2L, "user", "普通用户", 1, 2));
        when(sysUserMapper.getRolesByUserId("user-001")).thenReturn(roles);

        // when
        List<SysRole> result = sysRoleService.getRolesByUserId("user-001");

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("获取用户角色 - 无角色返回空列表")
    void getRolesByUserId_withNoRoles_shouldReturnEmptyList() {
        // given
        when(sysUserMapper.getRolesByUserId("user-002")).thenReturn(new ArrayList<>());

        // when
        List<SysRole> result = sysRoleService.getRolesByUserId("user-002");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("分配角色 - 正常场景")
    void assignRoles_withValidInput_shouldReturnTrue() {
        // given
        doNothing().when(sysUserMapper).deleteUserRoles("user-001");
        doNothing().when(sysUserMapper).batchInsertUserRoles(anyList());

        // when
        boolean result = sysRoleService.assignRoles("user-001", List.of("role-1", "role-2"));

        // then
        assertThat(result).isTrue();
        verify(sysUserMapper, times(1)).deleteUserRoles("user-001");
        verify(sysUserMapper, times(1)).batchInsertUserRoles(anyList());
    }

    @Test
    @DisplayName("分配角色 - 空角色列表")
    void assignRoles_withEmptyRoleList_shouldReturnTrue() {
        // given
        doNothing().when(sysUserMapper).deleteUserRoles("user-001");

        // when
        boolean result = sysRoleService.assignRoles("user-001", new ArrayList<>());

        // then
        assertThat(result).isTrue();
        verify(sysUserMapper, times(1)).deleteUserRoles("user-001");
        verify(sysUserMapper, times(0)).batchInsertUserRoles(anyList());
    }

    @Test
    @DisplayName("分配角色 - null角色列表")
    void assignRoles_withNullRoleList_shouldReturnTrue() {
        // given
        doNothing().when(sysUserMapper).deleteUserRoles("user-001");

        // when
        boolean result = sysRoleService.assignRoles("user-001", null);

        // then
        assertThat(result).isTrue();
        verify(sysUserMapper, times(1)).deleteUserRoles("user-001");
    }

    @Test
    @DisplayName("获取角色树 - 正常场景")
    void getRoleTree_shouldReturnSortedRoles() {
        // given
        List<SysRole> roles = new ArrayList<>();
        roles.add(createRole(2L, "user", "普通用户", 1, 2));
        roles.add(createRole(1L, "admin", "管理员", 1, 1));
        when(sysRoleMapper.selectList(any())).thenReturn(roles);

        // when
        List<SysRole> result = sysRoleService.getRoleTree();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSortOrder()).isLessThanOrEqualTo(result.get(1).getSortOrder());
    }

    @Test
    @DisplayName("获取角色列表 - 空结果")
    void getRoleList_withNoResults_shouldReturnEmptyList() {
        // given
        when(sysRoleMapper.selectList(any())).thenReturn(new ArrayList<>());

        // when
        List<SysRole> result = sysRoleService.getRoleList("nonexist", 1);

        // then
        assertThat(result).isEmpty();
    }
}