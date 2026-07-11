package com.myonline.authservice.controller;

import com.myonline.authservice.dto.request.AssignRolesRequest;
import com.myonline.authservice.dto.request.CreateUserRequest;
import com.myonline.authservice.dto.response.ApiResponse;
import com.myonline.authservice.dto.response.UserResponse;
import com.myonline.authservice.service.UserService;
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
import java.util.Map;

/**
 * REST controller for User management.
 *
 * <p>All endpoints require JWT authentication (Bearer token).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /api/users                    — List all users</li>
 *   <li>GET    /api/users/{id}               — Get user by ID</li>
 *   <li>GET    /api/users/shop/{shopId}       — Get users by shop/tenant</li>
 *   <li>GET    /api/users/count              — Count users by email or mobile (PUBLIC)</li>
 *   <li>POST   /api/users                    — Create a user</li>
 *   <li>PUT    /api/users/{id}/activate      — Activate a user</li>
 *   <li>PUT    /api/users/{id}/deactivate    — Deactivate a user</li>
 *   <li>POST   /api/users/{id}/roles         — Assign roles to user</li>
 *   <li>DELETE /api/users/{id}/roles         — Remove roles from user</li>
 *   <li>DELETE /api/users/{id}               — Delete a user</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Create, manage users and assign roles")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Public endpoint — no authentication required.
     * Returns the count of users matching the given email or mobile number.
     * Since both fields are unique, the count is always 0 (not found) or 1 (exists).
     * Used by the tenant registration form to validate uniqueness before submission.
     *
     * <p>Provide exactly one of {@code email} or {@code mobile} as a query parameter.
     */
    @GetMapping("/count")
    @Operation(
            summary = "Count users by email or mobile (public)",
            description = "Returns {\"count\": 0} or {\"count\": 1}. No authentication required. " +
                    "Used during tenant registration to check email/mobile uniqueness.")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> countUsers(
            @Parameter(description = "Email address to check") @RequestParam(required = false) String email,
            @Parameter(description = "Mobile number to check") @RequestParam(required = false) String mobile) {

        int count = 0;
        if (email != null && !email.isBlank()) {
            count = userService.countByEmail(email.trim());
        } else if (mobile != null && !mobile.isBlank()) {
            count = userService.countByMobile(mobile.trim());
        }
        return ResponseEntity.ok(ApiResponse.success("Count retrieved successfully", Map.of("count", count)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SYS_USER_VIEW') or hasAuthority('SHOP_USER_VIEW')")
    @Operation(summary = "Get all users", description = "Returns all users in the system")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SYS_USER_VIEW') or hasAuthority('SHOP_USER_VIEW')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasAuthority('SYS_USER_VIEW') or hasAuthority('SHOP_USER_VIEW')")
    @Operation(summary = "Get users by shop/tenant",
            description = "Returns all users belonging to a specific shop/tenant")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByShop(
            @Parameter(description = "Shop/Tenant ID") @PathVariable Long shopId) {
        List<UserResponse> users = userService.getUsersByShop(shopId);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SYS_USER_CREATE') or hasAuthority('SHOP_USER_CREATE') or hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @Operation(summary = "Create a new user",
            description = "Create a user account. Password is BCrypt hashed before storage. " +
                    "Also callable by TENANT_PAYMENT_APPROVAL to create the SHOP_ADMIN user on tenant approval.")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", created));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('SYS_USER_MANAGE') or hasAuthority('SHOP_USER_MANAGE')")
    @Operation(summary = "Activate a user", description = "Set user status to Active (1)")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserResponse updated = userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", updated));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('SYS_USER_MANAGE') or hasAuthority('SHOP_USER_MANAGE')")
    @Operation(summary = "Deactivate a user",
            description = "Set user status to Inactive (0) and revoke active sessions")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserResponse updated = userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", updated));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('SYS_USER_MANAGE') or hasAuthority('SHOP_USER_MANAGE') or hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @Operation(summary = "Assign roles to a user",
            description = "Assign one or more roles to a user. Already assigned roles are ignored. " +
                    "Also callable by TENANT_PAYMENT_APPROVAL to assign SHOP_ADMIN role during tenant approval.")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody AssignRolesRequest request) {
        UserResponse updated = userService.assignRoles(id, request);
        return ResponseEntity.ok(ApiResponse.success("Roles assigned successfully", updated));
    }

    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('SYS_USER_MANAGE') or hasAuthority('SHOP_USER_MANAGE')")
    @Operation(summary = "Remove roles from a user",
            description = "Remove specific roles from a user")
    public ResponseEntity<ApiResponse<UserResponse>> removeRoles(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody AssignRolesRequest request) {
        UserResponse updated = userService.removeRoles(id, request);
        return ResponseEntity.ok(ApiResponse.success("Roles removed successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SYS_USER_DELETE') or hasAuthority('SHOP_USER_DELETE')")
    @Operation(summary = "Delete a user",
            description = "Permanently delete a user account and all associated refresh tokens")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}
