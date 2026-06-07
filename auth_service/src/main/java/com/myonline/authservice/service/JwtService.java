package com.myonline.authservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for JWT access token <strong>generation only</strong>.
 *
 * <p>Token parsing and validation is handled by {@code JwtTokenParser}
 * from {@code auth-client-lib}, which is shared across all myonline microservices.
 * This keeps JWT verification logic in a single place.
 *
 * <p>Uses HMAC-SHA256 (HS256) signing with a configurable secret key.
 * Access tokens are short-lived (default 15 minutes).
 */
@Service
@Slf4j
public class JwtService {

    /** Secret key for signing JWT tokens (configured in application.properties) */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /** Access token expiry in milliseconds (default 15 minutes) */
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // =============================================
    // Token Generation
    // =============================================

    /**
     * Generate a JWT access token for a given user.
     *
     * @param userDetails the authenticated user details
     * @return signed JWT access token string
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generate a JWT access token with additional custom claims.
     *
     * <p>The {@code extraClaims} map should contain at minimum:
     * <ul>
     *   <li>{@code roles} — list of active role names</li>
     *   <li>{@code permissions} — list of active permission titles</li>
     *   <li>{@code userId} — the user's primary key</li>
     *   <li>{@code shopId} — tenant ID (null for system admin users)</li>
     *   <li>{@code portalType} — "SYSADMP", "SHPADMP", or "BOTH"</li>
     * </ul>
     *
     * @param extraClaims additional claims to embed in the token
     * @param userDetails the authenticated user details (provides the subject / email)
     * @return signed JWT access token string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        log.debug("Generating JWT token for user: {}", userDetails.getUsername());
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Get the configured access token expiry duration in milliseconds.
     */
    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    // =============================================
    // Private Helpers
    // =============================================

    /**
     * Build the HMAC-SHA signing key from the configured secret string.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
