package com.aioa.user.service;

import com.aioa.user.dto.UserDTO;
import com.aioa.user.dto.UserResponseDTO;
import com.aioa.user.entity.User;
import com.aioa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * 创建用户
     */
    public UserResponseDTO createUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword()) // 实际应加密
                .realName(dto.getRealName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .avatar(dto.getAvatar())
                .departmentId(dto.getDepartmentId())
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        User saved = userRepository.save(user);
        return convertToResponseDTO(saved);
    }

    /**
     * 更新用户
     */
    public UserResponseDTO updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(dto.getPassword()); // 实际应加密
        }
        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getDepartmentId() != null) user.setDepartmentId(dto.getDepartmentId());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());

        User saved = userRepository.save(user);
        return convertToResponseDTO(saved);
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    /**
     * 根据ID查找用户
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return convertToResponseDTO(user);
    }

    /**
     * 获取所有用户
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据用户名查找用户
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return convertToResponseDTO(user);
    }

    /**
     * 转换实体为响应DTO
     */
    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setDepartmentId(user.getDepartmentId());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().toString() : null);
        dto.setUpdateTime(user.getUpdateTime() != null ? user.getUpdateTime().toString() : null);
        return dto;
    }
}
