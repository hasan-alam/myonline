package com.myonline.authservice.entity;

import com.myonline.authservice.enums.PortalType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a system permission that can be assigned to roles.
 *
 * <p>Permissions are seeded into the database and can be managed via the Permission API.
 * Each permission is associated with a specific portal (SHPADMP, SYSADMP, or BOTH).
 *
 * <p>Status: 0 = Inactive, 1 = Active
 */
@Entity
@Table(name = "permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    /** Human-readable name/title of the permission (e.g., "CREATE_PRODUCT") */
    @Column(name = "permission_title", nullable = false, unique = true, length = 100)
    private String permissionTitle;

    /** Detailed description of what this permission allows */
    @Column(name = "permission_description", length = 255)
    private String permissionDescription;

    /**
     * Status of the permission.
     * 0 = Inactive (cannot be assigned or used)
     * 1 = Active (available for use)
     */
    @Column(name = "permission_status", nullable = false)
    @Builder.Default
    private Integer permissionStatus = 1;

    /**
     * Indicates which portal this permission applies to.
     * SHPADMP = Shop Admin Portal, SYSADMP = System Admin Portal, BOTH = Both portals
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_for", nullable = false, length = 10)
    private PortalType permissionFor;

    /** Timestamp when this permission was created */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when this permission was last updated */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Roles that have been assigned this permission */
    @ManyToMany(mappedBy = "permissions")
    private List<Role> roles;
}
