package com.myonline.authservice.service;

import com.myonline.authservice.dto.request.AssignPermissionsRequest;
import com.myonline.authservice.dto.request.CreateRoleRequest;
import com.myonline.authservice.dto.request.UpdateRoleRequest;
import com.myonline.authservice.dto.response.PermissionResponse;
import com.myonline.authservice.dto.response.RoleResponse;
import com.myonline.authservice.entity.Permission;
import com.myonline.authservice.entity.Role;
import com.myonline.authservice.exception.DuplicateResourceException;
import com.myonline.authservice.exception.ResourceNotFoundException;
import com.myonline.authservice.repository.PermissionRepository;
import com.myonline.authservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing roles (CRUD operations + permission assignment).
 *
 * <p>Roles group permissions and are assigned to users.
 * Seed roles (Super Admin, Shop Admin) are pre-loaded via data.sql.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionService permissionService;

    // =============================================
    // Create
    // =============================================

    /**
     * Create a new role.
     *
     * @param request the role creation request
     * @return the created role as a response DTO
     * @throws DuplicateResourceException if a role with the same name already exists
     */
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        log.info("Creating new role: {}", request.getRoleName());

        if (roleRepository.existsByRoleName(request.getRoleName())) {
            throw new DuplicateResourceException("Role", "name", request.getRoleName());
        }

        Role role = Role.builder()
                .roleName(request.getRoleName())
                .roleDescription(request.getRoleDescription())
                .roleFor(request.getRoleFor())
                .shopId(request.getShopId())
                .roleStatus(1) // Active by default
                .permissions(new ArrayList<>())
                .build();

        Role saved = roleRepository.save(role);
        log.info("Role created successfully with ID: {}", saved.getRoleId());
        return toResponse(saved);
    }

    // =============================================
    // Read
    // =============================================

    /**
     * Retrieve all roles.
     *
     * @return list of all roles
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        log.debug("Fetching all roles");
        return roleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a role by its ID.
     *
     * @param id the role ID
     * @return the role response DTO
     * @throws ResourceNotFoundException if the role is not found
     */
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        log.debug("Fetching role with ID: {}", id);
        return toResponse(findRoleById(id));
    }

    /**
     * Retrieve roles belonging to a specific shop/tenant.
     *
     * @param shopId the shop/tenant ID
     * @return list of roles for the shop
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesByShop(Long shopId) {
        log.debug("Fetching roles for shopId: {}", shopId);
        return roleRepository.findByShopId(shopId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =============================================
    // Update
    // =============================================

    /**
     * Update an existing role's details.
     * Only non-null fields in the request are applied.
     *
     * @param id      the ID of the role to update
     * @param request the update request
     * @return the updated role response DTO
     */
    @Transactional
    public RoleResponse updateRole(Long id, UpdateRoleRequest request) {
        log.info("Updating role with ID: {}", id);

        Role role = findRoleById(id);

        if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
            if (!request.getRoleName().equals(role.getRoleName())
                    && roleRepository.existsByRoleName(request.getRoleName())) {
                throw new DuplicateResourceException("Role", "name", request.getRoleName());
            }
            role.setRoleName(request.getRoleName());
        }

        if (request.getRoleDescription() != null) {
            role.setRoleDescription(request.getRoleDescription());
        }

        if (request.getRoleFor() != null) {
            role.setRoleFor(request.getRoleFor());
        }

        Role updated = roleRepository.save(role);
        log.info("Role ID {} updated successfully", id);
        return toResponse(updated);
    }

    /**
     * Activate a role (set status to 1).
     *
     * @param id the role ID
     * @return updated role response DTO
     */
    @Transactional
    public RoleResponse activateRole(Long id) {
        log.info("Activating role with ID: {}", id);
        Role role = findRoleById(id);
        role.setRoleStatus(1);
        return toResponse(roleRepository.save(role));
    }

    /**
     * Deactivate a role (set status to 0).
     *
     * @param id the role ID
     * @return updated role response DTO
     */
    @Transactional
    public RoleResponse deactivateRole(Long id) {
        log.info("Deactivating role with ID: {}", id);
        Role role = findRoleById(id);
        role.setRoleStatus(0);
        return toResponse(roleRepository.save(role));
    }

    // =============================================
    // Permission Assignment
    // =============================================

    /**
     * Assign permissions to a role.
     * Permissions already assigned to the role are not duplicated.
     *
     * @param roleId  the role ID
     * @param request contains the list of permission IDs to assign
     * @return the updated role response DTO
     */
    @Transactional
    public RoleResponse assignPermissions(Long roleId, AssignPermissionsRequest request) {
        log.info("Assigning {} permission(s) to role ID: {}", request.getPermissionIds().size(), roleId);

        Role role = findRoleById(roleId);
        List<Permission> currentPermissions = role.getPermissions();
        if (currentPermissions == null) {
            currentPermissions = new ArrayList<>();
        }

        // Add only permissions not already assigned
        for (Long permissionId : request.getPermissionIds()) {
            Permission permission = permissionService.findPermissionById(permissionId);
            boolean alreadyAssigned = currentPermissions.stream()
                    .anyMatch(p -> p.getId().equals(permissionId));
            if (!alreadyAssigned) {
                currentPermissions.add(permission);
            }
        }

        role.setPermissions(currentPermissions);
        Role updated = roleRepository.save(role);
        log.info("Permissions assigned to role ID: {}", roleId);
        return toResponse(updated);
    }

    /**
     * Remove specific permissions from a role.
     *
     * @param roleId  the role ID
     * @param request contains the list of permission IDs to remove
     * @return the updated role response DTO
     */
    @Transactional
    public RoleResponse removePermissions(Long roleId, AssignPermissionsRequest request) {
        log.info("Removing {} permission(s) from role ID: {}", request.getPermissionIds().size(), roleId);

        Role role = findRoleById(roleId);
        List<Permission> currentPermissions = role.getPermissions();
        if (currentPermissions != null) {
            currentPermissions.removeIf(p -> request.getPermissionIds().contains(p.getId()));
            role.setPermissions(currentPermissions);
        }

        Role updated = roleRepository.save(role);
        log.info("Permissions removed from role ID: {}", roleId);
        return toResponse(updated);
    }

    // =============================================
    // Delete
    // =============================================

    /**
     * Delete a role by ID.
     *
     * @param id the role ID
     */
    @Transactional
    public void deleteRole(Long id) {
        log.info("Deleting role with ID: {}", id);
        Role role = findRoleById(id);
        roleRepository.delete(role);
        log.info("Role ID {} deleted", id);
    }

    // =============================================
    // Package-level helper (used by UserService)
    // =============================================

    /**
     * Find a role by ID or throw ResourceNotFoundException.
     */
    public Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    // =============================================
    // Mapper
    // =============================================

    /**
     * Map a Role entity to its response DTO.
     */
    public RoleResponse toResponse(Role role) {
        List<PermissionResponse> permissionResponses = null;
        if (role.getPermissions() != null) {
            permissionResponses = role.getPermissions().stream()
                    .map(permissionService::toResponse)
                    .collect(Collectors.toList());
        }

        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .roleDescription(role.getRoleDescription())
                .roleStatus(role.getRoleStatus())
                .roleFor(role.getRoleFor())
                .shopId(role.getShopId())
                .permissions(permissionResponses)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
