package com.myonline.tenantservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a tenant subscription package / fee tier.
 *
 * <p>Each package defines a product count range and the associated fees.
 * Ranges must not overlap (e.g., 1–50 and 51–100 are valid; 1–50 and 40–100 are not).
 *
 * <p>Mapped to the {@code tenant_fees} table.
 */
@Entity
@Table(name = "tenant_fees",
    uniqueConstraints = {
        // Enforce that (productCountFrom, productCountTo) pairs are unique
        @UniqueConstraint(name = "uk_tenant_fees_range",
            columnNames = {"product_count_from", "product_count_to"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantFees {

    /**
     * Unique package code — acts as the primary key (e.g., "STARTER", "BASIC").
     */
    @Id
    @Column(name = "package_code", length = 25, nullable = false)
    private String packageCode;

    /**
     * Human-readable package name (e.g., "Starter Package").
     */
    @Column(name = "package_name", length = 100, nullable = false)
    private String packageName;

    /**
     * Minimum product count covered by this package (inclusive).
     */
    @Column(name = "product_count_from", nullable = false)
    private Integer productCountFrom;

    /**
     * Maximum product count covered by this package (inclusive).
     */
    @Column(name = "product_count_to", nullable = false)
    private Integer productCountTo;

    /**
     * One-time registration fee (in local currency unit).
     */
    @Column(name = "registration_fee", nullable = false)
    private Integer registrationFee;

    /**
     * Recurring monthly subscription fee (in local currency unit).
     */
    @Column(name = "monthly_fee", nullable = false)
    private Integer monthlyFee;

    /** Timestamp when this record was created. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when this record was last updated. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
