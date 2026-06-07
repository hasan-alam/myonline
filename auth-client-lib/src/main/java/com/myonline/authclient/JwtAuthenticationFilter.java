package com.myonline.authclient;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable JWT authentication filter for myonline microservices.
 *
 * <p>Drop this filter into any Spring Boot microservice's security chain to
 * validate JWT tokens issued by the Authorization Microservice and populate
 * the {@link SecurityContextHolder} with the authenticated user's roles and
 * permissions — all without any database call.
 *
 * <h3>How to use in a consuming microservice:</h3>
 * <pre>{@code
 * // 1. Add auth-client-lib as a Maven dependency in pom.xml
 * // 2. Expose the required beans in your SecurityConfig:
 *
 * @Bean
 * public JwtTokenParser jwtTokenParser(@Value("${app.jwt.secret}") String secret) {
 *     return new JwtTokenParser(secret);
 * }
 *
 * @Bean
 * public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenParser parser) {
 *     return new JwtAuthenticationFilter(parser);
 * }
 *
 * // 3. Register the filter in the security filter chain:
 * @Bean
 * public SecurityFilterChain securityFilterChain(HttpSecurity http,
 *         JwtAuthenticationFilter jwtFilter) throws Exception {
 *     return http
 *         .csrf(csrf -> csrf.disable())
 *         .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
 *         .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
 *         .authorizeHttpRequests(auth -> auth
 *             .requestMatchers("/public/**").permitAll()
 *             .anyRequest().authenticated())
 *         .build();
 * }
 * }</pre>
 *
 * <h3>Authority format set in SecurityContext:</h3>
 * <ul>
 *   <li>Roles: {@code ROLE_SUPER_ADMIN}, {@code ROLE_SHOP_ADMIN}</li>
 *   <li>Permissions: {@code PRODUCT_CREATE}, {@code ORDER_VIEW}</li>
 * </ul>
 *
 * <p>Use {@code @PreAuthorize("hasAuthority('PRODUCT_CREATE')")} in your controllers
 * after enabling {@code @EnableMethodSecurity} on your configuration class.
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenParser jwtTokenParser;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Skip processing if no Authorization header or not a Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token (strip "Bearer " prefix)
        final String jwt = authHeader.substring(7);
        String userEmail = null;

        try {
            userEmail = jwtTokenParser.extractUsername(jwt);
        } catch (Exception e) {
            log.warn("Failed to extract username from JWT: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Authenticate only if username was extracted and no auth is already set
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Validate token signature and expiry — no DB call needed
            if (jwtTokenParser.isTokenValid(jwt)) {

                // Build GrantedAuthority list from roles and permissions in token claims
                List<GrantedAuthority> authorities = buildAuthorities(jwt);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userEmail, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("JWT authenticated: user='{}', request='{} {}', authorities={}",
                        userEmail, request.getMethod(), request.getRequestURI(), authorities);

            } else {
                log.warn("Invalid or expired JWT for user: {}", userEmail);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Build Spring Security authorities from the JWT claims.
     *
     * <ul>
     *   <li>Roles from the {@code roles} claim are prefixed with {@code ROLE_}</li>
     *   <li>Permissions from the {@code permissions} claim are added as-is</li>
     * </ul>
     */
    private List<GrantedAuthority> buildAuthorities(String token) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        List<String> roles = jwtTokenParser.extractRoles(token);
        if (roles != null) {
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }

        List<String> permissions = jwtTokenParser.extractPermissions(token);
        if (permissions != null) {
            for (String permission : permissions) {
                authorities.add(new SimpleGrantedAuthority(permission));
            }
        }

        return authorities;
    }
}
