package com.myonline.authservice.config;

import com.myonline.authclient.JwtAuthenticationFilter;
import com.myonline.authservice.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the Authorization Microservice.
 *
 * <p>Security strategy:
 * <ul>
 *   <li>Stateless JWT-based authentication (no HTTP sessions)</li>
 *   <li>CSRF disabled (REST API — clients use JWT, not browser cookies)</li>
 *   <li>Public endpoints: login, refresh-token, Swagger UI, actuator health</li>
 *   <li>All other endpoints require a valid JWT token</li>
 *   <li>Password hashing with BCrypt</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize, @PostAuthorize annotations on controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Public endpoints that do not require authentication.
     * Includes login, refresh-token, and Swagger UI paths.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/refresh-token",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    /**
     * Configures the HTTP security filter chain.
     *
     * <ul>
     *   <li>Disables CSRF (not needed for stateless JWT APIs)</li>
     *   <li>Opens public endpoints (login, swagger, etc.)</li>
     *   <li>Requires authentication for all other requests</li>
     *   <li>Sets session management to STATELESS</li>
     *   <li>Registers the JWT filter before UsernamePasswordAuthenticationFilter</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — REST APIs use JWT tokens, not session cookies
                .csrf(AbstractHttpConfigurer::disable)

                // Configure request authorization
                .authorizeHttpRequests(auth -> auth
                        // Allow public endpoints without authentication
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // All other requests require a valid JWT token
                        .anyRequest().authenticated()
                )

                // Use stateless session management (no HTTP sessions)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Set the custom authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before the default username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures the DAO authentication provider.
     * Uses our UserDetailsService and BCrypt password encoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager bean for use in AuthService.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder for hashing and verifying passwords.
     * BCrypt strength factor 12 provides a good balance of security and performance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
