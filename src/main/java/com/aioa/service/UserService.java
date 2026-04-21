package com.aioa.service;

import com.aioa.entity.User;
import com.aioa.exception.ResourceConflictException;
import com.aioa.exception.ResourceNotFoundException;
import com.aioa.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        return userMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
    
    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        log.info("Fetching user by username: {}", username);
        return userMapper.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
    
    /**
     * 根据邮箱获取用户
     */
    public User getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        return userMapper.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
    
    /**
     * 创建用户
     */
    @Transactional
    public User createUser(User user) {
        log.info("Creating user: {}", user.getUsername());
        
        // 检查用户名是否已存在
        if (userMapper.findByUsername(user.getUsername()).isPresent()) {
            throw new ResourceConflictException("User", "username", user.getUsername());
        }
        
        // 检查邮箱是否已存在
        if (user.getEmail() != null && userMapper.findByEmail(user.getEmail()).isPresent()) {
            throw new ResourceConflictException("User", "email", user.getEmail());
        }
        
        // 检查手机号是否已存在
        if (user.getPhoneNumber() != null && 
            userMapper.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new ResourceConflictException("User", "phoneNumber", user.getPhoneNumber());
        }
        
        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        
        return userMapper.save(user);
    }
    
    /**
     * 更新用户
     */
    @Transactional
    public User updateUser(User user) {
        log.info("Updating user: {}", user.getId());
        
        // 检查用户是否存在
        User existingUser = userMapper.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", user.getId()));
        
        // 如果更新用户名，检查是否与其他用户冲突
        if (user.getUsername() != null && 
            !user.getUsername().equals(existingUser.getUsername())) {
            if (userMapper.findByUsername(user.getUsername()).isPresent()) {
                throw new ResourceConflictException("User", "username", user.getUsername());
            }
        }
        
        // 如果更新邮箱，检查是否与其他用户冲突
        if (user.getEmail() != null) {
            userMapper.findByEmail(user.getEmail())
                    .ifPresent(u -> {
                        if (!u.getId().equals(user.getId())) {
                            throw new ResourceConflictException("User", "email", user.getEmail());
                        }
                    });
        }
        
        // 更新用户信息
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }
        if (user.getAge() != null) {
            // 年龄验证
            if (user.getAge() < 0 || user.getAge() > 150) {
                throw new IllegalArgumentException("Age must be between 0 and 150");
            }
            existingUser.setAge(user.getAge());
        }
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }
        if (user.getDepartment() != null) {
            existingUser.setDepartment(user.getDepartment());
        }
        if (user.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }
        
        return userMapper.save(existingUser);
    }
    
    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        
        if (!userMapper.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        
        userMapper.deleteById(id);
    }
    
    /**
     * 根据状态获取用户列表
     */
    public List<User> getUsersByStatus(String status) {
        log.info("Fetching users by status: {}", status);
        return userMapper.findByStatus(status);
    }
    
    /**
     * 根据部门获取用户列表
     */
    public List<User> getUsersByDepartment(String department) {
        log.info("Fetching users by department: {}", department);
        return userMapper.findByDepartment(department);
    }
    
    /**
     * 搜索用户
     */
    public List<User> searchUsers(String username, String status, String department, Integer age) {
        log.info("Searching users with username: {}, status: {}, department: {}, age: {}", 
                username, status, department, age);
        return userMapper.searchUsers(username, status, department, age);
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userMapper.findAll();
    }
    
    /**
     * 统计用户数量
     */
    public long countByStatus(String status) {
        log.info("Counting users by status: {}", status);
        return userMapper.countByStatus(status);
    }
    
    /**
     * 批量更新用户状态
     */
    @Transactional
    public int batchUpdateUserStatus(List<Long> userIds, String status) {
        log.info("Batch updating user status for {} users to {}", userIds.size(), status);
        
        int count = 0;
        for (Long userId : userIds) {
            User user = userMapper.findById(userId).orElse(null);
            if (user != null) {
                user.setStatus(status);
                userMapper.save(user);
                count++;
            }
        }
        return count;
    }
    
    /**
     * 激活用户
     */
    @Transactional
    public User activateUser(Long id) {
        log.info("Activating user: {}", id);
        User user = getUserById(id);
        user.setStatus("ACTIVE");
        return userMapper.save(user);
    }
    
    /**
     * 停用用户
     */
    @Transactional
    public User deactivateUser(Long id) {
        log.info("Deactivating user: {}", id);
        User user = getUserById(id);
        user.setStatus("INACTIVE");
        return userMapper.save(user);
    }
}