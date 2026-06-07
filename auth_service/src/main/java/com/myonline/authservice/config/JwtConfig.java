package com.myonline.authservice.config;

import com.myonline.authclient.JwtAuthenticationFilter;
import com.myonline.authclient.JwtTokenParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT configuration for the Authorization Microservice.
 *
 * <p>Exposes {@link JwtTokenParser} and {@link JwtAuthenticationFilter} beans
 * sourced from {@code auth-client-lib}. The same beans are used by all other
 * myonline microservices, ensuring JWT validation logic lives in exactly one place.
 */
@Configuration
public class JwtConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * Shared JWT token parser — validates token signatures and extracts claims
     * (username, roles, permissions, shopId, portalType).
     *
     * @return a {@link JwtTokenParser} initialised with the application's signing secret
     */
    @Bean
    public JwtTokenParser jwtTokenParser() {
        return new JwtTokenParser(jwtSecret);
    }

    /**
     * Reusable JWT authentication filter from {@code auth-client-lib}.
     * Validates the {@code Authorization: Bearer <token>} header on every request
     * and populates the {@link org.springframework.security.core.context.SecurityContextHolder}.
     *
     * @param jwtTokenParser the shared token parser bean
     * @return a configured {@link JwtAuthenticationFilter}
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenParser jwtTokenParser) {
        return new JwtAuthenticationFilter(jwtTokenParser);
    }
}
