package com.myonline.tenantservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * HTTP client for communicating with the Auth Microservice.
 *
 * <p>The base URL is configurable via {@code app.auth-service.url} in application.properties.
 * All calls forward the caller's Bearer JWT token to satisfy auth_service security checks.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Create a new SHOP_ADMIN user on tenant approval (POST /api/users)</li>
 *   <li>Lookup the SHOP_ADMIN role ID (GET /api/roles)</li>
 *   <li>Assign the SHOP_ADMIN role to the newly created user (POST /api/users/{id}/roles)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.auth-service.url}")
    private String authServiceUrl;

    // ──────────────────────────────────────────────────────────────────────────
    // Uniqueness Checks (Public — no auth required)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Checks how many users in auth_service have the given email address.
     * Since email is unique, the result is 0 (free) or 1 (already taken).
     * Calls the public {@code GET /api/users/count?email=} endpoint.
     *
     * @param email the email address to check
     * @return 1 if a user with this email exists, 0 otherwise; 0 on any communication error
     */
    public int checkEmailCount(String email) {
        String url = authServiceUrl + "/api/users/count?email=" + email;
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
                    new ParameterizedTypeReference<>() {});
            return extractCount(response.getBody());
        } catch (Exception ex) {
            log.warn("Could not check email count in auth_service for '{}': {}", email, ex.getMessage());
            return 0; // Fail open — DB unique constraint will catch real duplicates
        }
    }

    /**
     * Checks how many users in auth_service have the given mobile number.
     * Since mobile is unique, the result is 0 (free) or 1 (already taken).
     * Calls the public {@code GET /api/users/count?mobile=} endpoint.
     *
     * @param mobile the mobile number to check
     * @return 1 if a user with this mobile exists, 0 otherwise; 0 on any communication error
     */
    public int checkMobileCount(String mobile) {
        String url = authServiceUrl + "/api/users/count?mobile=" + mobile;
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
                    new ParameterizedTypeReference<>() {});
            return extractCount(response.getBody());
        } catch (Exception ex) {
            log.warn("Could not check mobile count in auth_service for '{}': {}", mobile, ex.getMessage());
            return 0;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Create User
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new SHOP_ADMIN user in auth_service.
     *
     * @param name       full name of the user
     * @param mobile     contact phone number
     * @param email      email address (used as login)
     * @param password   plaintext password (auth_service BCrypt-hashes it)
     * @param shopId     tenant ID to link the user to (set as shopId)
     * @param authHeader the Authorization header value ("Bearer &lt;token&gt;") from the caller
     * @return the created user's ID, or {@code null} if the email is already registered (HTTP 409)
     * @throws RuntimeException if an unexpected error occurs
     */
    public Long createUser(String name, String mobile, String email,
                           String password, Long shopId, String authHeader) {
        String url = authServiceUrl + "/api/users";
        log.info("URL for creating user in auth_service: {}", url);
        Map<String, Object> requestBody = Map.of(
                "name", name,
                "mobile", mobile,
                "email", email,
                "password", password,
                "userFor", "SHPADMP",
                "shopId", shopId
        );
        log.info("Request body for creating user in auth_service: {}", requestBody);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, buildHeaders(authHeader));

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<>() {});

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                Map<String, Object> data = extractData(response.getBody());
                if (data != null && data.get("userId") != null) {
                    return ((Number) data.get("userId")).longValue();
                }
            }
            log.warn("Unexpected response from auth_service createUser: {}", response.getStatusCode());
            return null;

        } catch (HttpClientErrorException.Conflict ex) {
            // HTTP 409 — user with this email already exists
            log.info("User with email '{}' already exists in auth_service.", email);
            return null;
        } catch (Exception ex) {
            log.error("Failed to create user in auth_service for email '{}': {}", email, ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("User creation in auth_service failed: " + ex.getMessage(), ex);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Find SHOP_ADMIN Role ID
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Fetches all roles from auth_service and returns the ID of the role named "SHOP_ADMIN".
     *
     * @param authHeader the Authorization header value
     * @return the SHOP_ADMIN role ID, or {@code null} if not found or call fails
     */
    public Long findShopAdminRoleId(String authHeader) {
        String url = authServiceUrl + "/api/roles";
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(authHeader));

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<>() {});

            if (response.getBody() == null) return null;

            Object dataObj = response.getBody().get("data");
            if (!(dataObj instanceof List<?> roles)) return null;

            for (Object roleObj : roles) {
                if (!(roleObj instanceof Map<?, ?> role)) continue;
                Object roleName = role.get("roleName");
                Object roleId  = role.get("roleId");
                if ("SHOP_ADMIN".equals(roleName) && roleId != null) {
                    return ((Number) roleId).longValue();
                }
            }
            log.warn("SHOP_ADMIN role not found in auth_service roles list.");
            return null;

        } catch (Exception ex) {
            log.error("Failed to fetch roles from auth_service: {}", ex.getMessage());
            return null;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Assign Role
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Assigns a role to a user in auth_service.
     *
     * @param userId     the user ID
     * @param roleId     the role ID to assign
     * @param authHeader the Authorization header value
     */
    public void assignRoleToUser(Long userId, Long roleId, String authHeader) {
        String url = authServiceUrl + "/api/users/" + userId + "/roles";
        Map<String, Object> requestBody = Map.of("roleIds", List.of(roleId));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, buildHeaders(authHeader));

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            log.info("Assigned role {} to user {} in auth_service.", roleId, userId);
        } catch (Exception ex) {
            log.error("Failed to assign role {} to user {} in auth_service: {}", roleId, userId, ex.getMessage());
            // Non-fatal — user is created; role assignment failure is logged but doesn't roll back approval
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private HttpHeaders buildHeaders(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        }
        return headers;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractData(Map<String, Object> responseBody) {
        Object data = responseBody.get("data");
        if (data instanceof Map<?, ?>) {
            return (Map<String, Object>) data;
        }
        return null;
    }

    private int extractCount(Map<String, Object> responseBody) {
        if (responseBody == null) return 0;
        Object data = responseBody.get("data");
        if (data instanceof Map<?, ?> d) {
            Object count = d.get("count");
            if (count instanceof Number n) return n.intValue();
        }
        return 0;
    }
}
