package com.myonline.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a refresh token issued to a user upon successful login.
 *
 * <p>Refresh tokens are long-lived credentials stored in the database that allow
 * users to obtain new access tokens without re-authenticating.
 *
 * <p>A refresh token is invalidated (revoked) on logout or when a new one is issued.
 * Expired or revoked tokens cannot be used to generate new access tokens.
 */
@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The refresh token string (UUID-based, cryptographically random).
     * Stored and compared in plain text (no sensitive info, just a random identifier).
     */
    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    /** The user this refresh token was issued to */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;

    /** Absolute expiry time of this refresh token */
    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    /**
     * Whether this token has been revoked (e.g., due to logout).
     * Revoked tokens must not be accepted even if not yet expired.
     */
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private Boolean revoked = false;
}
