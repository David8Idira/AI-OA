package com.aioa.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户创建/更新DTO
 */
@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度应为3-50个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度应为6-100个字符")
    private String password;

    @Size(max = 100, message = "真实姓名长度不应超过100个字符")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号长度不应超过20个字符")
    private String phone;

    private String avatar;

    private Long departmentId;

    private Integer status;
}
