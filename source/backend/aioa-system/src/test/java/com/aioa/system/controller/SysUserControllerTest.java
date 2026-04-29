package com.aioa.system.controller;

import com.aioa.common.result.Result;
import com.aioa.system.dto.LoginDTO;
import com.aioa.system.dto.RegisterDTO;
import com.aioa.system.dto.UpdatePasswordDTO;
import com.aioa.system.entity.SysUser;
import com.aioa.system.service.SysUserService;
import com.aioa.system.vo.UserVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SysUserController 单元测试
 */
@DisplayName("SysUserControllerTest 系统用户控制器测试")
@SpringBootTest
@AutoConfigureMockMvc
class SysUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SysUserService userService;

    // ==================== Login Tests ====================

    @Test
    @DisplayName("登录成功 - 返回用户信息")
    void login_success() throws Exception {
        // given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("password123");
        UserVO userVO = new UserVO();
        userVO.setId("user-001");
        userVO.setUsername("admin");
        userVO.setNickname("管理员");
        when(userService.login("admin", "password123")).thenReturn(userVO);

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @DisplayName("登录失败 - 用户名或密码错误")
    void login_fail_wrongCredentials() throws Exception {
        // given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("wrongpassword");
        when(userService.login(anyString(), anyString())).thenReturn(null);

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("登录参数校验 - 用户名为空")
    void login_validation_usernameBlank() throws Exception {
        // given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("");
        loginDTO.setPassword("password123");

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Register Tests ====================

    @Test
    @DisplayName("注册成功 - 返回用户信息")
    void register_success() throws Exception {
        // given
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("password123");
        registerDTO.setNickname("新用户");
        SysUser user = new SysUser();
        user.setId("user-new");
        user.setUsername("newuser");
        when(userService.register("newuser", "password123", "新用户")).thenReturn(user);

        // when & then
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    @DisplayName("注册参数校验 - 密码为空")
    void register_validation_passwordBlank() throws Exception {
        // given
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("");
        registerDTO.setNickname("新用户");

        // when & then
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Get Current User Tests ====================

    @Test
    @DisplayName("获取当前用户信息成功")
    void getCurrentUser_success() throws Exception {
        // given
        UserVO userVO = new UserVO();
        userVO.setId("user-001");
        userVO.setUsername("admin");
        userVO.setNickname("管理员");
        when(userService.getUserInfo("user-001")).thenReturn(userVO);

        // when & then
        mockMvc.perform(get("/api/v1/users/current")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    @DisplayName("获取当前用户信息 - 用户不存在")
    void getCurrentUser_userNotFound() throws Exception {
        // given
        when(userService.getUserInfo("nonexist")).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/v1/users/current")
                        .requestAttr("userId", "nonexist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== Update Password Tests ====================

    @Test
    @DisplayName("修改密码成功")
    void updatePassword_success() throws Exception {
        // given
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword("oldpass");
        dto.setNewPassword("newpass");
        when(userService.updatePassword("user-001", "oldpass", "newpass")).thenReturn(true);

        // when & then
        mockMvc.perform(put("/api/v1/users/password")
                        .requestAttr("userId", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("修改密码失败 - 旧密码错误")
    void updatePassword_fail_wrongOldPassword() throws Exception {
        // given
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword("wrongold");
        dto.setNewPassword("newpass");
        when(userService.updatePassword("user-001", "wrongold", "newpass")).thenReturn(false);

        // when & then
        mockMvc.perform(put("/api/v1/users/password")
                        .requestAttr("userId", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== Get Permissions/Menus Tests ====================

    @Test
    @DisplayName("获取用户权限列表")
    void getUserPermissions_success() throws Exception {
        // given
        when(userService.getUserPermissions("user-001")).thenReturn(List.of("admin:read", "admin:write"));

        // when & then
        mockMvc.perform(get("/api/v1/users/permissions")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("获取用户菜单")
    void getUserMenus_success() throws Exception {
        // given
        when(userService.getUserMenus("user-001")).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/users/menus")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
