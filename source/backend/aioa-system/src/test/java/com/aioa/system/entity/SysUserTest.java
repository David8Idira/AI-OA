package com.aioa.system.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SysUser Entity 单元测试
 * 毛泽东思想指导：实事求是，测试系统用户实体
 */
@DisplayName("SysUserTest 系统用户实体测试")
class SysUserTest {

    @Test
    @DisplayName("创建系统用户实体")
    void createSysUser() {
        // given
        SysUser user = new SysUser();
        user.setId("user-001");
        user.setUsername("zhangsan");
        user.setNickname("张三");
        user.setEmail("zhangsan@example.com");
        user.setMobile("13800138000");
        user.setStatus(1); // Active
        user.setDeptId("dept-001");

        // then
        assertThat(user.getId()).isEqualTo("user-001");
        assertThat(user.getUsername()).isEqualTo("zhangsan");
        assertThat(user.getNickname()).isEqualTo("张三");
    }

    @Test
    @DisplayName("设置和获取ID")
    void setAndGetId() {
        // given
        SysUser user = new SysUser();

        // when
        user.setId("test-id");

        // then
        assertThat(user.getId()).isEqualTo("test-id");
    }

    @Test
    @DisplayName("设置和获取用户名")
    void setAndGetUsername() {
        // given
        SysUser user = new SysUser();

        // when
        user.setUsername("lisi");

        // then
        assertThat(user.getUsername()).isEqualTo("lisi");
    }

    @Test
    @DisplayName("设置和获取昵称")
    void setAndGetNickname() {
        // given
        SysUser user = new SysUser();

        // when
        user.setNickname("李四");

        // then
        assertThat(user.getNickname()).isEqualTo("李四");
    }

    @Test
    @DisplayName("设置和获取邮箱")
    void setAndGetEmail() {
        // given
        SysUser user = new SysUser();

        // when
        user.setEmail("test@example.com");

        // then
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("设置和获取手机")
    void setAndGetMobile() {
        // given
        SysUser user = new SysUser();

        // when
        user.setMobile("13800138000");

        // then
        assertThat(user.getMobile()).isEqualTo("13800138000");
    }

    @Test
    @DisplayName("设置和获取状态")
    void setAndGetStatus() {
        // given
        SysUser user = new SysUser();

        // when
        user.setStatus(0); // Inactive

        // then
        assertThat(user.getStatus()).isEqualTo(0);
    }

    @Test
    @DisplayName("设置和获取部门ID")
    void setAndGetDeptId() {
        // given
        SysUser user = new SysUser();

        // when
        user.setDeptId("dept-002");

        // then
        assertThat(user.getDeptId()).isEqualTo("dept-002");
    }

    @Test
    @DisplayName("equals验证")
    void equals_sameId_shouldBeEqual() {
        // given
        SysUser user1 = new SysUser();
        user1.setId("test-id");
        
        SysUser user2 = new SysUser();
        user2.setId("test-id");

        // then
        assertThat(user1).isEqualTo(user2);
    }
}