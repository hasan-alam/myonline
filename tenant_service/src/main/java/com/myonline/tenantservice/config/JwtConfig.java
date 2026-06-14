package com.myonline.tenantservice.config;

import com.myonline.authclient.JwtAuthenticationFilter;
import com.myonline.authclient.JwtTokenParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration for the Tenant Microservice.
 *
 * <p>Instantiates the {@link JwtTokenParser} and {@link JwtAuthenticationFilter} beans
 * provided by the shared {@code auth-client-lib} library.
 *
 * <p>The JWT secret must match the secret used in {@code auth_service} to ensure
 * tokens signed by auth_service can be validated here.
 *
 * <p>The {@link JwtAuthenticationFilter} is registered in the Spring Security filter chain
 * via {@link SecurityConfig}.
 */
@Configuration
public class JwtConfig {

    /**
     * The JWT signing secret — must match the value configured in auth_service.
     * Injected from {@code app.jwt.secret} in application.properties.
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * Creates a {@link JwtTokenParser} bean used to validate and parse JWT tokens.
     *
     * @return a configured JwtTokenParser instance
     */
    @Bean
    public JwtTokenParser jwtTokenParser() {
        return new JwtTokenParser(jwtSecret);
    }

    /**
     * Creates a {@link JwtAuthenticationFilter} bean that extracts and sets
     * the Spring Security authentication context from incoming JWT Bearer tokens.
     *
     * @param jwtTokenParser the parser used to validate and extract claims
     * @return a configured JwtAuthenticationFilter instance
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenParser jwtTokenParser) {
        return new JwtAuthenticationFilter(jwtTokenParser);
    }
}
