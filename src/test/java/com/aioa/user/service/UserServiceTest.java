package com.aioa.user.service;

import com.aioa.user.dto.UserDTO;
import com.aioa.user.dto.UserResponseDTO;
import com.aioa.user.entity.User;
import com.aioa.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    private User makeUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setRealName("用户" + id);
        u.setStatus(1);
        u.setEmail(username + "@example.com");
        return u;
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("成功创建用户")
        void createUser_Success() {
            UserDTO dto = new UserDTO();
            dto.setUsername("newuser");
            dto.setRealName("新用户");
            dto.setEmail("new@example.com");
            dto.setPhone("13800000000");

            User saved = makeUser(1L, "newuser");
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(saved);

            UserResponseDTO result = userService.createUser(dto);
            assertThat(result.getUsername()).isEqualTo("newuser");
        }

        @Test
        @DisplayName("用户名已存在 - 抛异常")
        void createUser_Conflict() {
            UserDTO dto = new UserDTO();
            dto.setUsername("existing");
            when(userRepository.existsByUsername("existing")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("用户名已存在");
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("成功获取用户")
        void getUserById_Success() {
            User u = makeUser(1L, "zhangsan");
            when(userRepository.findById(1L)).thenReturn(Optional.of(u));

            UserResponseDTO result = userService.getUserById(1L);
            assertThat(result.getUsername()).isEqualTo("zhangsan");
        }
    }
}
