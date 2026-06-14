package com.myonline.tenantservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the myonline Tenant Microservice.
 *
 * <p>This service manages:
 * <ul>
 *   <li>Tenant Fees / Subscription Packages (CRUD, search)</li>
 *   <li>Tenant Registration Requests (submit, list, filter, view, approve/reject)</li>
 *   <li>Tenant Info (auto-created on registration approval)</li>
 * </ul>
 *
 * <p>Security: JWT tokens issued by auth_service are validated via auth-client-lib.
 * All protected endpoints require a valid Bearer token in the Authorization header.
 *
 * <p>API Documentation: http://localhost:8082/swagger-ui.html
 */
@SpringBootApplication
public class TenantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenantServiceApplication.class, args);
    }
}
