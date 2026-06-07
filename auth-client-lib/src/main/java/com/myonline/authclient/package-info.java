/**
 * myonline Auth Client Library — shared JWT utilities for microservices.
 *
 * <h2>Components</h2>
 * <ul>
 *   <li>{@link com.myonline.authclient.JwtTokenParser} — validates JWT tokens and
 *       extracts claims (username, roles, permissions, shopId, portalType).</li>
 *   <li>{@link com.myonline.authclient.JwtAuthenticationFilter} — a ready-to-use
 *       {@code OncePerRequestFilter} that reads the {@code Authorization: Bearer} header,
 *       validates the token, and populates the Spring {@code SecurityContextHolder}.</li>
 * </ul>
 *
 * <h2>How to add to a microservice</h2>
 * <p>1. Install the library to your local Maven repository from the auth-client-lib directory:
 * <pre>mvn install</pre>
 *
 * <p>2. Add the dependency to the microservice's {@code pom.xml}:
 * <pre>{@code
 * <dependency>
 *     <groupId>com.myonline</groupId>
 *     <artifactId>auth-client-lib</artifactId>
 *     <version>1.0.0</version>
 * </dependency>
 * }</pre>
 *
 * <p>3. Configure beans in your {@code SecurityConfig}:
 * <pre>{@code
 * @Bean
 * public JwtTokenParser jwtTokenParser(@Value("${app.jwt.secret}") String secret) {
 *     return new JwtTokenParser(secret);
 * }
 *
 * @Bean
 * public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenParser parser) {
 *     return new JwtAuthenticationFilter(parser);
 * }
 * }</pre>
 *
 * <p>4. Register the filter in the security chain:
 * <pre>{@code
 * http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
 * }</pre>
 *
 * <p>5. Use {@code @PreAuthorize} on controller methods (requires {@code @EnableMethodSecurity}):
 * <pre>{@code
 * @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
 * public ResponseEntity<?> createProduct(...) { ... }
 * }</pre>
 *
 * <h2>JWT secret</h2>
 * <p>All microservices must share the same {@code app.jwt.secret} value as the
 * Authorization Microservice. Store it in Kubernetes Secrets or a vault — never
 * hardcode it in source code.
 */
package com.myonline.authclient;
