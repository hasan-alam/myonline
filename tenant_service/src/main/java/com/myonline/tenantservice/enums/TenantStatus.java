package com.myonline.tenantservice.enums;

/**
 * Represents the operational status of an active tenant account.
 *
 * <ul>
 *   <li>{@code A} - Active: tenant account is active and operational</li>
 *   <li>{@code I} - Inactive: tenant account has been deactivated</li>
 * </ul>
 */
public enum TenantStatus {
    /** Active — tenant account is operational */
    A,
    /** Inactive — tenant account is deactivated */
    I
}
