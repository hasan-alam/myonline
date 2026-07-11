package com.myonline.authservice.repository;

import com.myonline.authservice.entity.User;
import com.myonline.authservice.enums.PortalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find a user by email address (used for authentication) */
    Optional<User> findByEmail(String email);

    /** Check if a user with the given email already exists */
    boolean existsByEmail(String email);

    /** Check if a user with the given mobile number already exists */
    boolean existsByMobile(String mobile);

    /** Count users with the given email address (0 or 1 since email is unique) */
    int countByEmail(String email);

    /** Count users with the given mobile number (0 or 1 since mobile is unique) */
    int countByMobile(String mobile);

    /** Find all users belonging to a specific shop/tenant */
    List<User> findByShopId(Long shopId);

    /** Find all users for a specific portal type */
    List<User> findByUserFor(PortalType userFor);

    /** Find all active users (status = 1) */
    List<User> findByUserStatus(Integer userStatus);

    /** Find users by shop and status */
    List<User> findByShopIdAndUserStatus(Long shopId, Integer userStatus);
}
