package com.aioa.mapper;

import com.aioa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户Mapper接口
 */
@Repository
public interface UserMapper extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    List<User> findByStatus(String status);
    
    List<User> findByDepartment(String department);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR u.username LIKE %:username%) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:department IS NULL OR u.department = :department)")
    List<User> searchUsers(
            @Param("username") String username,
            @Param("status") String status,
            @Param("department") String department,
            @Param("age") Integer age);
    
    long countByStatus(String status);
}