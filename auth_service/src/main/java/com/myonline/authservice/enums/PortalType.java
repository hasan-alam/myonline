package com.myonline.authservice.enums;

/**
 * Represents the portal a role, permission, or user belongs to.
 *
 * <ul>
 *   <li>{@code SHPADMP} - Shop Admin Portal (for individual tenant shop admins)</li>
 *   <li>{@code SYSADMP} - System Admin Portal (for platform/application administrators)</li>
 *   <li>{@code BOTH}    - Applicable to both portals</li>
 * </ul>
 */
public enum PortalType {

    /** Shop Admin Portal - used by individual tenant shop administrators */
    SHPADMP,

    /** System Admin Portal - used by platform-level administrators */
    SYSADMP,

    /** Applicable to both Shop Admin and System Admin portals */
    BOTH
}
