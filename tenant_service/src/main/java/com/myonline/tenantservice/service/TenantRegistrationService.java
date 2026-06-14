package com.myonline.tenantservice.service;

import com.myonline.tenantservice.dto.request.SubmitRegistrationRequest;
import com.myonline.tenantservice.dto.request.TenantApprovalRequest;
import com.myonline.tenantservice.dto.response.TenantInfoResponse;
import com.myonline.tenantservice.dto.response.TenantRegistrationResponse;
import com.myonline.tenantservice.entity.TenantFees;
import com.myonline.tenantservice.entity.TenantInfo;
import com.myonline.tenantservice.entity.TenantRegistrationRequest;
import com.myonline.tenantservice.enums.ApprovalStatus;
import com.myonline.tenantservice.enums.TenantStatus;
import com.myonline.tenantservice.exception.DuplicateResourceException;
import com.myonline.tenantservice.exception.ResourceNotFoundException;
import com.myonline.tenantservice.repository.TenantInfoRepository;
import com.myonline.tenantservice.repository.TenantRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Service class for managing tenant registration requests and tenant info.
 *
 * <p>Business rules:
 * <ul>
 *   <li>Domain prefix must be unique across registration requests AND tenant_info.</li>
 *   <li>Package code must exist in tenant_fees; fees are auto-populated.</li>
 *   <li>On approval: a TenantInfo record is created with Active status.</li>
 *   <li>A request can only be approved/rejected if it is in Pending (P) status.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantRegistrationService {

    private final TenantRegistrationRepository registrationRepository;
    private final TenantInfoRepository tenantInfoRepository;
    private final TenantFeesService tenantFeesService;

    // ------------------------------------------------------------------
    // SUBMIT REGISTRATION (Public)
    // ------------------------------------------------------------------

    /**
     * Submits a new tenant registration request (publicly accessible endpoint).
     *
     * <p>Validates:
     * <ul>
     *   <li>Domain prefix is not already used (in requests or tenant_info)</li>
     *   <li>Package code exists in tenant_fees</li>
     *   <li>maxInventoryItems is within the selected package's product count range</li>
     * </ul>
     * Fees are auto-populated from the selected package.
     *
     * @param request the registration submission details
     * @return the created registration request as a response DTO
     * @throws DuplicateResourceException if domain prefix is already taken
     * @throws ResourceNotFoundException  if the package code does not exist
     * @throws IllegalArgumentException   if maxInventoryItems exceeds the package's limit
     */
    @Transactional
    public TenantRegistrationResponse submitRegistration(SubmitRegistrationRequest request) {
        log.debug("Submitting tenant registration for domainPrefix: {}", request.getDomainPrefix());

        // Check domain prefix uniqueness in registration requests
        if (registrationRepository.existsByDomainPrefix(request.getDomainPrefix())) {
            throw new DuplicateResourceException(
                    "TenantRegistrationRequest", "domainPrefix", request.getDomainPrefix());
        }

        // Check domain prefix uniqueness in active tenant accounts
        if (tenantInfoRepository.existsByDomainPrefix(request.getDomainPrefix())) {
            throw new DuplicateResourceException(
                    "TenantInfo", "domainPrefix", request.getDomainPrefix());
        }

        // Fetch and validate the selected package
        TenantFees tenantFees = tenantFeesService.findByPackageCodeOrThrow(request.getPackageCode());

        // Validate that maxInventoryItems fits within the package's product count range
        if (request.getMaxInventoryItems() < tenantFees.getProductCountFrom()
                || request.getMaxInventoryItems() > tenantFees.getProductCountTo()) {
            throw new IllegalArgumentException(
                    String.format("maxInventoryItems (%d) must be within the selected package's range [%d, %d] for package '%s'",
                            request.getMaxInventoryItems(),
                            tenantFees.getProductCountFrom(),
                            tenantFees.getProductCountTo(),
                            tenantFees.getPackageCode()));
        }

        // Decode Base64 receipt image if provided
        byte[] receiptBytes = null;
        if (request.getRegistrationFeePmtReceiptBase64() != null
                && !request.getRegistrationFeePmtReceiptBase64().isBlank()) {
            try {
                receiptBytes = Base64.getDecoder().decode(request.getRegistrationFeePmtReceiptBase64());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid Base64 encoded receipt image.");
            }
        }

        TenantRegistrationRequest registrationRequest = TenantRegistrationRequest.builder()
                .tenantBusinessName(request.getTenantBusinessName())
                .domainPrefix(request.getDomainPrefix().toLowerCase())
                .mailingAddress1(request.getMailingAddress1())
                .mailingAddress2(request.getMailingAddress2())
                .contactPerson(request.getContactPerson())
                .contactNumber1(request.getContactNumber1())
                .contactNumber2(request.getContactNumber2())
                .emailAddress(request.getEmailAddress())
                .maxInventoryItems(request.getMaxInventoryItems())
                .packageCode(tenantFees.getPackageCode())
                .registrationFee(tenantFees.getRegistrationFee())   // auto-populated from package
                .monthlyPayment(tenantFees.getMonthlyFee())          // auto-populated from package
                .approvalStatus(ApprovalStatus.P)                    // initially Pending
                .registrationFeePmtChannel(request.getRegistrationFeePmtChannel())
                .registrationFeePmtRef(request.getRegistrationFeePmtRef())
                .registrationFeePmtReceipt(receiptBytes)
                .build();

        TenantRegistrationRequest saved = registrationRepository.save(registrationRequest);
        log.info("Tenant registration request submitted: id={}, domainPrefix={}",
                saved.getId(), saved.getDomainPrefix());
        return toRegistrationResponse(saved);
    }

    // ------------------------------------------------------------------
    // LIST & SEARCH
    // ------------------------------------------------------------------

    /**
     * Returns all tenant registration requests ordered by submission date (newest first).
     *
     * @return list of all registration requests
     */
    public List<TenantRegistrationResponse> getAllRegistrations() {
        log.debug("Fetching all tenant registration requests");
        return registrationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toRegistrationResponse)
                .toList();
    }

    /**
     * Searches registration requests with multiple optional filter criteria.
     *
     * @param id                 optional filter by ID
     * @param packageCode        optional filter by package code
     * @param tenantBusinessName optional partial name search (case-insensitive)
     * @param approvalStatus     optional filter by status (P/A/R)
     * @param domainPrefix       optional filter by domain prefix
     * @param contactNumber      optional filter by contact number (searches both numbers)
     * @param emailAddress       optional filter by email address
     * @return list of matching registration requests
     */
    public List<TenantRegistrationResponse> searchRegistrations(
            Long id, String packageCode, String tenantBusinessName,
            ApprovalStatus approvalStatus, String domainPrefix,
            String contactNumber, String emailAddress) {

        log.debug("Searching registration requests with filters");
        return registrationRepository.searchRegistrations(
                id, packageCode, tenantBusinessName, approvalStatus,
                domainPrefix, contactNumber, emailAddress)
                .stream()
                .map(this::toRegistrationResponse)
                .toList();
    }

    /**
     * Returns a single registration request by its ID.
     *
     * @param id the registration request ID
     * @return the registration request as a response DTO
     * @throws ResourceNotFoundException if no request with the given ID exists
     */
    public TenantRegistrationResponse getRegistrationById(Long id) {
        log.debug("Fetching registration request by id: {}", id);
        TenantRegistrationRequest request = findRegistrationByIdOrThrow(id);
        return toRegistrationResponse(request);
    }

    // ------------------------------------------------------------------
    // DOMAIN AVAILABILITY CHECK (Public)
    // ------------------------------------------------------------------

    /**
     * Checks whether a domain prefix is available (not already in use).
     *
     * <p>A domain prefix is considered unavailable if it exists in either:
     * <ul>
     *   <li>{@code tenant_registration_request} (any status)</li>
     *   <li>{@code tenant_info} (any status)</li>
     * </ul>
     *
     * @param domainPrefix the domain prefix to check
     * @return a map with keys: "available" (boolean), "domainPrefix" (string), "message" (string)
     */
    public Map<String, Object> checkDomainAvailability(String domainPrefix) {
        log.debug("Checking domain availability for: {}", domainPrefix);
        boolean usedInRegistrations = registrationRepository.existsByDomainPrefix(domainPrefix.toLowerCase());
        boolean usedInTenants = tenantInfoRepository.existsByDomainPrefix(domainPrefix.toLowerCase());
        boolean available = !usedInRegistrations && !usedInTenants;

        String message = available
                ? String.format("Domain prefix '%s' is available.", domainPrefix)
                : String.format("Domain prefix '%s' is already taken.", domainPrefix);

        return Map.of(
                "available", available,
                "domainPrefix", domainPrefix.toLowerCase(),
                "message", message
        );
    }

    // ------------------------------------------------------------------
    // APPROVE / REJECT
    // ------------------------------------------------------------------

    /**
     * Approves or rejects a pending tenant registration request.
     *
     * <p>On Approval:
     * <ul>
     *   <li>Registration request status is updated to {@link ApprovalStatus#A}.</li>
     *   <li>A new {@link TenantInfo} record is created with {@link TenantStatus#A} (Active).</li>
     *   <li>The created tenant info is returned in the response.</li>
     * </ul>
     *
     * <p>On Rejection:
     * <ul>
     *   <li>Registration request status is updated to {@link ApprovalStatus#R}.</li>
     *   <li>No tenant account is created.</li>
     * </ul>
     *
     * @param id      the registration request ID
     * @param request the approval/rejection decision and optional remarks
     * @return result map with "registrationRequest" and optionally "tenantInfo" (if approved)
     * @throws ResourceNotFoundException if no request with the given ID exists
     * @throws IllegalArgumentException  if the request is not in Pending status
     */
    @Transactional
    public Map<String, Object> approveOrRejectRegistration(Long id, TenantApprovalRequest request) {
        log.debug("Processing approval decision for registration id={}, approved={}",
                id, request.getApproved());

        TenantRegistrationRequest registrationRequest = findRegistrationByIdOrThrow(id);

        // Only pending requests can be approved/rejected
        if (registrationRequest.getApprovalStatus() != ApprovalStatus.P) {
            throw new IllegalArgumentException(
                    String.format("Registration request id=%d is already in '%s' status. " +
                            "Only Pending (P) requests can be approved or rejected.",
                            id, registrationRequest.getApprovalStatus()));
        }

        // Set admin remarks if provided
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            registrationRequest.setAdminRemarks(request.getRemarks());
        }

        if (Boolean.TRUE.equals(request.getApproved())) {
            // ---- APPROVE ----
            registrationRequest.setApprovalStatus(ApprovalStatus.A);
            registrationRepository.save(registrationRequest);

            // Create the tenant account
            TenantInfo tenantInfo = TenantInfo.builder()
                    .tenantBusinessName(registrationRequest.getTenantBusinessName())
                    .domainPrefix(registrationRequest.getDomainPrefix())
                    .mailingAddress1(registrationRequest.getMailingAddress1())
                    .mailingAddress2(registrationRequest.getMailingAddress2())
                    .contactPerson(registrationRequest.getContactPerson())
                    .contactNumber1(registrationRequest.getContactNumber1())
                    .contactNumber2(registrationRequest.getContactNumber2())
                    .emailAddress(registrationRequest.getEmailAddress())
                    .maxInventoryItems(registrationRequest.getMaxInventoryItems())
                    .packageCode(registrationRequest.getPackageCode())
                    .status(TenantStatus.A)
                    .registrationRequestId(registrationRequest.getId())
                    .build();

            TenantInfo savedTenant = tenantInfoRepository.save(tenantInfo);
            log.info("Tenant registration approved: id={}, tenantId={}, domainPrefix={}",
                    id, savedTenant.getTenantId(), savedTenant.getDomainPrefix());

            return Map.of(
                    "registrationRequest", toRegistrationResponse(registrationRequest),
                    "tenantInfo", toTenantInfoResponse(savedTenant)
            );
        } else {
            // ---- REJECT ----
            registrationRequest.setApprovalStatus(ApprovalStatus.R);
            registrationRepository.save(registrationRequest);
            log.info("Tenant registration rejected: id={}", id);

            return Map.of(
                    "registrationRequest", toRegistrationResponse(registrationRequest)
            );
        }
    }

    // ------------------------------------------------------------------
    // Helper Methods
    // ------------------------------------------------------------------

    /**
     * Finds a registration request by ID or throws ResourceNotFoundException.
     */
    private TenantRegistrationRequest findRegistrationByIdOrThrow(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TenantRegistrationRequest", "id", id));
    }

    /**
     * Maps a {@link TenantRegistrationRequest} entity to a {@link TenantRegistrationResponse} DTO.
     * Receipt bytes are intentionally excluded from the response.
     */
    public TenantRegistrationResponse toRegistrationResponse(TenantRegistrationRequest entity) {
        return TenantRegistrationResponse.builder()
                .id(entity.getId())
                .tenantBusinessName(entity.getTenantBusinessName())
                .domainPrefix(entity.getDomainPrefix())
                .mailingAddress1(entity.getMailingAddress1())
                .mailingAddress2(entity.getMailingAddress2())
                .contactPerson(entity.getContactPerson())
                .contactNumber1(entity.getContactNumber1())
                .contactNumber2(entity.getContactNumber2())
                .emailAddress(entity.getEmailAddress())
                .maxInventoryItems(entity.getMaxInventoryItems())
                .packageCode(entity.getPackageCode())
                .registrationFee(entity.getRegistrationFee())
                .monthlyPayment(entity.getMonthlyPayment())
                .approvalStatus(entity.getApprovalStatus())
                .registrationFeePmtChannel(entity.getRegistrationFeePmtChannel())
                .registrationFeePmtRef(entity.getRegistrationFeePmtRef())
                .adminRemarks(entity.getAdminRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Maps a {@link TenantInfo} entity to a {@link TenantInfoResponse} DTO.
     */
    public TenantInfoResponse toTenantInfoResponse(TenantInfo entity) {
        return TenantInfoResponse.builder()
                .tenantId(entity.getTenantId())
                .tenantBusinessName(entity.getTenantBusinessName())
                .domainPrefix(entity.getDomainPrefix())
                .mailingAddress1(entity.getMailingAddress1())
                .mailingAddress2(entity.getMailingAddress2())
                .contactPerson(entity.getContactPerson())
                .contactNumber1(entity.getContactNumber1())
                .contactNumber2(entity.getContactNumber2())
                .emailAddress(entity.getEmailAddress())
                .maxInventoryItems(entity.getMaxInventoryItems())
                .packageCode(entity.getPackageCode())
                .status(entity.getStatus())
                .registrationRequestId(entity.getRegistrationRequestId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
