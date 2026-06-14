package com.myonline.tenantservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource is not found in the database.
 * Results in a {@code 404 Not Found} HTTP response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Convenience constructor that formats: "{resourceName} not found with {fieldName}: '{fieldValue}'".
     *
     * @param resourceName the entity type (e.g., "TenantFees")
     * @param fieldName    the field searched on (e.g., "packageCode")
     * @param fieldValue   the value that was not found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
