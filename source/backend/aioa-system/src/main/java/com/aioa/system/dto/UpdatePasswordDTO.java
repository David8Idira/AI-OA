package com.aioa.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Update Password DTO
 */
@Data
@Schema(description = "Update Password Request")
public class UpdatePasswordDTO {

    @NotBlank(message = "Old password cannot be blank")
    @Schema(description = "Old password")
    private String oldPassword;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, max = 20, message = "Password must be 6-20 characters")
    @Schema(description = "New password")
    private String newPassword;
}
