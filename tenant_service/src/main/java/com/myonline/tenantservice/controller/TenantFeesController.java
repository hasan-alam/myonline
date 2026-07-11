package com.myonline.tenantservice.controller;

import com.myonline.tenantservice.dto.request.CreateTenantFeesRequest;
import com.myonline.tenantservice.dto.request.UpdateTenantFeesRequest;
import com.myonline.tenantservice.dto.response.ApiResponse;
import com.myonline.tenantservice.dto.response.TenantFeesResponse;
import com.myonline.tenantservice.service.TenantFeesService;
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
 * REST controller for managing tenant subscription packages (tenant_fees).
 *
 * <p>Base path: {@code /api/tenant-fees}
 *
 * <p>Permission requirements:
 * <ul>
 *   <li>View/Search/List: {@code TENANT_PAYMENT_VIEW} or {@code TENANT_PAYMENT_APPROVAL}</li>
 *   <li>Create/Update/Delete: {@code TENANT_PAYMENT_APPROVAL}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/tenant-fees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Fees", description = "Manage tenant subscription packages and pricing tiers")
@SecurityRequirement(name = "bearerAuth")
public class TenantFeesController {

    private final TenantFeesService tenantFeesService;

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    /**
     * Creates a new tenant subscription package.
     * Requires TENANT_PAYMENT_APPROVAL permission.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @Operation(
        summary = "Create a new subscription package",
        description = "Creates a new tenant subscription package. Package code must be unique. " +
                      "Product count range must not overlap with existing packages. " +
                      "Requires TENANT_PAYMENT_APPROVAL permission."
    )
    public ResponseEntity<ApiResponse<TenantFeesResponse>> createTenantFees(
            @Valid @RequestBody CreateTenantFeesRequest request) {

        log.info("POST /api/tenant-fees - Creating package: {}", request.getPackageCode());
        TenantFeesResponse response = tenantFeesService.createTenantFees(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tenant fees package created successfully.", response));
    }

    // ------------------------------------------------------------------
    // READ - List All
    // ------------------------------------------------------------------

    /**
     * Returns all tenant subscription packages.
     * Requires TENANT_PAYMENT_VIEW or TENANT_PAYMENT_APPROVAL permission.
     */
    @GetMapping
    @Operation(
        summary = "List all subscription packages (Public)",
        description = "Returns all tenant subscription packages ordered by product count range. " +
                      "No authentication required — used by the public registration page."
    )
    public ResponseEntity<ApiResponse<List<TenantFeesResponse>>> getAllTenantFees() {
        log.info("GET /api/tenant-fees - Fetching all packages");
        List<TenantFeesResponse> response = tenantFeesService.getAllTenantFees();
        return ResponseEntity.ok(ApiResponse.success("Tenant fees packages fetched successfully.", response));
    }

    // ------------------------------------------------------------------
    // READ - Get by Package Code
    // ------------------------------------------------------------------

    /**
     * Returns a single subscription package by its package code.
     * Requires TENANT_PAYMENT_VIEW or TENANT_PAYMENT_APPROVAL permission.
     */
    @GetMapping("/{packageCode}")
    @PreAuthorize("hasAuthority('TENANT_PAYMENT_VIEW') or hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @Operation(
        summary = "Get a subscription package by code",
        description = "Returns a single tenant subscription package by its unique package code. " +
                      "Requires TENANT_PAYMENT_VIEW or TENANT_PAYMENT_APPROVAL permission."
    )
    public ResponseEntity<ApiResponse<TenantFeesResponse>> getTenantFeesByPackageCode(
            @Parameter(description = "The package code to look up", example = "STARTER")
            @PathVariable String packageCode) {

        log.info("GET /api/tenant-fees/{} - Fetching package", packageCode);
        TenantFeesResponse response = tenantFeesService.getTenantFeesByPackageCode(packageCode);
        return ResponseEntity.ok(ApiResponse.success("Tenant fees package fetched successfully.", response));
    }

    // ------------------------------------------------------------------
    // READ - Search by Product Count Range
    // ------------------------------------------------------------------

    /**
     * Searches subscription packages by product count range.
     * Both parameters are optional. Requires TENANT_PAYMENT_VIEW or TENANT_PAYMENT_APPROVAL.
     *
     * <p>Filter behaviour:
     * <ul>
     *   <li>{@code from} — returns packages where productCountFrom >= from</li>
     *   <li>{@code to} — returns packages where productCountTo <= to</li>
     * </ul>
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('TENANT_PAYMENT_VIEW') or hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @Operation(
        summary = "Search packages by product count range",
        description = "Filters subscription packages by product count range. " +
                      "Both 'from' and 'to' are optional. " +
                      "Results are ordered by productCountFrom ascending. " +
                      "Requires TENANT_PAYMENT_VIEW or TENANT_PAYMENT_APPROVAL permission."
    )
    public ResponseEntity<ApiResponse<List<TenantFeesResponse>>> searchByProductCountRange(
            @Parameter(description = "Filter: productCountFrom >= this value", example = "1")
            @RequestParam(required = false) Integer from,
            @Parameter(description = "Filter: productCountTo <= this value", example = "100")
            @RequestParam(required = false) Integer to) {

        log.info("GET /api/tenant-fees/search - from={}, to={}", from, to);
        List<TenantFeesResponse> response = tenantFeesService.searchByProductCountRange(from, to);
        return ResponseEntity.ok(ApiResponse.success("Tenant fees packages fetched successfully.", response));
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    /**
     * Updates an existing subscription package.
     * Only provided fields are updated (partial update).
     * Requires TENANT_PAYMENT_APPROVAL permission.
     */
    @PutMapping("/{packageCode}")
    @PreAuthorize("hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @Operation(
        summary = "Update a subscription package",
        description = "Partially updates an existing tenant subscription package. " +
                      "Only provided fields are updated. Package code cannot be changed. " +
                      "Range overlap validation is applied if range fields are updated. " +
                      "Requires TENANT_PAYMENT_APPROVAL permission."
    )
    public ResponseEntity<ApiResponse<TenantFeesResponse>> updateTenantFees(
            @Parameter(description = "The package code to update", example = "STARTER")
            @PathVariable String packageCode,
            @Valid @RequestBody UpdateTenantFeesRequest request) {

        log.info("PUT /api/tenant-fees/{} - Updating package", packageCode);
        TenantFeesResponse response = tenantFeesService.updateTenantFees(packageCode, request);
        return ResponseEntity.ok(ApiResponse.success("Tenant fees package updated successfully.", response));
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    /**
     * Deletes a subscription package by its package code.
     * Requires TENANT_PAYMENT_APPROVAL permission.
     */
    @DeleteMapping("/{packageCode}")
    @PreAuthorize("hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @Operation(
        summary = "Delete a subscription package",
        description = "Permanently deletes a tenant subscription package. " +
                      "Requires TENANT_PAYMENT_APPROVAL permission."
    )
    public ResponseEntity<ApiResponse<Void>> deleteTenantFees(
            @Parameter(description = "The package code to delete", example = "STARTER")
            @PathVariable String packageCode) {

        log.info("DELETE /api/tenant-fees/{} - Deleting package", packageCode);
        tenantFeesService.deleteTenantFees(packageCode);
        return ResponseEntity.ok(ApiResponse.success("Tenant fees package deleted successfully."));
    }
}
