package com.myonline.authservice.controller;

import com.myonline.authservice.dto.request.LoginRequest;
import com.myonline.authservice.dto.request.RefreshTokenRequest;
import com.myonline.authservice.dto.request.UpdatePasswordRequest;
import com.myonline.authservice.dto.response.ApiResponse;
import com.myonline.authservice.dto.response.LoginResponse;
import com.myonline.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/auth/login           — Authenticate user, return JWT + refresh token</li>
 *   <li>POST /api/auth/logout          — Revoke refresh token (requires JWT)</li>
 *   <li>POST /api/auth/refresh-token   — Get new access token using refresh token</li>
 *   <li>PUT  /api/auth/change-password — Update current user's password (requires JWT)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Login, logout, token refresh, and password management")
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticate a user with email and password.
     * Returns a JWT access token (15 min) and a refresh token (7 days).
     *
     * @param request login credentials
     * @return 200 OK with login response containing tokens
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate with email and password to receive JWT and refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }

    /**
     * Log out the currently authenticated user.
     * Revokes the user's refresh token, invalidating the session.
     *
     * @param userDetails the currently authenticated user (from JWT)
     * @return 200 OK
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Revoke the current user's refresh token",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal String email) {
        log.info("Logout request for user: {}", email);
        authService.logout(email);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * Generate a new JWT access token using a valid refresh token.
     * The refresh token remains the same; only a new access token is issued.
     *
     * @param request contains the refresh token string
     * @return 200 OK with new access token
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Use a valid refresh token to get a new JWT access token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    /**
     * Update the password of the currently authenticated user.
     * Requires the current password for verification.
     * All active refresh tokens are revoked after the password change.
     *
     * @param userDetails the currently authenticated user (from JWT)
     * @param request     contains current password, new password, and confirmation
     * @return 200 OK
     */
    @PutMapping("/change-password")
    @Operation(summary = "Change password", description = "Update current user's password (current password required)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody UpdatePasswordRequest request) {
        log.info("Password change request for user: {}", email);
        authService.updatePassword(email, request);
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully. Please login again."));
    }
}
