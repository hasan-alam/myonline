package com.myonline.authservice.service;

import com.myonline.authservice.dto.request.LoginRequest;
import com.myonline.authservice.dto.request.RefreshTokenRequest;
import com.myonline.authservice.dto.request.UpdatePasswordRequest;
import com.myonline.authservice.dto.response.LoginResponse;
import com.myonline.authservice.entity.Permission;
import com.myonline.authservice.entity.RefreshToken;
import com.myonline.authservice.entity.Role;
import com.myonline.authservice.entity.User;
import com.myonline.authservice.exception.AuthException;
import com.myonline.authservice.exception.ResourceNotFoundException;
import com.myonline.authservice.repository.RefreshTokenRepository;
import com.myonline.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service handling user authentication operations.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>User login — authenticate credentials, issue JWT + refresh token</li>
 *   <li>User logout — revoke refresh token</li>
 *   <li>Token refresh — validate refresh token and issue a new access token</li>
 *   <li>Password update — verify current password and set new hashed password</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /** Refresh token expiry in milliseconds (from application.properties) */
    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    // =============================================
    // Login
    // =============================================

    /**
     * Authenticate a user with email and password, then issue JWT and refresh tokens.
     *
     * @param request login credentials
     * @return LoginResponse containing access token, refresh token, and user info
     * @throws AuthException if credentials are invalid or account is inactive
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Delegate to Spring Security authentication manager
        // This will throw BadCredentialsException or DisabledException if invalid
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Load the user entity for building the response
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Build UserDetails and embed roles/permissions as JWT claims
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .build();

        // Generate JWT access token with embedded roles and permissions
        String accessToken = jwtService.generateToken(buildJwtClaims(user), userDetails);

        // Revoke any existing refresh tokens for this user (one active session policy)
        refreshTokenRepository.revokeAllByUser(user);

        // Create and persist a new refresh token
        RefreshToken refreshToken = createRefreshToken(user);

        log.info("Login successful for user: {} (ID: {})", user.getEmail(), user.getUserId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpirationMs())
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    // =============================================
    // Logout
    // =============================================

    /**
     * Logout a user by revoking all their active refresh tokens.
     *
     * @param email the email of the authenticated user (extracted from JWT)
     */
    @Transactional
    public void logout(String email) {
        log.info("Logout request for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Revoke all refresh tokens for this user
        refreshTokenRepository.revokeAllByUser(user);

        log.info("User '{}' logged out successfully", email);
    }

    // =============================================
    // Token Refresh
    // =============================================

    /**
     * Generate a new JWT access token using a valid, non-expired, non-revoked refresh token.
     *
     * @param request containing the refresh token string
     * @return new LoginResponse with a fresh access token (same refresh token reused)
     * @throws AuthException if the refresh token is invalid, expired, or revoked
     */
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Token refresh request received");

        // Find the refresh token in the database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        // Check if the token has been revoked
        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            log.warn("Attempt to use a revoked refresh token");
            throw new AuthException("Refresh token has been revoked. Please login again.");
        }

        // Check if the token has expired
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Attempt to use an expired refresh token");
            // Revoke the expired token
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new AuthException("Refresh token has expired. Please login again.");
        }

        // Generate a new access token for the user with embedded roles and permissions
        User user = refreshToken.getUser();
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .build();

        String newAccessToken = jwtService.generateToken(buildJwtClaims(user), userDetails);

        log.info("New access token issued for user: {}", user.getEmail());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken()) // reuse the same refresh token
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpirationMs())
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    // =============================================
    // Password Update
    // =============================================

    /**
     * Update a user's password after verifying the current password.
     *
     * @param email   the email of the authenticated user (from JWT)
     * @param request contains current password, new password, and confirmation
     * @throws AuthException        if the current password is incorrect
     * @throws IllegalArgumentException if new password and confirm password don't match
     */
    @Transactional
    public void updatePassword(String email, UpdatePasswordRequest request) {
        log.info("Password update request for user: {}", email);

        // Validate that new password and confirmation match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Verify the current password against stored BCrypt hash
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password update failed for user '{}': incorrect current password", email);
            throw new AuthException("Current password is incorrect");
        }

        // Hash and save the new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens to force re-login with new password
        refreshTokenRepository.revokeAllByUser(user);

        log.info("Password updated successfully for user: {}", email);
    }

    // =============================================
    // Private Helpers
    // =============================================

    /**
     * Build JWT custom claims containing the user's roles and permissions.
     *
     * <p>Only active roles (roleStatus == 1) and their active permissions
     * (permissionStatus == 1) are included. This allows downstream microservices
     * to make authorization decisions directly from the token without a DB call.
     *
     * @param user the authenticated user entity (with eagerly loaded roles/permissions)
     * @return map of custom JWT claims
     */
    private Map<String, Object> buildJwtClaims(User user) {
        Set<String> roleSet = new LinkedHashSet<>();
        Set<String> permissionSet = new LinkedHashSet<>();

        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                if (role.getRoleStatus() != 1) continue;
                roleSet.add(role.getRoleName());
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        if (permission.getPermissionStatus() == 1) {
                            permissionSet.add(permission.getPermissionTitle());
                        }
                    }
                }
            }
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId",      user.getUserId());
        claims.put("shopId",      user.getShopId());
        claims.put("portalType",  user.getUserFor() != null ? user.getUserFor().name() : null);
        claims.put("roles",       new ArrayList<>(roleSet));
        claims.put("permissions", new ArrayList<>(permissionSet));
        return claims;
    }

    /**
     * Create and persist a new refresh token for a user.
     *
     * @param user the user to issue the refresh token for
     * @return the saved RefreshToken entity
     */
    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}
