package com.myonline.tenantservice.enums;

/**
 * Represents the approval status of a tenant registration request.
 *
 * <ul>
 *   <li>{@code P} - Pending: initial state when submitted</li>
 *   <li>{@code A} - Approved: request approved; tenant account created in tenant_info</li>
 *   <li>{@code R} - Rejected: request rejected</li>
 * </ul>
 */
public enum ApprovalStatus {
    /** Pending — initial state on submission */
    P,
    /** Approved — tenant account created */
    A,
    /** Rejected */
    R
}
