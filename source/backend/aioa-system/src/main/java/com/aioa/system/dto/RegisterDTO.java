package com.aioa.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Register DTO
 */
@Data
@Schema(description = "Registration Request")
public class RegisterDTO {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    @Schema(description = "Username")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 20, message = "Password must be 6-20 characters")
    @Schema(description = "Password")
    private String password;

    @Schema(description = "Nickname")
    private String nickname;
}
