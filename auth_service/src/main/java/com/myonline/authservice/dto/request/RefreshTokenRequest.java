package com.myonline.authservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for generating a new access token using a refresh token.
 */
@Data
@Schema(description = "Request to regenerate access token using refresh token")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "The refresh token issued at login", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;
}
