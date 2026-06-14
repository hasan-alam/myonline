package com.myonline.tenantservice.entity;

import com.myonline.tenantservice.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a tenant's registration request submitted via the public endpoint.
 *
 * <p>A new request starts with {@link ApprovalStatus#P} (Pending).
 * Once reviewed by a system admin:
 * <ul>
 *   <li>{@link ApprovalStatus#A} — Approved: a corresponding {@link TenantInfo} record is created.</li>
 *   <li>{@link ApprovalStatus#R} — Rejected: no tenant account is created.</li>
 * </ul>
 *
 * <p>Mapped to the {@code tenant_registration_request} table.
 */
@Entity
@Table(name = "tenant_registration_request",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_reg_request_domain_prefix",
            columnNames = "domain_prefix")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantRegistrationRequest {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Name of the business applying for a tenant account (required).
     */
    @Column(name = "tenant_business_name", nullable = false, length = 255)
    private String tenantBusinessName;

    /**
     * Desired subdomain prefix (e.g., "lubuta" → lubuta.myonline.com).
     * Must be unique across all registration requests and active tenants.
     */
    @Column(name = "domain_prefix", nullable = false, length = 100)
    private String domainPrefix;

    /** Primary mailing address line (required). */
    @Column(name = "mailing_address1", nullable = false, length = 255)
    private String mailingAddress1;

    /** Secondary mailing address line (optional). */
    @Column(name = "mailing_address2", length = 255)
    private String mailingAddress2;

    /** Name of the primary contact person (required). */
    @Column(name = "contact_person", nullable = false, length = 150)
    private String contactPerson;

    /** Primary contact phone number (required). */
    @Column(name = "contact_number1", nullable = false, length = 20)
    private String contactNumber1;

    /** Secondary contact phone number (optional). */
    @Column(name = "contact_number2", length = 20)
    private String contactNumber2;

    /** Business email address (required). */
    @Column(name = "email_address", nullable = false, length = 150)
    private String emailAddress;

    /**
     * Maximum number of inventory items allowed under the requested package (required).
     */
    @Column(name = "max_inventory_items", nullable = false)
    private Integer maxInventoryItems;

    /**
     * Selected subscription package code (references {@link TenantFees#packageCode}).
     */
    @Column(name = "package_code", length = 25)
    private String packageCode;

    /**
     * Registration fee amount — populated automatically from {@link TenantFees} on submission.
     */
    @Column(name = "registration_fee", nullable = false)
    private Integer registrationFee;

    /**
     * Monthly subscription fee — populated automatically from {@link TenantFees} on submission.
     */
    @Column(name = "monthly_payment", nullable = false)
    private Integer monthlyPayment;

    /**
     * Approval/rejection status of this request.
     * Defaults to {@link ApprovalStatus#P} (Pending) on submission.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 1)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.P;

    /** Payment channel used for registration fee (e.g., "bKash", "Bank Transfer"). */
    @Column(name = "registration_fee_pmt_channel", length = 100)
    private String registrationFeePmtChannel;

    /** Transaction reference number for the registration fee payment. */
    @Column(name = "registration_fee_pmt_ref", length = 100)
    private String registrationFeePmtRef;

    /**
     * Screenshot/receipt of the registration fee payment, stored as a BLOB.
     * Accepted as Base64-encoded string via API and decoded before storage.
     */
    @Lob
    @Column(name = "registration_fee_pmt_receipt", columnDefinition = "LONGBLOB")
    private byte[] registrationFeePmtReceipt;

    /** Optional remarks added by the admin during approval or rejection. */
    @Column(name = "admin_remarks", length = 500)
    private String adminRemarks;

    /** Timestamp when this registration request was submitted. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp of the last update (e.g., when status changed). */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
