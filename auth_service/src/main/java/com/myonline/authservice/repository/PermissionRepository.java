package com.myonline.authservice.repository;

import com.myonline.authservice.entity.Permission;
import com.myonline.authservice.enums.PortalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Permission entity.
 * Provides CRUD operations and custom queries for permission management.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /** Find a permission by its unique title */
    Optional<Permission> findByPermissionTitle(String permissionTitle);

    /** Check if a permission with the given title already exists */
    boolean existsByPermissionTitle(String permissionTitle);

    /** Find all permissions for a specific portal type */
    List<Permission> findByPermissionFor(PortalType permissionFor);

    /** Find all active permissions (status = 1) */
    List<Permission> findByPermissionStatus(Integer permissionStatus);

    /** Find active permissions for a specific portal */
    List<Permission> findByPermissionStatusAndPermissionFor(Integer permissionStatus, PortalType permissionFor);
}
