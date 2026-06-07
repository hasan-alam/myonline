package com.myonline.authservice.repository;

import com.myonline.authservice.entity.Role;
import com.myonline.authservice.enums.PortalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Role entity.
 * Provides CRUD operations and custom queries for role management.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /** Find a role by its unique name */
    Optional<Role> findByRoleName(String roleName);

    /** Check if a role with the given name already exists */
    boolean existsByRoleName(String roleName);

    /** Find all roles for a specific portal type */
    List<Role> findByRoleFor(PortalType roleFor);

    /** Find all roles belonging to a specific shop/tenant */
    List<Role> findByShopId(Long shopId);

    /** Find all active roles (status = 1) */
    List<Role> findByRoleStatus(Integer roleStatus);

    /** Find roles for a specific portal and shop (includes null shopId for system roles) */
    List<Role> findByRoleForAndShopId(PortalType roleFor, Long shopId);
}
