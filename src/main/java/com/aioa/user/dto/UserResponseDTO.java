package com.aioa.user.dto;

import lombok.Data;

/**
 * 用户响应DTO
 */
@Data
public class UserResponseDTO {

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String avatar;

    private Long departmentId;

    private String departmentName;

    private Integer status;

    private String createTime;

    private String updateTime;
}
