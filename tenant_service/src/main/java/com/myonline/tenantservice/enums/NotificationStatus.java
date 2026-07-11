package com.myonline.tenantservice.enums;

/**
 * Status of a tenant registration notification.
 * Notifications start as PENDING and are updated by the Notification Service (future).
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED
}
