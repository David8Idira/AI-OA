package com.aioa.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login DTO
 */
@Data
@Schema(description = "Login Request")
public class LoginDTO {

    @NotBlank(message = "Username cannot be blank")
    @Schema(description = "Username")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Schema(description = "Password")
    private String password;
}
