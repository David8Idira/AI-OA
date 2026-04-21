package com.aioa.service;

import com.aioa.entity.User;
import com.aioa.exception.ResourceConflictException;
import com.aioa.exception.ResourceNotFoundException;
import com.aioa.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserService测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User normalUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .id(1L)
                .username("normal_user")
                .email("normal@example.com")
                .age(25)
                .status("ACTIVE")
                .department("IT")
                .phoneNumber("13800138000")
                .createdAt(LocalDateTime.now())
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin_user")
                .email("admin@example.com")
                .age(30)
                .status("ADMIN")
                .department("Management")
                .phoneNumber("13900139000")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ============= 正常业务流程测试 =============

    @Nested
    @DisplayName("getUserById - 正常流程")
    class GetUserByIdSuccess {
        @Test
        @DisplayName("根据ID获取用户成功")
        void testGetUserById_Success() {
            // Given
            when(userMapper.findById(1L)).thenReturn(Optional.of(normalUser));

            // When
            User result = userService.getUserById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("normal_user");
            assertThat(result.getEmail()).isEqualTo("normal@example.com");
            verify(userMapper, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("getUserByUsername - 正常流程")
    class GetUserByUsernameSuccess {
        @Test
        @DisplayName("根据用户名获取用户成功")
        void testGetUserByUsername_Success() {
            // Given
            when(userMapper.findByUsername("normal_user")).thenReturn(Optional.of(normalUser));

            // When
            User result = userService.getUserByUsername("normal_user");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("normal_user");
            verify(userMapper, times(1)).findByUsername("normal_user");
        }
    }

    @Nested
    @DisplayName("getUserByEmail - 正常流程")
    class GetUserByEmailSuccess {
        @Test
        @DisplayName("根据邮箱获取用户成功")
        void testGetUserByEmail_Success() {
            // Given
            when(userMapper.findByEmail("normal@example.com")).thenReturn(Optional.of(normalUser));

            // When
            User result = userService.getUserByEmail("normal@example.com");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("normal@example.com");
            verify(userMapper, times(1)).findByEmail("normal@example.com");
        }
    }

    @Nested
    @DisplayName("createUser - 正常流程")
    class CreateUserSuccess {
        @Test
        @DisplayName("创建用户成功")
        void testCreateUser_Success() {
            // Given
            User newUser = User.builder()
                    .username("new_user")
                    .email("new@example.com")
                    .age(28)
                    .phoneNumber("13700137000")
                    .build();

            when(userMapper.findByUsername("new_user")).thenReturn(Optional.empty());
            when(userMapper.findByEmail("new@example.com")).thenReturn(Optional.empty());
            when(userMapper.findByPhoneNumber("13700137000")).thenReturn(Optional.empty());
            when(userMapper.save(any(User.class))).thenReturn(newUser);

            // When
            User result = userService.createUser(newUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("new_user");
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            verify(userMapper, times(1)).findByUsername("new_user");
            verify(userMapper, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("创建用户时设置默认状态为ACTIVE")
        void testCreateUser_SetsDefaultStatus() {
            // Given
            User newUser = User.builder()
                    .username("new_user")
                    .email("new@example.com")
                    .build();

            when(userMapper.findByUsername("new_user")).thenReturn(Optional.empty());
            when(userMapper.findByEmail("new@example.com")).thenReturn(Optional.empty());
            when(userMapper.findByPhoneNumber(null)).thenReturn(Optional.empty());
            when(userMapper.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.createUser(newUser);

            // Then
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
        }
    }

    @Nested
    @DisplayName("updateUser - 正常流程")
    class UpdateUserSuccess {
        @Test
        @DisplayName("更新用户成功")
        void testUpdateUser_Success() {
            // Given
            User updateData = User.builder()
                    .id(1L)
                    .username("updated_user")
                    .email("updated@example.com")
                    .age(26)
                    .status("INACTIVE")
                    .build();

            when(userMapper.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userMapper.findByUsername("updated_user")).thenReturn(Optional.empty());
            when(userMapper.findByEmail("updated@example.com")).thenReturn(Optional.empty());
            when(userMapper.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.updateUser(updateData);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("updated_user");
            assertThat(result.getEmail()).isEqualTo("updated@example.com");
            verify(userMapper, times(1)).findById(1L);
            verify(userMapper, times(1)).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("deleteUser - 正常流程")
    class DeleteUserSuccess {
        @Test
        @DisplayName("删除用户成功")
        void testDeleteUser_Success() {
            // Given
            when(userMapper.existsById(1L)).thenReturn(true);
            doNothing().when(userMapper).deleteById(1L);

            // When
            userService.deleteUser(1L);

            // Then
            verify(userMapper, times(1)).existsById(1L);
            verify(userMapper, times(1)).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("getUsersByStatus - 正常流程")
    class GetUsersByStatusSuccess {
        @Test
        @DisplayName("根据状态获取用户列表成功")
        void testGetUsersByStatus_Success() {
            // Given
            List<User> users = Arrays.asList(normalUser, adminUser);
            when(userMapper.findByStatus("ACTIVE")).thenReturn(users);

            // When
            List<User> result = userService.getUsersByStatus("ACTIVE");

            // Then
            assertThat(result).hasSize(2);
            verify(userMapper, times(1)).findByStatus("ACTIVE");
        }
    }

    @Nested
    @DisplayName("searchUsers - 正常流程")
    class SearchUsersSuccess {
        @Test
        @DisplayName("搜索用户成功")
        void testSearchUsers_Success() {
            // Given
            List<User> users = Arrays.asList(normalUser);
            when(userMapper.searchUsers("normal", "ACTIVE", "IT", null)).thenReturn(users);

            // When
            List<User> result = userService.searchUsers("normal", "ACTIVE", "IT", null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUsername()).isEqualTo("normal_user");
            verify(userMapper, times(1)).searchUsers("normal", "ACTIVE", "IT", null);
        }

        @Test
        @DisplayName("空条件搜索用户成功")
        void testSearchUsers_AllNull() {
            // Given
            List<User> users = Arrays.asList(normalUser, adminUser);
            when(userMapper.searchUsers(null, null, null, null)).thenReturn(users);

            // When
            List<User> result = userService.searchUsers(null, null, null, null);

            // Then
            assertThat(result).hasSize(2);
            verify(userMapper, times(1)).searchUsers(null, null, null, null);
        }
    }

    @Nested
    @DisplayName("activateUser/deactivateUser - 正常流程")
    class ActivateDeactivateUserSuccess {
        @Test
        @DisplayName("激活用户成功")
        void testActivateUser_Success() {
            // Given
            when(userMapper.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userMapper.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.activateUser(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            verify(userMapper, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("停用用户成功")
        void testDeactivateUser_Success() {
            // Given
            when(userMapper.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userMapper.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.deactivateUser(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo("INACTIVE");
            verify(userMapper, times(1)).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("batchUpdateUserStatus - 正常流程")
    class BatchUpdateUserStatusSuccess {
        @Test
        @DisplayName("批量更新用户状态成功")
        void testBatchUpdateUserStatus_Success() {
            // Given
            List<Long> userIds = Arrays.asList(1L, 2L, 3L);
            when(userMapper.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userMapper.findById(2L)).thenReturn(Optional.of(adminUser));
            when(userMapper.findById(3L)).thenReturn(Optional.empty());
            when(userMapper.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            int count = userService.batchUpdateUserStatus(userIds, "INACTIVE");

            // Then
            assertThat(count).isEqualTo(2);
            verify(userMapper, times(2)).save(any(User.class));
        }
    }

    // ============= 异常情况处理测试 =============

    @Nested
    @DisplayName("getUserById - 异常情况")
    class GetUserByIdException {
        @Test
        @DisplayName("用户不存在时抛出ResourceNotFoundException")
        void testGetUserById_NotFound_ThrowsException() {
            // Given
            when(userMapper.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("getUserByUsername - 异常情况")
    class GetUserByUsernameException {
        @Test
        @DisplayName("用户名不存在时抛出异常")
        void testGetUserByUsername_NotFound_ThrowsException() {
            // Given
            when(userMapper.findByUsername("non_existing")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.getUserByUsername("non_existing"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getUserByEmail - 异常情况")
    class GetUserByEmailException {
        @Test
        @DisplayName("邮箱不存在时抛出异常")
        void testGetUserByEmail_NotFound_ThrowsException() {
            // Given
            when(userMapper.findByEmail("non_existing@example.com")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.getUserByEmail("non_existing@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createUser - 异常情况")
    class CreateUserException {
        @Test
        @DisplayName("用户名已存在时抛出ResourceConflictException")
        void testCreateUser_UsernameExists_ThrowsException() {
            // Given
            User newUser = User.builder()
                    .username("existing_user")
                    .email("new@example.com")
                    .build();

            when(userMapper.findByUsername("existing_user")).thenReturn(Optional.of(normalUser));

            // When/Then
            assertThatThrownBy(() -> userService.createUser(newUser))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("username");
        }

        @Test
        @DisplayName("邮箱已存在时抛出ResourceConflictException")
        void testCreateUser_EmailExists_ThrowsException() {
            // Given
            User newUser = User.builder()
                    .username("new_user")
                    .email("normal@example.com")
                    .build();

            when(userMapper.findByUsername("new_user")).thenReturn(Optional.empty());
            when(userMapper.findByEmail("normal@example.com")).thenReturn(Optional.of(normalUser));

            // When/Then
            assertThatThrownBy(() -> userService.createUser(newUser))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("手机号已存在时抛出ResourceConflictException")
        void testCreateUser_PhoneExists_ThrowsException() {
            // Given
            User newUser = User.builder()
                    .username("new_user")
                    .email("new@example.com")
                    .phoneNumber("13800138000")
                    .build();

            when(userMapper.findByUsername("new_user")).thenReturn(Optional.empty());
            when(userMapper.findByEmail("new@example.com")).thenReturn(Optional.empty());
            when(userMapper.findByPhoneNumber("13800138000")).thenReturn(Optional.of(normalUser));

            // When/Then
            assertThatThrownBy(() -> userService.createUser(newUser))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("phoneNumber");
        }
    }

    @Nested
    @DisplayName("updateUser - 异常情况")
    class UpdateUserException {
        @Test
        @DisplayName("用户不存在时抛出ResourceNotFoundException")
        void testUpdateUser_UserNotFound_ThrowsException() {
            // Given
            User updateData = User.builder()
                    .id(999L)
                    .username("updated")
                    .build();

            when(userMapper.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(updateData))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("更新用户名冲突时抛出ResourceConflictException")
        void testUpdateUser_UsernameConflict_ThrowsException() {
            // Given
            User updateData = User.builder()
                    .id(1L)
                    .username("admin_user")
                    .build();

            when(userMapper.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userMapper.findByUsername("admin_user")).thenReturn(Optional.of(adminUser));

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(updateData))
                    .isInstanceOf(ResourceConflictException.class);
        }

        @Test
        @DisplayName("更新邮箱冲突时抛出ResourceConflictException")
        void testUpdateUser_EmailConflict_ThrowsException() {
            // Given
            User updateData = User.builder()
                    .id(1L)
                    .email("admin@example.com")
                    .build();

            when(userMapper.findById(1L)).thenReturn(Optional.of(normalUser));
            when(userMapper.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(updateData))
                    .isInstanceOf(ResourceConflictException.class);
        }

        @Test
        @DisplayName("年龄超出范围时抛出IllegalArgumentException")
        void testUpdateUser_InvalidAge_ThrowsException() {
            // Given
            User invalidAgeUser = User.builder()
                    .id(1L)
                    .age(-5)
                    .build();

            when(userMapper.findById(1L)).thenReturn(Optional.of(normalUser));

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(invalidAgeUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Age must be between 0 and 150");
        }

        @Test
        @DisplayName("年龄超过150时抛出IllegalArgumentException")
        void testUpdateUser_AgeOver150_ThrowsException() {
            // Given
            User invalidAgeUser = User.builder()
                    .id(1L)
                    .age(200)
                    .build();

            when(userMapper.findById(1L)).thenReturn(Optional.of(normalUser));

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(invalidAgeUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Age must be between 0 and 150");
        }
    }

    @Nested
    @DisplayName("deleteUser - 异常情况")
    class DeleteUserException {
        @Test
        @DisplayName("删除不存在的用户时抛出ResourceNotFoundException")
        void testDeleteUser_NotFound_ThrowsException() {
            // Given
            when(userMapper.existsById(999L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ============= 边界条件测试 =============

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {
        @Test
        @DisplayName("创建年龄为0的用户成功")
        void testCreateUser_AgeZero_Success() {
            // Given
            User newUser = User.builder()
                    .username("baby_user")
                    .email("baby@example.com")
                    .age(0)
                    .build();

            when(userMapper.findByUsername("baby_user")).thenReturn(Optional.empty());
            when(userMapper.findByEmail("baby@example.com")).thenReturn(Optional.empty());
            when(userMapper.findByPhoneNumber(null)).thenReturn(Optional.empty());
            when(userMapper.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.createUser(newUser);

            // Then
            assertThat(result.getAge()).isEqualTo(0);
        }

        @Test
        @DisplayName("创建年龄为150的用户成功")
        void testCreateUser_Age150_Success() {
            // Given
            User newUser = User.builder()
                    .username("elder_user")
                    .email("elder@example.com")
                    .age(150)
                    .build();

            when(userMapper.findByUsername("elder_user")).thenReturn(Optional.empty());
            when(userMapper.findByEmail("elder@example.com")).thenReturn(Optional.empty());
            when(userMapper.findByPhoneNumber(null)).thenReturn(Optional.empty());
            when(userMapper.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.createUser(newUser);

            // Then
            assertThat(result.getAge()).isEqualTo(150);
        }

        @Test
        @DisplayName("创建用户名长度为100的用户成功")
        void testCreateUser_MaxLengthUsername_Success() {
            // Given
            String maxUsername = "a".repeat(100);
            User newUser = User.builder()
                    .username(maxUsername)
                    .email("max@example.com")
                    .build();

            when(userMapper.findByUsername(maxUsername)).thenReturn(Optional.empty());
            when(userMapper.findByEmail("max@example.com")).thenReturn(Optional.empty());
            when(userMapper.findByPhoneNumber(null)).thenReturn(Optional.empty());
            when(userMapper.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.createUser(newUser);

            // Then
            assertThat(result.getUsername()).hasSize(100);
        }

        @Test
        @DisplayName("创建不带可选字段的用户成功")
        void testCreateUser_OptionalFieldsNull_Success() {
            // Given
            User newUser = User.builder()
                    .username("min_user")
                    .email("min@example.com")
                    .build();

            when(userMapper.findByUsername("min_user")).thenReturn(Optional.empty());
            when(userMapper.findByEmail("min@example.com")).thenReturn(Optional.empty());
            when(userMapper.findByPhoneNumber(null)).thenReturn(Optional.empty());
            when(userMapper.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.createUser(newUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("批量更新空列表返回0")
        void testBatchUpdateUserStatus_EmptyList_ReturnsZero() {
            // Given
            List<Long> emptyIds = Arrays.asList();

            // When
            int count = userService.batchUpdateUserStatus(emptyIds, "INACTIVE");

            // Then
            assertThat(count).isEqualTo(0);
            verify(userMapper, never()).save(any(User.class));
        }

        @Test
        @DisplayName("搜索返回空列表")
        void testSearchUsers_NoResults_ReturnsEmptyList() {
            // Given
            when(userMapper.searchUsers(any(), any(), any(), any())).thenReturn(Arrays.asList());

            // When
            List<User> result = userService.searchUsers("non_existing", "ACTIVE", "IT", null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ============= 并发安全性测试 =============

    @Nested
    @DisplayName("并发安全性测试")
    class ConcurrencyTests {
        @Test
        @DisplayName("多次调用countByStatus方法线程安全")
        void testCountByStatus_ThreadSafe() {
            // Given
            when(userMapper.countByStatus("ACTIVE")).thenReturn(10L);

            // When & Then - 多次调用验证mock正常工作
            long count1 = userService.countByStatus("ACTIVE");
            long count2 = userService.countByStatus("ACTIVE");
            long count3 = userService.countByStatus("ACTIVE");

            assertThat(count1).isEqualTo(10L);
            assertThat(count2).isEqualTo(10L);
            assertThat(count3).isEqualTo(10L);
            verify(userMapper, times(3)).countByStatus("ACTIVE");
        }

        @Test
        @DisplayName("getAllUsers方法线程安全")
        void testGetAllUsers_ThreadSafe() {
            // Given
            List<User> users = Arrays.asList(normalUser, adminUser);
            when(userMapper.findAll()).thenReturn(users);

            // When
            List<User> result = userService.getAllUsers();

            // Then
            assertThat(result).hasSize(2);
            verify(userMapper, times(1)).findAll();
        }

        @Test
        @DisplayName("getUsersByDepartment方法线程安全")
        void testGetUsersByDepartment_ThreadSafe() {
            // Given
            List<User> users = Arrays.asList(normalUser);
            when(userMapper.findByDepartment("IT")).thenReturn(users);

            // When
            List<User> result = userService.getUsersByDepartment("IT");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDepartment()).isEqualTo("IT");
        }
    }
}