package com.aioa.system.controller;

import com.aioa.common.result.Result;
import com.aioa.system.dto.LoginDTO;
import com.aioa.system.dto.RegisterDTO;
import com.aioa.system.dto.UpdatePasswordDTO;
import com.aioa.system.entity.SysUser;
import com.aioa.system.service.SysUserService;
import com.aioa.system.vo.UserVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SysUserController单元测试
 * 毛主席思想指导：实事求是，测试所有用户相关接口
 */
@WebMvcTest(SysUserController.class)
@DisplayName("SysUserController单元测试")
class SysUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SysUserService userService;

    private UserVO testUserVO;
    private SysUser testSysUser;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "测试用户";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 准备测试数据 - 实事求是
        testUserVO = new UserVO();
        testUserVO.setUserId(TEST_USER_ID.toString());
        testUserVO.setUsername(TEST_USERNAME);
        testUserVO.setNickname(TEST_NICKNAME);
        testUserVO.setEmail("test@example.com");
        testUserVO.setToken("test-jwt-token");

        testSysUser = new SysUser();
        testSysUser.setId(TEST_USER_ID);
        testSysUser.setUsername(TEST_USERNAME);
        testSysUser.setNickname(TEST_NICKNAME);
        testSysUser.setEmail("test@example.com");
        testSysUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/v1/users/login - 有效凭证登录应返回200和UserVO")
    void login_withValidCredentials_shouldReturn200AndUserVO() throws Exception {
        // given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword(TEST_PASSWORD);
        
        when(userService.login(TEST_USERNAME, TEST_PASSWORD)).thenReturn(testUserVO);

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.nickname").value(TEST_NICKNAME))
                .andExpect(jsonPath("$.data.token").value("test-jwt-token"));

        verify(userService, times(1)).login(TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    @DisplayName("POST /api/v1/users/login - 无效凭证登录应返回错误")
    void login_withInvalidCredentials_shouldReturnError() throws Exception {
        // given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword("wrongpassword");
        
        when(userService.login(TEST_USERNAME, "wrongpassword"))
                .thenThrow(new RuntimeException("用户名或密码错误"));

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        verify(userService, times(1)).login(TEST_USERNAME, "wrongpassword");
    }

    @Test
    @DisplayName("POST /api/v1/users/login - 空用户名应返回400")
    void login_withEmptyUsername_shouldReturn400() throws Exception {
        // given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("");
        loginDTO.setPassword(TEST_PASSWORD);

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/v1/users/login - 空密码应返回400")
    void login_withEmptyPassword_shouldReturn400() throws Exception {
        // given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword("");

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/v1/users/register - 有效数据注册应返回200和用户信息")
    void register_withValidData_shouldReturn200AndUser() throws Exception {
        // given
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(TEST_USERNAME);
        registerDTO.setPassword(TEST_PASSWORD);
        registerDTO.setNickname(TEST_NICKNAME);
        
        when(userService.register(TEST_USERNAME, TEST_PASSWORD, TEST_NICKNAME))
                .thenReturn(testSysUser);

        // when & then
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.nickname").value(TEST_NICKNAME));

        verify(userService, times(1)).register(TEST_USERNAME, TEST_PASSWORD, TEST_NICKNAME);
    }

    @Test
    @DisplayName("POST /api/v1/users/register - 用户名已存在应返回错误")
    void register_withExistingUsername_shouldReturnError() throws Exception {
        // given
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(TEST_USERNAME);
        registerDTO.setPassword(TEST_PASSWORD);
        registerDTO.setNickname(TEST_NICKNAME);
        
        when(userService.register(TEST_USERNAME, TEST_PASSWORD, TEST_NICKNAME))
                .thenThrow(new RuntimeException("用户名已存在"));

        // when & then
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户名已存在"));

        verify(userService, times(1)).register(TEST_USERNAME, TEST_PASSWORD, TEST_NICKNAME);
    }

    @Test
    @DisplayName("POST /api/v1/users/register - 弱密码应返回400")
    void register_withWeakPassword_shouldReturn400() throws Exception {
        // given
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(TEST_USERNAME);
        registerDTO.setPassword("123"); // 弱密码
        registerDTO.setNickname(TEST_NICKNAME);

        // when & then
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("GET /api/v1/users/current - 有效用户ID应返回200和用户信息")
    void getCurrentUser_withValidUserId_shouldReturn200AndUserInfo() throws Exception {
        // given
        when(userService.getUserInfo(TEST_USER_ID.toString())).thenReturn(testUserVO);

        // when & then
        mockMvc.perform(get("/api/v1/users/current")
                .requestAttr("userId", TEST_USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.nickname").value(TEST_NICKNAME));

        verify(userService, times(1)).getUserInfo(TEST_USER_ID.toString());
    }

    @Test
    @DisplayName("GET /api/v1/users/current - 无效用户ID应返回错误")
    void getCurrentUser_withInvalidUserId_shouldReturnError() throws Exception {
        // given
        when(userService.getUserInfo("999"))
                .thenThrow(new RuntimeException("用户不存在"));

        // when & then
        mockMvc.perform(get("/api/v1/users/current")
                .requestAttr("userId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户不存在"));

        verify(userService, times(1)).getUserInfo("999");
    }

    @Test
    @DisplayName("PUT /api/v1/users/password - 有效密码更新应返回200")
    void updatePassword_withValidData_shouldReturn200() throws Exception {
        // given
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword(TEST_PASSWORD);
        dto.setNewPassword("newPassword123");
        
        when(userService.updatePassword(TEST_USER_ID, TEST_PASSWORD, "newPassword123"))
                .thenReturn(true);

        // when & then
        mockMvc.perform(put("/api/v1/users/password")
                .requestAttr("userId", TEST_USER_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).updatePassword(TEST_USER_ID, TEST_PASSWORD, "newPassword123");
    }

    @Test
    @DisplayName("PUT /api/v1/users/password - 旧密码错误应返回错误")
    void updatePassword_withWrongOldPassword_shouldReturnError() throws Exception {
        // given
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword("wrongpassword");
        dto.setNewPassword("newPassword123");
        
        when(userService.updatePassword(TEST_USER_ID, "wrongpassword", "newPassword123"))
                .thenThrow(new RuntimeException("旧密码错误"));

        // when & then
        mockMvc.perform(put("/api/v1/users/password")
                .requestAttr("userId", TEST_USER_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("旧密码错误"));

        verify(userService, times(1)).updatePassword(TEST_USER_ID, "wrongpassword", "newPassword123");
    }

    @Test
    @DisplayName("GET /api/v1/users/permissions - 应返回200和权限列表")
    void getUserPermissions_shouldReturn200AndPermissions() throws Exception {
        // given
        when(userService.getUserPermissions(TEST_USER_ID.toString()))
                .thenReturn(java.util.Arrays.asList("admin", "user", "view"));

        // when & then
        mockMvc.perform(get("/api/v1/users/permissions")
                .requestAttr("userId", TEST_USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("admin"))
                .andExpect(jsonPath("$.data[1]").value("user"))
                .andExpect(jsonPath("$.data[2]").value("view"));

        verify(userService, times(1)).getUserPermissions(TEST_USER_ID.toString());
    }

    @Test
    @DisplayName("GET /api/v1/users/menus - 应返回200和菜单列表")
    void getUserMenus_shouldReturn200AndMenus() throws Exception {
        // given
        when(userService.getUserMenus(TEST_USER_ID.toString()))
                .thenReturn(java.util.Arrays.asList("Dashboard", "Users", "Settings"));

        // when & then
        mockMvc.perform(get("/api/v1/users/menus")
                .requestAttr("userId", TEST_USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("Dashboard"))
                .andExpect(jsonPath("$.data[1]").value("Users"))
                .andExpect(jsonPath("$.data[2]").value("Settings"));

        verify(userService, times(1)).getUserMenus(TEST_USER_ID.toString());
    }

    /**
     * 边界测试
     */
    @Test
    @DisplayName("POST /api/v1/users/login - 超长用户名应返回400")
    void login_withTooLongUsername_shouldReturn400() throws Exception {
        // given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("a".repeat(256));
        loginDTO.setPassword(TEST_PASSWORD);

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/v1/users/register - 空用户名应返回400")
    void register_withEmptyUsername_shouldReturn400() throws Exception {
        // given
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("");
        registerDTO.setPassword(TEST_PASSWORD);
        registerDTO.setNickname(TEST_NICKNAME);

        // when & then
        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(anyString(), anyString(), anyString());
    }
}