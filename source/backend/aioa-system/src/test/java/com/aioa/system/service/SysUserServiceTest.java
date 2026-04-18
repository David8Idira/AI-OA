package com.aioa.system.service;

import com.aioa.system.entity.SysUser;
import com.aioa.system.vo.UserVO;
import com.aioa.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * SysUserService单元测试
 * 基于毛主席思想：实事求是，全面测试，保证质量
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysUserService单元测试")
class SysUserServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    private SysUser testUser;
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "测试用户";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPasswordHash";
    private static final String TEST_TOKEN = "test-jwt-token";

    @BeforeEach
    void setUp() {
        // 准备测试数据 - 实事求是：基于真实业务场景
        testUser = new SysUser();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);
        testUser.setPassword(ENCODED_PASSWORD);
        testUser.setNickname(TEST_NICKNAME);
        testUser.setEmail("testuser@example.com");
        testUser.setPhone("13800138000");
        testUser.setStatus(1);
        testUser.setIsDeleted(0);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("should_returnUserVO_when_loginWithValidCredentials - 有效凭证登录应返回UserVO")
    void should_returnUserVO_when_loginWithValidCredentials() {
        // given - 准备测试数据
        when(sysUserMapper.selectByUsername(TEST_USERNAME)).thenReturn(testUser);
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        
        // when - 执行测试动作
        UserVO result = sysUserService.login(TEST_USERNAME, TEST_PASSWORD);
        
        // then - 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getNickname()).isEqualTo(TEST_NICKNAME);
        
        // 验证Mock交互
        verify(sysUserMapper, times(1)).selectByUsername(TEST_USERNAME);
        verify(passwordEncoder, times(1)).matches(TEST_PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("should_throwException_when_loginWithInvalidUsername - 无效用户名登录应抛出异常")
    void should_throwException_when_loginWithInvalidUsername() {
        // given - 准备测试数据（用户不存在）
        when(sysUserMapper.selectByUsername(TEST_USERNAME)).thenReturn(null);
        
        // when & then - 执行并验证异常
        assertThatThrownBy(() -> sysUserService.login(TEST_USERNAME, TEST_PASSWORD))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名或密码错误");
        
        verify(sysUserMapper, times(1)).selectByUsername(TEST_USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("should_throwException_when_loginWithInvalidPassword - 无效密码登录应抛出异常")
    void should_throwException_when_loginWithInvalidPassword() {
        // given - 准备测试数据（密码不匹配）
        when(sysUserMapper.selectByUsername(TEST_USERNAME)).thenReturn(testUser);
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);
        
        // when & then - 执行并验证异常
        assertThatThrownBy(() -> sysUserService.login(TEST_USERNAME, TEST_PASSWORD))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名或密码错误");
        
        verify(sysUserMapper, times(1)).selectByUsername(TEST_USERNAME);
        verify(passwordEncoder, times(1)).matches(TEST_PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("should_throwException_when_loginWithDisabledUser - 禁用用户登录应抛出异常")
    void should_throwException_when_loginWithDisabledUser() {
        // given - 准备测试数据（用户被禁用）
        testUser.setStatus(0); // 状态0表示禁用
        when(sysUserMapper.selectByUsername(TEST_USERNAME)).thenReturn(testUser);
        
        // when & then - 执行并验证异常
        assertThatThrownBy(() -> sysUserService.login(TEST_USERNAME, TEST_PASSWORD))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户已被禁用");
        
        verify(sysUserMapper, times(1)).selectByUsername(TEST_USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("should_returnUserVO_when_getUserInfoWithValidUserId - 有效用户ID获取用户信息应返回UserVO")
    void should_returnUserVO_when_getUserInfoWithValidUserId() {
        // given - 准备测试数据
        when(sysUserMapper.selectById(TEST_USER_ID)).thenReturn(testUser);
        
        // when - 执行测试动作
        UserVO result = sysUserService.getUserInfo(TEST_USER_ID.toString());
        
        // then - 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID.toString());
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getNickname()).isEqualTo(TEST_NICKNAME);
        
        verify(sysUserMapper, times(1)).selectById(TEST_USER_ID);
    }

    @Test
    @DisplayName("should_throwException_when_getUserInfoWithInvalidUserId - 无效用户ID获取用户信息应抛出异常")
    void should_throwException_when_getUserInfoWithInvalidUserId() {
        // given - 准备测试数据（用户不存在）
        when(sysUserMapper.selectById(TEST_USER_ID)).thenReturn(null);
        
        // when & then - 执行并验证异常
        assertThatThrownBy(() -> sysUserService.getUserInfo(TEST_USER_ID.toString()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");
        
        verify(sysUserMapper, times(1)).selectById(TEST_USER_ID);
    }

    @Test
    @DisplayName("should_returnSysUser_when_getUserByUsernameWithValidUsername - 有效用户名获取用户应返回SysUser")
    void should_returnSysUser_when_getUserByUsernameWithValidUsername() {
        // given - 准备测试数据
        when(sysUserMapper.selectByUsername(TEST_USERNAME)).thenReturn(testUser);
        
        // when - 执行测试动作
        SysUser result = sysUserService.getUserByUsername(TEST_USERNAME);
        
        // then - 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        
        verify(sysUserMapper, times(1)).selectByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("should_returnNull_when_getUserByUsernameWithInvalidUsername - 无效用户名获取用户应返回null")
    void should_returnNull_when_getUserByUsernameWithInvalidUsername() {
        // given - 准备测试数据（用户不存在）
        when(sysUserMapper.selectByUsername(TEST_USERNAME)).thenReturn(null);
        
        // when - 执行测试动作
        SysUser result = sysUserService.getUserByUsername(TEST_USERNAME);
        
        // then - 验证结果
        assertThat(result).isNull();
        
        verify(sysUserMapper, times(1)).selectByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("should_returnSysUser_when_registerWithValidData - 有效数据注册应返回SysUser")
    void should_returnSysUser_when_registerWithValidData() {
        // given - 准备测试数据
        when(sysUserMapper.selectByUsername(TEST_USERNAME)).thenReturn(null); // 用户名未被占用
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(sysUserMapper.insert(any(SysUser.class))).thenReturn(1); // 插入成功
        
        // when - 执行测试动作
        SysUser result = sysUserService.register(TEST_USERNAME, TEST_PASSWORD, TEST_NICKNAME);
        
        // then - 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getNickname()).isEqualTo(TEST_NICKNAME);
        
        // 验证密码被加密
        verify(passwordEncoder, times(1)).encode(TEST_PASSWORD);
        
        // 验证用户已保存
        verify(sysUserMapper, times(1)).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("should_throwException_when_registerWithExistingUsername - 已存在用户名注册应抛出异常")
    void should_throwException_when_registerWithExistingUsername() {
        // given - 准备测试数据（用户名已存在）
        when(sysUserMapper.selectByUsername(TEST_USERNAME)).thenReturn(testUser);
        
        // when & then - 执行并验证异常
        assertThatThrownBy(() -> sysUserService.register(TEST_USERNAME, TEST_PASSWORD, TEST_NICKNAME))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名已存在");
        
        verify(sysUserMapper, times(1)).selectByUsername(TEST_USERNAME);
        verify(passwordEncoder, never()).encode(anyString());
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("should_returnTrue_when_updatePasswordWithValidCredentials - 有效凭证更新密码应返回true")
    void should_returnTrue_when_updatePasswordWithValidCredentials() {
        // given - 准备测试数据
        String newPassword = "newPassword123";
        String encodedNewPassword = "$2a$10$newEncodedPasswordHash";
        
        when(sysUserMapper.selectById(TEST_USER_ID)).thenReturn(testUser);
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1); // 更新成功
        
        // when - 执行测试动作
        boolean result = sysUserService.updatePassword(TEST_USER_ID, TEST_PASSWORD, newPassword);
        
        // then - 验证结果
        assertThat(result).isTrue();
        
        // 验证旧密码检查
        verify(passwordEncoder, times(1)).matches(TEST_PASSWORD, ENCODED_PASSWORD);
        
        // 验证新密码加密
        verify(passwordEncoder, times(1)).encode(newPassword);
        
        // 验证用户更新
        verify(sysUserMapper, times(1)).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("should_throwException_when_updatePasswordWithInvalidOldPassword - 无效旧密码更新密码应抛出异常")
    void should_throwException_when_updatePasswordWithInvalidOldPassword() {
        // given - 准备测试数据（旧密码错误）
        String newPassword = "newPassword123";
        
        when(sysUserMapper.selectById(TEST_USER_ID)).thenReturn(testUser);
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);
        
        // when & then - 执行并验证异常
        assertThatThrownBy(() -> sysUserService.updatePassword(TEST_USER_ID, TEST_PASSWORD, newPassword))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("旧密码错误");
        
        verify(passwordEncoder, times(1)).matches(TEST_PASSWORD, ENCODED_PASSWORD);
        verify(passwordEncoder, never()).encode(anyString());
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("should_throwException_when_updatePasswordWithNonExistentUser - 不存在的用户更新密码应抛出异常")
    void should_throwException_when_updatePasswordWithNonExistentUser() {
        // given - 准备测试数据（用户不存在）
        String newPassword = "newPassword123";
        
        when(sysUserMapper.selectById(TEST_USER_ID)).thenReturn(null);
        
        // when & then - 执行并验证异常
        assertThatThrownBy(() -> sysUserService.updatePassword(TEST_USER_ID, TEST_PASSWORD, newPassword))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");
        
        verify(sysUserMapper, times(1)).selectById(TEST_USER_ID);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("边界测试：空用户名登录应抛出异常")
    void should_throwException_when_loginWithEmptyUsername() {
        // when & then - 执行并验证异常
        assertThatThrownBy(() -> sysUserService.login("", TEST_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名不能为空");
        
        verify(sysUserMapper, never()).selectByUsername(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("边界测试：空密码登录应抛出异常")
    void should_throwException_when_loginWithEmptyPassword() {
        // when & then - 执行并验证异常
        assertThatThrownBy(() -> sysUserService.login(TEST_USERNAME, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("密码不能为空");
        
        verify(sysUserMapper, never()).selectByUsername(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("边界测试：超长用户名注册应抛出异常")
    void should_throwException_when_registerWithTooLongUsername() {
        // given - 准备测试数据（超长用户名）
        String tooLongUsername = "a".repeat(256);
        
        // when & then - 执行并验证异常
        assertThatThrownBy(() -> sysUserService.register(tooLongUsername, TEST_PASSWORD, TEST_NICKNAME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名长度超过限制");
        
        verify(sysUserMapper, never()).selectByUsername(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("边界测试：弱密码注册应抛出异常")
    void should_throwException_when_registerWithWeakPassword() {
        // given - 准备测试数据（弱密码）
        String weakPassword = "123";
        
        // when & then - 执行并验证异常
        assertThatThrownBy(() -> sysUserService.register(TEST_USERNAME, weakPassword, TEST_NICKNAME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("密码强度不足");
        
        verify(sysUserMapper, never()).selectByUsername(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("性能测试：多次登录调用验证性能")
    void should_handleMultipleLoginCalls_efficiently() {
        // given - 准备测试数据
        when(sysUserMapper.selectByUsername(TEST_USERNAME)).thenReturn(testUser);
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        
        // when - 执行多次调用
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            sysUserService.login(TEST_USERNAME, TEST_PASSWORD);
        }
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // then - 验证性能（100次调用应在1秒内完成）
        assertThat(executionTime).isLessThan(1000);
        
        // 验证调用次数
        verify(sysUserMapper, times(100)).selectByUsername(TEST_USERNAME);
        verify(passwordEncoder, times(100)).matches(TEST_PASSWORD, ENCODED_PASSWORD);
    }

    /**
     * 测试数据工厂方法 - 群众路线：复用测试数据
     */
    private SysUser createTestUser(Long id, String username, String password, String nickname) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setEmail(username + "@example.com");
        user.setPhone("13800138000");
        user.setStatus(1);
        user.setIsDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }
}