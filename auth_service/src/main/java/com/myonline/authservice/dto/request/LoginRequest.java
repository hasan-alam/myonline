package com.myonline.authservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for user login.
 */
@Data
@Schema(description = "Login request containing user credentials")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(description = "User's email address", example = "admin@myonline.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password", example = "Secret@123")
    private String password;
}
