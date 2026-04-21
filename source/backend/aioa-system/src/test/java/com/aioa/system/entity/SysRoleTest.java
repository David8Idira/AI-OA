package com.aioa.system.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SysRole Entity 单元测试
 * 毛泽东思想指导：实事求是，测试系统角色实体
 */
@DisplayName("SysRoleTest 系统角色实体测试")
class SysRoleTest {

    @Test
    @DisplayName("创建系统角色实体")
    void createSysRole() {
        // given
        SysRole role = new SysRole();
        role.setRoleCode("admin");
        role.setRoleName("管理员");
        role.setRoleType("SYSTEM");
        role.setStatus(1);
        role.setSortOrder(1);

        // then
        assertThat(role.getRoleCode()).isEqualTo("admin");
        assertThat(role.getRoleName()).isEqualTo("管理员");
        assertThat(role.getRoleType()).isEqualTo("SYSTEM");
    }

    @Test
    @DisplayName("设置和获取角色代码")
    void setAndGetRoleCode() {
        // given
        SysRole role = new SysRole();

        // when
        role.setRoleCode("test_role");

        // then
        assertThat(role.getRoleCode()).isEqualTo("test_role");
    }

    @Test
    @DisplayName("设置和获取角色名称")
    void setAndGetRoleName() {
        // given
        SysRole role = new SysRole();

        // when
        role.setRoleName("测试角色");

        // then
        assertThat(role.getRoleName()).isEqualTo("测试角色");
    }

    @Test
    @DisplayName("设置和获取角色类型")
    void setAndGetRoleType() {
        // given
        SysRole role = new SysRole();

        // when
        role.setRoleType("CUSTOM");

        // then
        assertThat(role.getRoleType()).isEqualTo("CUSTOM");
    }

    @Test
    @DisplayName("设置和获取数据范围")
    void setAndGetDataScope() {
        // given
        SysRole role = new SysRole();

        // when
        role.setDataScope("DEPT");

        // then
        assertThat(role.getDataScope()).isEqualTo("DEPT");
    }

    @Test
    @DisplayName("设置和获取状态")
    void setAndGetStatus() {
        // given
        SysRole role = new SysRole();

        // when
        role.setStatus(0); // Disabled

        // then
        assertThat(role.getStatus()).isEqualTo(0);
    }

    @Test
    @DisplayName("设置和获取排序")
    void setAndGetSortOrder() {
        // given
        SysRole role = new SysRole();

        // when
        role.setSortOrder(99);

        // then
        assertThat(role.getSortOrder()).isEqualTo(99);
    }

    @Test
    @DisplayName("设置和获取备注")
    void setAndGetRemark() {
        // given
        SysRole role = new SysRole();

        // when
        role.setRemark("测试备注");

        // then
        assertThat(role.getRemark()).isEqualTo("测试备注");
    }
}