package com.myonline.authservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returned on successful login.
 *
 * <p>Contains the JWT access token (short-lived) and refresh token (long-lived)
 * along with basic user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login response containing JWT tokens and user info")
public class LoginResponse {

    @Schema(description = "JWT access token (valid for 15 minutes)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Refresh token used to regenerate access token (valid for 7 days)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    @Schema(description = "Token type, always 'Bearer'", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiry duration in milliseconds", example = "900000")
    private Long expiresIn;

    @Schema(description = "Logged-in user's ID", example = "1")
    private Long userId;

    @Schema(description = "Logged-in user's name", example = "John Doe")
    private String name;

    @Schema(description = "Logged-in user's email", example = "john@myonline.com")
    private String email;
}
