package com.myonline.tenantservice.config;

import com.myonline.authclient.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the Tenant Microservice.
 *
 * <p>This service is a JWT consumer (not an issuer). It:
 * <ul>
 *   <li>Validates JWT tokens issued by auth_service via {@link JwtAuthenticationFilter}.</li>
 *   <li>Does NOT manage users or issue tokens (no UserDetailsService or AuthenticationManager needed).</li>
 *   <li>Enforces method-level security via {@code @PreAuthorize} (enabled by {@code @EnableMethodSecurity}).</li>
 * </ul>
 *
 * <p>Public endpoints (no authentication required):
 * <ul>
 *   <li>{@code POST /api/tenant-registrations} — submit registration</li>
 *   <li>{@code GET /api/tenant-registrations/check-domain} — check domain availability</li>
 *   <li>Swagger UI and API docs</li>
 * </ul>
 *
 * <p>All other endpoints require a valid JWT Bearer token.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Enables @PreAuthorize on controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    /** Filter from auth-client-lib that validates JWT and sets the SecurityContext. */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Endpoints that are publicly accessible without authentication.
     * - Registration submission is public so anyone can apply.
     * - Domain check is public to allow verification before filling the form.
     * - Swagger endpoints are open for documentation browsing.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/tenant-registrations",           // POST (submit registration)
        "/api/tenant-registrations/check-domain", // GET (domain availability check)
        "/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**"
    };

    /**
     * Configures the Spring Security filter chain.
     *
     * <ul>
     *   <li>CSRF disabled (stateless REST API).</li>
     *   <li>Stateless session management (no HTTP sessions).</li>
     *   <li>Public endpoints are permitted without authentication.</li>
     *   <li>All other requests require authentication.</li>
     *   <li>JWT filter is applied before the UsernamePasswordAuthenticationFilter.</li>
     * </ul>
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT-based REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session — JWT is self-contained, no server-side sessions
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated())

            // Register JWT authentication filter before the default auth filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
