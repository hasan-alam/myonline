package com.myonline.tenantservice.entity;

import com.myonline.tenantservice.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing an approved and active tenant account.
 *
 * <p>A {@code TenantInfo} record is automatically created when a
 * {@link TenantRegistrationRequest} is approved by a system admin with
 * the {@code TENANT_PAYMENT_APPROVAL} permission.
 *
 * <p>Mapped to the {@code tenant_info} table.
 */
@Entity
@Table(name = "tenant_info",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tenant_info_domain_prefix",
            columnNames = "domain_prefix")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantInfo {

    /** Auto-generated primary key for the tenant account. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id")
    private Long tenantId;

    /**
     * Name of the tenant's business.
     */
    @Column(name = "tenant_business_name", nullable = false, length = 255)
    private String tenantBusinessName;

    /**
     * Unique subdomain prefix assigned to this tenant (e.g., "lubuta" → lubuta.myonline.com).
     */
    @Column(name = "domain_prefix", nullable = false, length = 100)
    private String domainPrefix;

    /** Primary mailing address. */
    @Column(name = "mailing_address1", nullable = false, length = 255)
    private String mailingAddress1;

    /** Secondary mailing address (optional). */
    @Column(name = "mailing_address2", length = 255)
    private String mailingAddress2;

    /** Name of the primary contact person. */
    @Column(name = "contact_person", nullable = false, length = 150)
    private String contactPerson;

    /** Primary contact phone number. */
    @Column(name = "contact_number1", nullable = false, length = 20)
    private String contactNumber1;

    /** Secondary contact phone number (optional). */
    @Column(name = "contact_number2", length = 20)
    private String contactNumber2;

    /** Business email address. */
    @Column(name = "email_address", nullable = false, length = 150)
    private String emailAddress;

    /**
     * Maximum number of inventory items allowed for this tenant.
     */
    @Column(name = "max_inventory_items", nullable = false)
    private Integer maxInventoryItems;

    /**
     * Subscription package code assigned to this tenant.
     */
    @Column(name = "package_code", length = 25)
    private String packageCode;

    /**
     * Operational status of the tenant account.
     * Set to {@link TenantStatus#A} (Active) on initial creation.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 1)
    @Builder.Default
    private TenantStatus status = TenantStatus.A;

    /**
     * Reference to the original registration request that led to this tenant account.
     * Useful for audit trails.
     */
    @Column(name = "registration_request_id")
    private Long registrationRequestId;

    /** Timestamp when this tenant account was created. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp of the last update to this tenant account. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
