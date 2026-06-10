package com.myonline.authservice.controller;

import com.myonline.authservice.dto.request.CreatePermissionRequest;
import com.myonline.authservice.dto.request.UpdatePermissionRequest;
import com.myonline.authservice.dto.response.ApiResponse;
import com.myonline.authservice.dto.response.PermissionResponse;
import com.myonline.authservice.enums.PortalType;
import com.myonline.authservice.service.PermissionService;
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
 * REST controller for Permission management.
 *
 * <p>All endpoints require JWT authentication (Bearer token).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/permissions              — List all permissions</li>
 *   <li>GET    /api/permissions/{id}         — Get permission by ID</li>
 *   <li>GET    /api/permissions/portal/{type} — Get permissions by portal type</li>
 *   <li>POST   /api/permissions              — Create a permission</li>
 *   <li>PUT    /api/permissions/{id}         — Update a permission</li>
 *   <li>PUT    /api/permissions/{id}/activate   — Activate a permission</li>
 *   <li>PUT    /api/permissions/{id}/deactivate — Deactivate a permission</li>
 *   <li>DELETE /api/permissions/{id}         — Delete a permission</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permission Management", description = "CRUD operations for system permissions")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_VIEW') or hasAuthority('SYS_PERMISSION_MANAGE')")
    @Operation(summary = "Get all permissions", description = "Returns all permissions in the system")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success("Permissions retrieved successfully", permissions));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW') or hasAuthority('SYS_PERMISSION_MANAGE')")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(
            @Parameter(description = "Permission ID") @PathVariable Long id) {
        PermissionResponse permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success("Permission retrieved successfully", permission));
    }

    @GetMapping("/portal/{portalType}")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW') or hasAuthority('SYS_PERMISSION_MANAGE')")
    @Operation(summary = "Get permissions by portal type",
            description = "Filter permissions by portal: SHPADMP, SYSADMP, or BOTH")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissionsByPortal(
            @Parameter(description = "Portal type (SHPADMP / SYSADMP / BOTH)")
            @PathVariable PortalType portalType) {
        List<PermissionResponse> permissions = permissionService.getPermissionsByPortal(portalType);
        return ResponseEntity.ok(ApiResponse.success("Permissions retrieved successfully", permissions));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SYS_PERMISSION_MANAGE')")
    @Operation(summary = "Create a new permission")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {
        PermissionResponse created = permissionService.createPermission(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permission created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SYS_PERMISSION_MANAGE')")
    @Operation(summary = "Update a permission")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @Parameter(description = "Permission ID") @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        PermissionResponse updated = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", updated));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('SYS_PERMISSION_MANAGE')")
    @Operation(summary = "Activate a permission", description = "Set permission status to Active (1)")
    public ResponseEntity<ApiResponse<PermissionResponse>> activatePermission(
            @Parameter(description = "Permission ID") @PathVariable Long id) {
        PermissionResponse updated = permissionService.activatePermission(id);
        return ResponseEntity.ok(ApiResponse.success("Permission activated successfully", updated));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('SYS_PERMISSION_MANAGE')")
    @Operation(summary = "Deactivate a permission", description = "Set permission status to Inactive (0)")
    public ResponseEntity<ApiResponse<PermissionResponse>> deactivatePermission(
            @Parameter(description = "Permission ID") @PathVariable Long id) {
        PermissionResponse updated = permissionService.deactivatePermission(id);
        return ResponseEntity.ok(ApiResponse.success("Permission deactivated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SYS_PERMISSION_MANAGE')")
    @Operation(summary = "Delete a permission")
    public ResponseEntity<ApiResponse<Void>> deletePermission(
            @Parameter(description = "Permission ID") @PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success("Permission deleted successfully"));
    }
}
