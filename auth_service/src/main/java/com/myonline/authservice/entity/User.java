package com.myonline.authservice.entity;

import com.myonline.authservice.enums.PortalType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a user of the myonline platform.
 *
 * <p>Users can belong to the Shop Admin Portal (tenant users), the System Admin Portal
 * (platform administrators), or both.
 *
 * <p>Multi-tenancy: system admin users have {@code shopId = null};
 * shop admin users carry their tenant's {@code shopId}.
 *
 * <p>Status: 0 = Inactive, 1 = Active
 */
@Entity
@Table(name = "user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_mobile", columnNames = "mobile")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /** Full name of the user */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Mobile phone number (unique across the platform) */
    @Column(name = "mobile", nullable = false, length = 20)
    private String mobile;

    /** Email address used for login (unique across the platform) */
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    /** BCrypt-hashed password — never stored in plain text */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Indicates which portal this user belongs to.
     * SHPADMP = Shop Admin Portal, SYSADMP = System Admin Portal, BOTH = Both portals
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_for", nullable = false, length = 10)
    private PortalType userFor;

    /**
     * Status of the user account.
     * 0 = Inactive (cannot log in)
     * 1 = Active
     */
    @Column(name = "user_status", nullable = false)
    @Builder.Default
    private Integer userStatus = 1;

    /**
     * Tenant identifier. Null for system admin users (SYSADMP).
     * For shop admin users, holds the shop/tenant ID.
     */
    @Column(name = "shop_id")
    private Long shopId;

    /** Timestamp when this user account was created */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when this user account was last updated */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Roles assigned to this user.
     * Uses a join table (user_role) for the many-to-many relationship.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;
}
