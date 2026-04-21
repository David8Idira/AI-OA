package com.aioa.system.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SysMenu Entity 单元测试
 * 毛泽东思想指导：实事求是，测试系统菜单实体
 */
@DisplayName("SysMenuTest 系统菜单实体测试")
class SysMenuTest {

    @Test
    @DisplayName("创建系统菜单实体")
    void createSysMenu() {
        // given
        SysMenu menu = new SysMenu();
        menu.setId("menu-001");
        menu.setMenuName("系统管理");
        menu.setParentId("0");
        menu.setSortOrder(1);
        menu.setStatus(1);

        // then
        assertThat(menu.getId()).isEqualTo("menu-001");
        assertThat(menu.getMenuName()).isEqualTo("系统管理");
        assertThat(menu.getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("设置和获取ID")
    void setAndGetId() {
        // given
        SysMenu menu = new SysMenu();

        // when
        menu.setId("test-id");

        // then
        assertThat(menu.getId()).isEqualTo("test-id");
    }

    @Test
    @DisplayName("设置和获取菜单名称")
    void setAndGetMenuName() {
        // given
        SysMenu menu = new SysMenu();

        // when
        menu.setMenuName("测试菜单");

        // then
        assertThat(menu.getMenuName()).isEqualTo("测试菜单");
    }

    @Test
    @DisplayName("设置和获取父菜单ID")
    void setAndGetParentId() {
        // given
        SysMenu menu = new SysMenu();

        // when
        menu.setParentId("parent-001");

        // then
        assertThat(menu.getParentId()).isEqualTo("parent-001");
    }

    @Test
    @DisplayName("设置和获取排序")
    void setAndGetSortOrder() {
        // given
        SysMenu menu = new SysMenu();

        // when
        menu.setSortOrder(99);

        // then
        assertThat(menu.getSortOrder()).isEqualTo(99);
    }

    @Test
    @DisplayName("设置和获取状态")
    void setAndGetStatus() {
        // given
        SysMenu menu = new SysMenu();

        // when
        menu.setStatus(0); // Disabled

        // then
        assertThat(menu.getStatus()).isEqualTo(0);
    }

    @Test
    @DisplayName("equals验证")
    void equals_sameId_shouldBeEqual() {
        // given
        SysMenu m1 = new SysMenu();
        m1.setId("test-id");
        
        SysMenu m2 = new SysMenu();
        m2.setId("test-id");

        // then
        assertThat(m1).isEqualTo(m2);
    }
}
