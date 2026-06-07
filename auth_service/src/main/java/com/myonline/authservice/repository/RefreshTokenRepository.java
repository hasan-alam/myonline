package com.myonline.authservice.repository;

import com.myonline.authservice.entity.RefreshToken;
import com.myonline.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for RefreshToken entity.
 * Manages storage and retrieval of refresh tokens for session management.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** Find a refresh token by its token string */
    Optional<RefreshToken> findByToken(String token);

    /** Find the active (non-revoked) refresh token for a user */
    Optional<RefreshToken> findByUserAndRevoked(User user, Boolean revoked);

    /**
     * Revoke all active refresh tokens for a given user.
     * Called on logout to invalidate all existing sessions.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user AND rt.revoked = false")
    void revokeAllByUser(User user);

    /** Delete all refresh tokens belonging to a user (for account deletion) */
    void deleteByUser(User user);
}
