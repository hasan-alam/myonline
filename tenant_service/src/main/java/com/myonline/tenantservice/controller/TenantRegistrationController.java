package com.myonline.tenantservice.controller;

import com.myonline.tenantservice.dto.request.SubmitRegistrationRequest;
import com.myonline.tenantservice.dto.request.TenantApprovalRequest;
import com.myonline.tenantservice.dto.response.ApiResponse;
import com.myonline.tenantservice.dto.response.TenantRegistrationResponse;
import com.myonline.tenantservice.enums.ApprovalStatus;
import com.myonline.tenantservice.service.TenantRegistrationService;
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
 * REST controller for managing tenant registration requests.
 *
 * <p>Base path: {@code /api/tenant-registrations}
 *
 * <p>Endpoint permission requirements:
 * <ul>
 *   <li>Submit registration: Public (no auth required)</li>
 *   <li>Check domain availability: Public (no auth required)</li>
 *   <li>List / Search / View: {@code TENANT_PAYMENT_VIEW} or {@code TENANT_PAYMENT_APPROVAL}</li>
 *   <li>Approve / Reject: {@code TENANT_PAYMENT_APPROVAL}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/tenant-registrations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Registration", description = "Manage tenant registration requests and approval workflow")
public class TenantRegistrationController {

    private final TenantRegistrationService registrationService;

    // ------------------------------------------------------------------
    // SUBMIT REGISTRATION (Public)
    // ------------------------------------------------------------------

    /**
     * Publicly accessible endpoint to submit a new tenant registration request.
     * No authentication required.
     * Fees are auto-populated from the selected package code.
     */
    @PostMapping
    @Operation(
        summary = "Submit a tenant registration request (Public)",
        description = "Allows anyone to submit a tenant registration request. " +
                      "No authentication required. " +
                      "registrationFee and monthlyPayment are automatically populated from the selected package. " +
                      "The domain prefix must be unique. The request starts in Pending (P) status."
    )
    public ResponseEntity<ApiResponse<TenantRegistrationResponse>> submitRegistration(
            @Valid @RequestBody SubmitRegistrationRequest request) {

        log.info("POST /api/tenant-registrations - New registration for domainPrefix: {}",
                request.getDomainPrefix());
        TenantRegistrationResponse response = registrationService.submitRegistration(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Tenant registration request submitted successfully. " +
                        "Your application is under review.", response));
    }

    // ------------------------------------------------------------------
    // CHECK DOMAIN AVAILABILITY (Public)
    // ------------------------------------------------------------------

    /**
     * Publicly accessible endpoint to check if a domain prefix is available.
     * No authentication required.
     */
    @GetMapping("/check-domain")
    @Operation(
        summary = "Check domain prefix availability (Public)",
        description = "Checks whether the given domain prefix is available for registration. " +
                      "No authentication required. " +
                      "A domain prefix is unavailable if it exists in any registration request or active tenant."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkDomainAvailability(
            @Parameter(description = "Domain prefix to check (e.g., 'myshop')", example = "myshop")
            @RequestParam String domainPrefix) {

        log.info("GET /api/tenant-registrations/check-domain?domainPrefix={}", domainPrefix);
        Map<String, Object> result = registrationService.checkDomainAvailability(domainPrefix);
        boolean available = (Boolean) result.get("available");
        String message = (String) result.get("message");
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    // ------------------------------------------------------------------
    // LIST ALL
    // ------------------------------------------------------------------

    /**
     * Returns all registration requests (newest first).
     * Requires TENANT_PAYMENT_VIEW or TENANT_PAYMENT_APPROVAL permission.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('TENANT_PAYMENT_VIEW') or hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "List all registration requests",
        description = "Returns all tenant registration requests ordered by submission date (newest first). " +
                      "Requires TENANT_PAYMENT_VIEW or TENANT_PAYMENT_APPROVAL permission."
    )
    public ResponseEntity<ApiResponse<List<TenantRegistrationResponse>>> getAllRegistrations() {
        log.info("GET /api/tenant-registrations - Fetching all registrations");
        List<TenantRegistrationResponse> response = registrationService.getAllRegistrations();
        return ResponseEntity.ok(ApiResponse.success(
                "Tenant registration requests fetched successfully.", response));
    }

    // ------------------------------------------------------------------
    // SEARCH WITH FILTERS
    // ------------------------------------------------------------------

    /**
     * Searches registration requests with multiple optional filter criteria.
     * All parameters are optional. Requires TENANT_PAYMENT_VIEW or TENANT_PAYMENT_APPROVAL permission.
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('TENANT_PAYMENT_VIEW') or hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Search registration requests with filters",
        description = "Searches registration requests using multiple optional filter criteria. " +
                      "tenantBusinessName supports partial/case-insensitive matching. " +
                      "contactNumber is searched in both contactNumber1 and contactNumber2. " +
                      "All parameters are optional. " +
                      "Requires TENANT_PAYMENT_VIEW or TENANT_PAYMENT_APPROVAL permission."
    )
    public ResponseEntity<ApiResponse<List<TenantRegistrationResponse>>> searchRegistrations(
            @Parameter(description = "Filter by registration ID", example = "1")
            @RequestParam(required = false) Long id,

            @Parameter(description = "Filter by exact package code", example = "STARTER")
            @RequestParam(required = false) String packageCode,

            @Parameter(description = "Partial name search (case-insensitive)", example = "My Shop")
            @RequestParam(required = false) String tenantBusinessName,

            @Parameter(description = "Filter by approval status: P, A, or R", example = "P")
            @RequestParam(required = false) ApprovalStatus approvalStatus,

            @Parameter(description = "Filter by exact domain prefix", example = "myshop")
            @RequestParam(required = false) String domainPrefix,

            @Parameter(description = "Filter by contact number (searches both contactNumber1 and contactNumber2)",
                example = "01712345678")
            @RequestParam(required = false) String contactNumber,

            @Parameter(description = "Filter by exact email address", example = "owner@myshop.com")
            @RequestParam(required = false) String emailAddress) {

        log.info("GET /api/tenant-registrations/search - Searching with filters");
        List<TenantRegistrationResponse> response = registrationService.searchRegistrations(
                id, packageCode, tenantBusinessName, approvalStatus,
                domainPrefix, contactNumber, emailAddress);
        return ResponseEntity.ok(ApiResponse.success(
                "Tenant registration requests fetched successfully.", response));
    }

    // ------------------------------------------------------------------
    // GET BY ID
    // ------------------------------------------------------------------

    /**
     * Returns a single registration request by its ID.
     * Requires TENANT_PAYMENT_VIEW or TENANT_PAYMENT_APPROVAL permission.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TENANT_PAYMENT_VIEW') or hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get a registration request by ID",
        description = "Returns a single tenant registration request by its ID. " +
                      "Requires TENANT_PAYMENT_VIEW or TENANT_PAYMENT_APPROVAL permission."
    )
    public ResponseEntity<ApiResponse<TenantRegistrationResponse>> getRegistrationById(
            @Parameter(description = "Registration request ID", example = "1")
            @PathVariable Long id) {

        log.info("GET /api/tenant-registrations/{} - Fetching registration", id);
        TenantRegistrationResponse response = registrationService.getRegistrationById(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Tenant registration request fetched successfully.", response));
    }

    // ------------------------------------------------------------------
    // APPROVE / REJECT
    // ------------------------------------------------------------------

    /**
     * Approves or rejects a pending tenant registration request.
     *
     * <p>On approval, a tenant account is automatically created in {@code tenant_info}
     * with Active status. The response includes both the updated registration request
     * and the newly created tenant info.
     *
     * <p>Requires TENANT_PAYMENT_APPROVAL permission.
     */
    @PutMapping("/{id}/decision")
    @PreAuthorize("hasAuthority('TENANT_PAYMENT_APPROVAL')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Approve or reject a registration request",
        description = "Approves or rejects a Pending (P) tenant registration request. " +
                      "On approval: creates tenant_info, creates a SHOP_ADMIN user in auth_service (if new email), " +
                      "assigns SHOP_ADMIN role, and saves a PENDING notification. " +
                      "On rejection: saves a PENDING notification with the rejection reason. " +
                      "Only Pending (P) requests can be processed. " +
                      "Requires TENANT_PAYMENT_APPROVAL permission."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> approveOrRejectRegistration(
            @Parameter(description = "Registration request ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody TenantApprovalRequest request,
            @org.springframework.web.bind.annotation.RequestHeader(
                value = "Authorization", required = false) String authHeader) {

        log.info("PUT /api/tenant-registrations/{}/decision - approved={}", id, request.getApproved());
        Map<String, Object> result = registrationService.approveOrRejectRegistration(id, request, authHeader);

        boolean approved = Boolean.TRUE.equals(request.getApproved());
        String message = approved
                ? "Tenant registration approved. Tenant account created successfully."
                : "Tenant registration rejected.";

        return ResponseEntity.ok(ApiResponse.success(message, result));
    }
}
