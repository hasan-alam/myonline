package com.myonline.tenantservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the Tenant Microservice.
 *
 * <p>Configures the API documentation metadata and security scheme.
 * JWT Bearer token authentication is required for all protected endpoints.
 *
 * <p>Swagger UI: <a href="http://localhost:8082/swagger-ui.html">http://localhost:8082/swagger-ui.html</a>
 * <p>API Docs:   <a href="http://localhost:8082/api-docs">http://localhost:8082/api-docs</a>
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title       = "myonline - Tenant Microservice API",
        version     = "v1.0.0",
        description = "REST API for managing tenants in the myonline multi-tenant e-commerce platform. " +
                      "Covers tenant subscription packages, registration requests, and approval workflow. " +
                      "Authentication is via JWT Bearer token issued by the Authorization Microservice.",
        contact = @Contact(
            name  = "myonline Platform Team",
            email = "admin@myonline.com"
        ),
        license = @License(name = "Private")
    ),
    servers = {
        @Server(url = "http://localhost:8082", description = "Local Development Server")
    }
)
@SecurityScheme(
    name         = "bearerAuth",
    type         = SecuritySchemeType.HTTP,
    scheme       = "bearer",
    bearerFormat = "JWT",
    description  = "Enter the JWT token obtained from POST /api/auth/login on the Authorization Microservice (port 8081). " +
                   "Format: Bearer <token>"
)
public class OpenApiConfig {
    // No bean definitions needed — annotations are processed by springdoc-openapi
}
