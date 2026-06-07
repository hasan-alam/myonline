package com.myonline.authclient;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Stateless utility for parsing and validating JWT tokens issued by the
 * myonline Authorization Microservice.
 *
 * <p>Instantiate once per microservice, typically as a Spring {@code @Bean}:
 *
 * <pre>{@code
 * @Bean
 * public JwtTokenParser jwtTokenParser(@Value("${app.jwt.secret}") String secret) {
 *     return new JwtTokenParser(secret);
 * }
 * }</pre>
 *
 * <p>The {@code secret} must match the one configured in the Authorization
 * Microservice ({@code app.jwt.secret} property).
 *
 * <p>This class is thread-safe — the signing key is built once at construction.
 */
@Slf4j
public class JwtTokenParser {

    private final SecretKey signingKey;

    /**
     * Construct a parser using the HMAC-SHA256 secret shared with the auth service.
     *
     * @param secret the raw secret string (must be at least 32 characters for HS256)
     */
    public JwtTokenParser(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // =============================================
    // Token Validation
    // =============================================

    /**
     * Validate a JWT token by verifying its signature and checking that it has not expired.
     *
     * @param token the JWT token string
     * @return {@code true} if the token is signed correctly and is not expired
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check whether a JWT token has passed its expiry time.
     *
     * @param token the JWT token string
     * @return {@code true} if the token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // =============================================
    // Claims Extraction
    // =============================================

    /**
     * Extract the username (email) stored in the {@code sub} claim.
     *
     * @param token the JWT token string
     * @return the subject (email address) of the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the user ID embedded in the {@code userId} claim.
     *
     * @param token the JWT token string
     * @return the user ID, or {@code null} if not present
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object val = claims.get("userId");
            if (val instanceof Integer) return ((Integer) val).longValue();
            if (val instanceof Long)    return (Long) val;
            return null;
        });
    }

    /**
     * Extract the list of role names embedded in the {@code roles} claim.
     *
     * <p>Example value: {@code ["SUPER_ADMIN", "SHOP_ADMIN"]}
     *
     * @param token the JWT token string
     * @return list of role names, or {@code null} if the claim is absent
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> (List<String>) claims.get("roles"));
    }

    /**
     * Extract the list of permission titles embedded in the {@code permissions} claim.
     *
     * <p>Example value: {@code ["PRODUCT_CREATE", "ORDER_VIEW", "REPORT_VIEW"]}
     *
     * @param token the JWT token string
     * @return list of permission titles, or {@code null} if the claim is absent
     */
    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return extractClaim(token, claims -> (List<String>) claims.get("permissions"));
    }

    /**
     * Extract the portal type embedded in the {@code portalType} claim.
     *
     * <p>Possible values: {@code "SYSADMP"}, {@code "SHPADMP"}, {@code "BOTH"}
     *
     * @param token the JWT token string
     * @return portal type string, or {@code null} if not present
     */
    public String extractPortalType(String token) {
        return extractClaim(token, claims -> (String) claims.get("portalType"));
    }

    /**
     * Extract the shop/tenant ID embedded in the {@code shopId} claim.
     *
     * <p>System admin users will have {@code null} here.
     *
     * @param token the JWT token string
     * @return the shop ID, or {@code null} for system admin users
     */
    public Long extractShopId(String token) {
        return extractClaim(token, claims -> {
            Object val = claims.get("shopId");
            if (val instanceof Integer) return ((Integer) val).longValue();
            if (val instanceof Long)    return (Long) val;
            return null;
        });
    }

    /**
     * Extract the token expiration date from the {@code exp} claim.
     *
     * @param token the JWT token string
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claims extractor using a resolver function.
     *
     * @param token          the JWT token string
     * @param claimsResolver function applied to the parsed claims
     * @param <T>            return type of the resolver
     * @return the resolved claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // =============================================
    // Private Helpers
    // =============================================

    /**
     * Parse and return all claims from the token.
     * Throws {@link JwtException} if the signature is invalid or the token is malformed.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
