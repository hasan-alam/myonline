package com.myonline.authservice.service;

import com.myonline.authservice.dto.request.CreatePermissionRequest;
import com.myonline.authservice.dto.request.UpdatePermissionRequest;
import com.myonline.authservice.dto.response.PermissionResponse;
import com.myonline.authservice.entity.Permission;
import com.myonline.authservice.enums.PortalType;
import com.myonline.authservice.exception.DuplicateResourceException;
import com.myonline.authservice.exception.ResourceNotFoundException;
import com.myonline.authservice.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing permissions (CRUD operations + status management).
 *
 * <p>Permissions are the atomic units of access control, grouped into roles.
 * They can be seeded via data.sql and also created/modified via the API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    // =============================================
    // Create
    // =============================================

    /**
     * Create a new permission.
     *
     * @param request the permission creation request
     * @return the created permission as a response DTO
     * @throws DuplicateResourceException if a permission with the same title already exists
     */
    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        log.info("Creating new permission: {}", request.getPermissionTitle());

        // Ensure no duplicate permission titles
        if (permissionRepository.existsByPermissionTitle(request.getPermissionTitle())) {
            throw new DuplicateResourceException("Permission", "title", request.getPermissionTitle());
        }

        Permission permission = Permission.builder()
                .permissionTitle(request.getPermissionTitle())
                .permissionDescription(request.getPermissionDescription())
                .permissionFor(request.getPermissionFor())
                .permissionStatus(1) // Active by default
                .build();

        Permission saved = permissionRepository.save(permission);
        log.info("Permission created successfully with ID: {}", saved.getId());
        return toResponse(saved);
    }

    // =============================================
    // Read
    // =============================================

    /**
     * Retrieve all permissions.
     *
     * @return list of all permissions
     */
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        log.debug("Fetching all permissions");
        return permissionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a permission by its ID.
     *
     * @param id the permission ID
     * @return the permission response DTO
     * @throws ResourceNotFoundException if the permission is not found
     */
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long id) {
        log.debug("Fetching permission with ID: {}", id);
        Permission permission = findPermissionById(id);
        return toResponse(permission);
    }

    /**
     * Retrieve permissions filtered by portal type.
     *
     * @param portalType the portal type to filter by
     * @return filtered list of permissions
     */
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsByPortal(PortalType portalType) {
        log.debug("Fetching permissions for portal: {}", portalType);
        return permissionRepository.findByPermissionFor(portalType)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =============================================
    // Update
    // =============================================

    /**
     * Update an existing permission's details.
     * Only non-null fields in the request are applied.
     *
     * @param id      the ID of the permission to update
     * @param request the update request
     * @return the updated permission response DTO
     * @throws ResourceNotFoundException  if the permission is not found
     * @throws DuplicateResourceException if the new title conflicts with another permission
     */
    @Transactional
    public PermissionResponse updatePermission(Long id, UpdatePermissionRequest request) {
        log.info("Updating permission with ID: {}", id);

        Permission permission = findPermissionById(id);

        // Update title only if provided and not already taken
        if (request.getPermissionTitle() != null && !request.getPermissionTitle().isBlank()) {
            if (!request.getPermissionTitle().equals(permission.getPermissionTitle())
                    && permissionRepository.existsByPermissionTitle(request.getPermissionTitle())) {
                throw new DuplicateResourceException("Permission", "title", request.getPermissionTitle());
            }
            permission.setPermissionTitle(request.getPermissionTitle());
        }

        if (request.getPermissionDescription() != null) {
            permission.setPermissionDescription(request.getPermissionDescription());
        }

        if (request.getPermissionFor() != null) {
            permission.setPermissionFor(request.getPermissionFor());
        }

        Permission updated = permissionRepository.save(permission);
        log.info("Permission ID {} updated successfully", id);
        return toResponse(updated);
    }

    /**
     * Activate a permission (set status to 1).
     *
     * @param id the permission ID
     * @return updated permission response DTO
     */
    @Transactional
    public PermissionResponse activatePermission(Long id) {
        log.info("Activating permission with ID: {}", id);
        Permission permission = findPermissionById(id);
        permission.setPermissionStatus(1);
        return toResponse(permissionRepository.save(permission));
    }

    /**
     * Deactivate a permission (set status to 0).
     *
     * @param id the permission ID
     * @return updated permission response DTO
     */
    @Transactional
    public PermissionResponse deactivatePermission(Long id) {
        log.info("Deactivating permission with ID: {}", id);
        Permission permission = findPermissionById(id);
        permission.setPermissionStatus(0);
        return toResponse(permissionRepository.save(permission));
    }

    // =============================================
    // Delete
    // =============================================

    /**
     * Delete a permission by ID.
     *
     * @param id the permission ID
     * @throws ResourceNotFoundException if the permission is not found
     */
    @Transactional
    public void deletePermission(Long id) {
        log.info("Deleting permission with ID: {}", id);
        Permission permission = findPermissionById(id);
        permissionRepository.delete(permission);
        log.info("Permission ID {} deleted", id);
    }

    // =============================================
    // Package-level helper (used by RoleService)
    // =============================================

    /**
     * Find a permission by ID or throw ResourceNotFoundException.
     *
     * @param id the permission ID
     * @return the Permission entity
     */
    public Permission findPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
    }

    // =============================================
    // Mapper
    // =============================================

    /**
     * Map a Permission entity to its response DTO.
     */
    public PermissionResponse toResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .permissionTitle(permission.getPermissionTitle())
                .permissionDescription(permission.getPermissionDescription())
                .permissionStatus(permission.getPermissionStatus())
                .permissionFor(permission.getPermissionFor())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}
