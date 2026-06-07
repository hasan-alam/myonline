package com.myonline.authservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the Authorization Microservice.
 *
 * <p>Defines:
 * <ul>
 *   <li>API metadata (title, version, description, contact)</li>
 *   <li>Bearer token security scheme for JWT authentication</li>
 *   <li>Server URL for the Swagger UI "Try it out" feature</li>
 * </ul>
 *
 * <p>Access Swagger UI at: http://localhost:8081/swagger-ui.html
 * <p>Access OpenAPI JSON at: http://localhost:8081/api-docs
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "myonline - Authorization Microservice API",
                version = "v1.0.0",
                description = """
                        REST API for the Authorization Microservice of the myonline multi-tenant
                        e-commerce platform.
                        
                        **Features:**
                        - User authentication with JWT access tokens and refresh tokens
                        - Role management (CRUD) with seed data (Super Admin, Shop Admin)
                        - Permission management (CRUD) with portal-level access control
                        - User management (create, activate, deactivate, delete)
                        - Multi-tenant support via shop_id
                        
                        **Authentication:**
                        Use the `/api/auth/login` endpoint to obtain a Bearer token,
                        then click **Authorize** and enter: `Bearer <your_access_token>`
                        """,
                contact = @Contact(
                        name = "myonline Platform Team",
                        email = "admin@myonline.com"
                ),
                license = @License(name = "Private")
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Local Development Server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter JWT token obtained from /api/auth/login. Format: Bearer <token>"
)
public class OpenApiConfig {
    // Configuration is provided via annotations above
}
