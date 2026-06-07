package com.myonline.authservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for updating a user's password.
 */
@Data
@Schema(description = "Request to update user password")
public class UpdatePasswordRequest {

    @NotBlank(message = "Current password is required")
    @Schema(description = "The user's current password", example = "OldSecret@123")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    @Schema(description = "The desired new password (minimum 8 characters)", example = "NewSecret@456")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Confirm new password (must match newPassword)", example = "NewSecret@456")
    private String confirmPassword;
}
