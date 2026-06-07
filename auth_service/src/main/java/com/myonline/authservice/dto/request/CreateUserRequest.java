package com.myonline.authservice.dto.request;

import com.myonline.authservice.enums.PortalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request payload for creating a new user.
 */
@Data
@Schema(description = "Request to create a new user account")
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "Invalid mobile number format")
    @Schema(description = "Mobile phone number", example = "+8801700000000")
    private String mobile;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Schema(description = "Email address (used for login)", example = "john@shopname.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Initial password (minimum 8 characters)", example = "Secret@123")
    private String password;

    @NotNull(message = "userFor is required")
    @Schema(description = "Portal the user belongs to (SHPADMP / SYSADMP / BOTH)", example = "SHPADMP")
    private PortalType userFor;

    @Schema(description = "Tenant/shop ID (required for SHPADMP users; null for SYSADMP)", example = "1")
    private Long shopId;
}
