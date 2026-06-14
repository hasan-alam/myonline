package com.myonline.tenantservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when attempting to create a resource that already exists (e.g., duplicate packageCode or domainPrefix).
 * Results in a {@code 409 Conflict} HTTP response.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    /**
     * Convenience constructor that formats: "{resourceName} already exists with {fieldName}: '{fieldValue}'".
     *
     * @param resourceName the entity type (e.g., "TenantFees")
     * @param fieldName    the field that is duplicated (e.g., "packageCode")
     * @param fieldValue   the duplicate value
     */
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
