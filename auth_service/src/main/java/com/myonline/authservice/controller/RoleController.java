package com.myonline.authservice.controller;

import com.myonline.authservice.dto.request.AssignPermissionsRequest;
import com.myonline.authservice.dto.request.CreateRoleRequest;
import com.myonline.authservice.dto.request.UpdateRoleRequest;
import com.myonline.authservice.dto.response.ApiResponse;
import com.myonline.authservice.dto.response.RoleResponse;
import com.myonline.authservice.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Role management.
 *
 * <p>All endpoints require JWT authentication (Bearer token).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/roles                          — List all roles</li>
 *   <li>GET    /api/roles/{id}                     — Get role by ID</li>
 *   <li>GET    /api/roles/shop/{shopId}             — Get roles by shop</li>
 *   <li>POST   /api/roles                          — Create a role</li>
 *   <li>PUT    /api/roles/{id}                     — Update a role</li>
 *   <li>PUT    /api/roles/{id}/activate            — Activate a role</li>
 *   <li>PUT    /api/roles/{id}/deactivate          — Deactivate a role</li>
 *   <li>POST   /api/roles/{id}/permissions         — Assign permissions to role</li>
 *   <li>DELETE /api/roles/{id}/permissions         — Remove permissions from role</li>
 *   <li>DELETE /api/roles/{id}                     — Delete a role</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "CRUD operations for roles and permission assignment")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('SYS_ROLE_VIEW') or hasAuthority('SHOP_ROLE_VIEW') or hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @Operation(summary = "Get all roles", description = "Returns all roles in the system. " +
            "Also callable by TENANT_PAYMENT_APPROVAL to look up the SHOP_ADMIN role ID during tenant approval.")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SYS_ROLE_VIEW') or hasAuthority('SHOP_ROLE_VIEW')")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
            @Parameter(description = "Role ID") @PathVariable Long id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", role));
    }

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasAuthority('SYS_ROLE_VIEW') or hasAuthority('SHOP_ROLE_VIEW')")
    @Operation(summary = "Get roles by shop/tenant",
            description = "Returns all roles belonging to a specific shop/tenant")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRolesByShop(
            @Parameter(description = "Shop/Tenant ID") @PathVariable Long shopId) {
        List<RoleResponse> roles = roleService.getRolesByShop(shopId);
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SYS_ROLE_CREATE') or hasAuthority('SHOP_ROLE_ADD')")
    @Operation(summary = "Create a new role")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {
        RoleResponse created = roleService.createRole(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SYS_ROLE_EDIT') or hasAuthority('SHOP_ROLE_EDIT')")
    @Operation(summary = "Update a role")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @Parameter(description = "Role ID") @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        RoleResponse updated = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", updated));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('SYS_ROLE_ACTIVATE') or hasAuthority('SHOP_ROLE_EDIT')")
    @Operation(summary = "Activate a role", description = "Set role status to Active (1)")
    public ResponseEntity<ApiResponse<RoleResponse>> activateRole(
            @Parameter(description = "Role ID") @PathVariable Long id) {
        RoleResponse updated = roleService.activateRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role activated successfully", updated));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('SYS_ROLE_ACTIVATE') or hasAuthority('SHOP_ROLE_EDIT')")
    @Operation(summary = "Deactivate a role", description = "Set role status to Inactive (0)")
    public ResponseEntity<ApiResponse<RoleResponse>> deactivateRole(
            @Parameter(description = "Role ID") @PathVariable Long id) {
        RoleResponse updated = roleService.deactivateRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deactivated successfully", updated));
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('SYS_ROLE_EDIT') or hasAuthority('SHOP_ROLE_EDIT')")
    @Operation(summary = "Assign permissions to a role",
            description = "Add permissions to a role. Already assigned permissions are ignored.")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissions(
            @Parameter(description = "Role ID") @PathVariable Long id,
            @Valid @RequestBody AssignPermissionsRequest request) {
        RoleResponse updated = roleService.assignPermissions(id, request);
        return ResponseEntity.ok(ApiResponse.success("Permissions assigned successfully", updated));
    }

    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('SYS_ROLE_EDIT') or hasAuthority('SHOP_ROLE_EDIT')")
    @Operation(summary = "Remove permissions from a role",
            description = "Remove specific permissions from a role")
    public ResponseEntity<ApiResponse<RoleResponse>> removePermissions(
            @Parameter(description = "Role ID") @PathVariable Long id,
            @Valid @RequestBody AssignPermissionsRequest request) {
        RoleResponse updated = roleService.removePermissions(id, request);
        return ResponseEntity.ok(ApiResponse.success("Permissions removed successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SYS_ROLE_DELETE') or hasAuthority('SHOP_ROLE_DELETE')")
    @Operation(summary = "Delete a role")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @Parameter(description = "Role ID") @PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully"));
    }
}
