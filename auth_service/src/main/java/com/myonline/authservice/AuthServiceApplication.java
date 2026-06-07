package com.myonline.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Authorization Microservice.
 *
 * <p>This microservice is responsible for:
 * <ul>
 *   <li>User authentication (login, logout)</li>
 *   <li>JWT access token and refresh token management</li>
 *   <li>Role and Permission management (CRUD)</li>
 *   <li>User management (create, activate, deactivate, delete)</li>
 * </ul>
 *
 * <p>Part of the myonline multi-tenant e-commerce platform.
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
