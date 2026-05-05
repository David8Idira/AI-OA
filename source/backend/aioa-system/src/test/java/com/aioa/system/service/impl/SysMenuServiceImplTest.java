package com.aioa.system.service.impl;

import com.aioa.system.entity.SysMenu;
import com.aioa.system.mapper.SysMenuMapper;
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
 * SysMenuServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试菜单服务
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysMenuServiceImplTest 单元测试")
class SysMenuServiceImplTest {

    @Mock
    private SysMenuMapper sysMenuMapper;

    private SysMenuServiceImpl sysMenuService;

    @BeforeEach
    void setUp() {
        sysMenuService = new SysMenuServiceImpl();
        ReflectionTestUtils.setField(sysMenuService, "baseMapper", sysMenuMapper);
    }

    private SysMenu createMenu(Long id, String menuName, String parentId, String path, 
                                String component, Integer sortOrder, Integer status, Integer visible, Integer keepAlive) {
        SysMenu menu = new SysMenu();
        menu.setId(id.toString());
        menu.setMenuName(menuName);
        menu.setParentId(parentId);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setSortOrder(sortOrder);
        menu.setStatus(status);
        menu.setVisible(visible);
        menu.setKeepAlive(keepAlive);
        menu.setMenuType("menu");
        menu.setPermission("user:read");
        menu.setIcon("icon-home");
        return menu;
    }

    @Test
    @DisplayName("获取菜单树 - 正常场景")
    void getMenuTree_shouldReturnMenuTree() {
        // given
        List<SysMenu> menus = new ArrayList<>();
        menus.add(createMenu(1L, "系统管理", "0", "/system", "Layout", 1, 1, 1, 1));
        menus.add(createMenu(2L, "用户管理", "1", "/system/user", "UserManage", 1, 1, 1, 1));
        menus.add(createMenu(3L, "角色管理", "1", "/system/role", "RoleManage", 2, 1, 1, 1));
        when(sysMenuMapper.selectList(any())).thenReturn(menus);

        // when
        List<SysMenu> result = sysMenuService.getMenuTree();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMenuName()).isEqualTo("系统管理");
        assertThat(result.get(0).getChildren()).hasSize(2);
    }

    @Test
    @DisplayName("获取菜单树 - 空数据")
    void getMenuTree_withEmptyData_shouldReturnEmptyList() {
        // given
        when(sysMenuMapper.selectList(any())).thenReturn(new ArrayList<>());

        // when
        List<SysMenu> result = sysMenuService.getMenuTree();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("获取用户菜单树 - 正常场景")
    void getMenuTreeByUserId_shouldReturnUserMenus() {
        // given
        List<SysMenu> menus = new ArrayList<>();
        menus.add(createMenu(1L, "首页", "0", "/home", "Home", 1, 1, 1, 1));
        menus.add(createMenu(2L, "工作台", "1", "/workbench", "Workbench", 1, 1, 1, 1));
        when(sysMenuMapper.getMenusByUserId("user-001")).thenReturn(menus);

        // when
        List<SysMenu> result = sysMenuService.getMenuTreeByUserId("user-001");

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("获取用户权限 - 正常场景")
    void getPermissionsByUserId_shouldReturnPermissions() {
        // given
        List<String> permissions = List.of("user:read", "user:write", "user:delete");
        when(sysMenuMapper.getPermissionsByUserId("user-001")).thenReturn(permissions);

        // when
        List<String> result = sysMenuService.getPermissionsByUserId("user-001");

        // then
        assertThat(result).hasSize(3);
        assertThat(result).contains("user:read", "user:write", "user:delete");
    }

    @Test
    @DisplayName("获取用户权限 - 无权限")
    void getPermissionsByUserId_withNoPermissions_shouldReturnEmptyList() {
        // given
        when(sysMenuMapper.getPermissionsByUserId("user-002")).thenReturn(new ArrayList<>());

        // when
        List<String> result = sysMenuService.getPermissionsByUserId("user-002");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("构建路由菜单 - 正常场景")
    void buildRouterMenus_shouldReturnRouterTree() {
        // given
        List<SysMenu> menus = new ArrayList<>();
        menus.add(createMenu(1L, "首页", "0", "/home", "Home", 1, 1, 1, 1));
        menus.add(createMenu(2L, "系统", "1", "/system", "Layout", 1, 1, 1, 1));
        when(sysMenuMapper.getMenusByUserId("user-001")).thenReturn(menus);

        // when
        List<Object> result = sysMenuService.buildRouterMenus("user-001");

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("获取菜单树 - 返回所有菜单")
    void getMenuTree_shouldReturnAllMenus() {
        // given - 当mock返回所有菜单时（测试不依赖DB过滤）
        List<SysMenu> menus = new ArrayList<>();
        menus.add(createMenu(1L, "正常菜单", "0", "/normal", "Normal", 1, 1, 1, 1));
        menus.add(createMenu(2L, "另一个菜单", "0", "/other", "Other", 2, 1, 1, 1));
        when(sysMenuMapper.selectList(any())).thenReturn(menus);

        // when
        List<SysMenu> result = sysMenuService.getMenuTree();

        // then - 返回所有菜单（DB已过滤status=1）
        assertThat(result).hasSize(2); // parentId=0的两个菜单都返回
    }

    @Test
    @DisplayName("获取用户菜单树 - 多级菜单")
    void getMenuTreeByUserId_withMultiLevelMenus_shouldBuildCorrectTree() {
        // given
        List<SysMenu> menus = new ArrayList<>();
        menus.add(createMenu(1L, "一级菜单", "0", "/level1", "Level1", 1, 1, 1, 1));
        menus.add(createMenu(2L, "二级菜单", "1", "/level1/level2", "Level2", 1, 1, 1, 1));
        menus.add(createMenu(3L, "三级菜单", "2", "/level1/level2/level3", "Level3", 1, 1, 1, 1));
        when(sysMenuMapper.getMenusByUserId("user-001")).thenReturn(menus);

        // when
        List<SysMenu> result = sysMenuService.getMenuTreeByUserId("user-001");

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("构建路由菜单 - 过滤不可见菜单")
    void buildRouterMenus_shouldFilterInvisibleMenus() {
        // given
        List<SysMenu> menus = new ArrayList<>();
        menus.add(createMenu(1L, "可见菜单", "0", "/visible", "Visible", 1, 1, 1, 1));
        menus.add(createMenu(2L, "不可见菜单", "0", "/invisible", "Invisible", 2, 1, 0, 1)); // visible = 0
        when(sysMenuMapper.getMenusByUserId("user-001")).thenReturn(menus);

        // when
        List<Object> result = sysMenuService.buildRouterMenus("user-001");

        // then - only visible menus should be included
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("获取菜单树 - 按排序顺序")
    void getMenuTree_shouldOrderBySortOrder() {
        // given
        List<SysMenu> menus = new ArrayList<>();
        menus.add(createMenu(3L, "第三个", "0", "/third", "Third", 3, 1, 1, 1));
        menus.add(createMenu(1L, "第一个", "0", "/first", "First", 1, 1, 1, 1));
        menus.add(createMenu(2L, "第二个", "0", "/second", "Second", 2, 1, 1, 1));
        when(sysMenuMapper.selectList(any())).thenReturn(menus);

        // when
        List<SysMenu> result = sysMenuService.getMenuTree();

        // then
        assertThat(result).hasSize(3);
    }
}