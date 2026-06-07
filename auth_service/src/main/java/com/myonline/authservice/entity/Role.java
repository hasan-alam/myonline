package com.myonline.authservice.entity;

import com.myonline.authservice.enums.PortalType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a role in the system that can be assigned to users.
 *
 * <p>Roles group permissions together and define what actions a user can perform.
 * Seed roles include "Super Admin" (SYSADMP) and "Shop Admin" (SHPADMP).
 *
 * <p>Multi-tenancy: system-level roles have {@code shopId = null};
 * tenant-specific roles carry the tenant's {@code shopId}.
 *
 * <p>Status: 0 = Inactive, 1 = Active
 */
@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    /** Short unique name for the role (e.g., "SUPER_ADMIN", "SHOP_ADMIN") */
    @Column(name = "role_name", nullable = false, unique = true, length = 100)
    private String roleName;

    /** Detailed description of the role and its purpose */
    @Column(name = "role_description", length = 255)
    private String roleDescription;

    /**
     * Status of the role.
     * 0 = Inactive (role cannot be assigned or used)
     * 1 = Active
     */
    @Column(name = "role_status", nullable = false)
    @Builder.Default
    private Integer roleStatus = 1;

    /**
     * Indicates which portal this role applies to.
     * SHPADMP = Shop Admin Portal, SYSADMP = System Admin Portal, BOTH = Both portals
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_for", nullable = false, length = 10)
    private PortalType roleFor;

    /**
     * Tenant identifier. Null for system-level roles (SYSADMP).
     * For tenant-specific roles, this holds the shop/tenant ID.
     */
    @Column(name = "shop_id")
    private Long shopId;

    /** Timestamp when this role was created */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when this role was last updated */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Permissions associated with this role.
     * Uses a join table (role_permission) for the many-to-many relationship.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private List<Permission> permissions;
}
