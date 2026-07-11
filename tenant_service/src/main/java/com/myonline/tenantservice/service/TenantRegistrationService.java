package com.myonline.tenantservice.service;

import com.myonline.tenantservice.client.AuthServiceClient;
import com.myonline.tenantservice.dto.request.SubmitRegistrationRequest;
import com.myonline.tenantservice.dto.request.TenantApprovalRequest;
import com.myonline.tenantservice.dto.response.TenantInfoResponse;
import com.myonline.tenantservice.dto.response.TenantRegistrationResponse;
import com.myonline.tenantservice.entity.TenantFees;
import com.myonline.tenantservice.entity.TenantInfo;
import com.myonline.tenantservice.entity.TenantRegistrationNotification;
import com.myonline.tenantservice.entity.TenantRegistrationRequest;
import com.myonline.tenantservice.enums.ApprovalStatus;
import com.myonline.tenantservice.enums.NotificationStatus;
import com.myonline.tenantservice.enums.TenantStatus;
import com.myonline.tenantservice.exception.DuplicateResourceException;
import com.myonline.tenantservice.exception.ResourceNotFoundException;
import com.myonline.tenantservice.repository.TenantInfoRepository;
import com.myonline.tenantservice.repository.TenantRegistrationNotificationRepository;
import com.myonline.tenantservice.repository.TenantRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

/**
 * Service class for managing tenant registration requests and tenant info.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantRegistrationService {

    private final TenantRegistrationRepository registrationRepository;
    private final TenantInfoRepository tenantInfoRepository;
    private final TenantRegistrationNotificationRepository notificationRepository;
    private final TenantFeesService tenantFeesService;
    private final AuthServiceClient authServiceClient;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ------------------------------------------------------------------
    // SUBMIT REGISTRATION (Public)
    // ------------------------------------------------------------------

    @Transactional
    public TenantRegistrationResponse submitRegistration(SubmitRegistrationRequest request) {
        log.debug("Submitting tenant registration for domainPrefix: {}", request.getDomainPrefix());

        if (registrationRepository.existsByDomainPrefix(request.getDomainPrefix())) {
            throw new DuplicateResourceException(
                    "TenantRegistrationRequest", "domainPrefix", request.getDomainPrefix());
        }
        if (tenantInfoRepository.existsByDomainPrefix(request.getDomainPrefix())) {
            throw new DuplicateResourceException(
                    "TenantInfo", "domainPrefix", request.getDomainPrefix());
        }

        // Check email uniqueness against auth_service before accepting the registration
        if (authServiceClient.checkEmailCount(request.getEmailAddress()) > 0) {
            throw new DuplicateResourceException("User", "emailAddress", request.getEmailAddress());
        }

        // Check primary contact number uniqueness against auth_service
        if (authServiceClient.checkMobileCount(request.getContactNumber1()) > 0) {
            throw new DuplicateResourceException("User", "contactNumber1", request.getContactNumber1());
        }

        TenantFees tenantFees = tenantFeesService.findByPackageCodeOrThrow(request.getPackageCode());

        if (request.getMaxInventoryItems() < tenantFees.getProductCountFrom()
                || request.getMaxInventoryItems() > tenantFees.getProductCountTo()) {
            throw new IllegalArgumentException(
                    String.format("maxInventoryItems (%d) must be within the selected package's range [%d, %d] for package '%s'",
                            request.getMaxInventoryItems(),
                            tenantFees.getProductCountFrom(),
                            tenantFees.getProductCountTo(),
                            tenantFees.getPackageCode()));
        }

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
                .registrationFee(tenantFees.getRegistrationFee())
                .monthlyPayment(tenantFees.getMonthlyFee())
                .approvalStatus(ApprovalStatus.P)
                .registrationFeePmtChannel(request.getRegistrationFeePmtChannel())
                .registrationFeePmtRef(request.getRegistrationFeePmtRef())
                .registrationFeePmtReceipt(receiptBytes)
                .build();

        TenantRegistrationRequest saved = registrationRepository.save(registrationRequest);
        log.info("Tenant registration submitted: id={}, domainPrefix={}", saved.getId(), saved.getDomainPrefix());
        return toRegistrationResponse(saved);
    }

    // ------------------------------------------------------------------
    // LIST & SEARCH
    // ------------------------------------------------------------------

    public List<TenantRegistrationResponse> getAllRegistrations() {
        return registrationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toRegistrationResponse)
                .toList();
    }

    public List<TenantRegistrationResponse> searchRegistrations(
            Long id, String packageCode, String tenantBusinessName,
            ApprovalStatus approvalStatus, String domainPrefix,
            String contactNumber, String emailAddress) {

        return registrationRepository.searchRegistrations(
                id, packageCode, tenantBusinessName, approvalStatus,
                domainPrefix, contactNumber, emailAddress)
                .stream()
                .map(this::toRegistrationResponse)
                .toList();
    }

    /**
     * Returns a single registration by ID, including the Base64-encoded receipt image.
     */
    public TenantRegistrationResponse getRegistrationById(Long id) {
        TenantRegistrationRequest request = findRegistrationByIdOrThrow(id);
        return toRegistrationResponseWithReceipt(request);
    }

    // ------------------------------------------------------------------
    // DOMAIN AVAILABILITY CHECK (Public)
    // ------------------------------------------------------------------

    public Map<String, Object> checkDomainAvailability(String domainPrefix) {
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
     * Approves or rejects a pending tenant registration.
     *
     * <p>On Approval:
     * <ol>
     *   <li>Updates registration status to A (Approved).</li>
     *   <li>Creates a {@link TenantInfo} record with Active status.</li>
     *   <li>Calls auth_service to create a SHOP_ADMIN user (shop_id = tenant_id, user_for = SHPADMP).
     *       A random default password is generated; the approver's JWT is forwarded.
     *       Email uniqueness is guaranteed because it is enforced at registration submission time.</li>
     *   <li>Assigns the SHOP_ADMIN role to the newly created user.</li>
     *   <li>Saves a PENDING notification with the outcome message.</li>
     * </ol>
     *
     * <p>On Rejection:
     * <ol>
     *   <li>Updates registration status to R (Rejected).</li>
     *   <li>Saves a PENDING notification with the rejection reason.</li>
     * </ol>
     *
     * @param id         registration request ID
     * @param request    approval/rejection details
     * @param authHeader the caller's Authorization header (forwarded to auth_service)
     * @return result map containing registrationRequest, optionally tenantInfo,
     *         userCreated flag, and generatedPassword (when a new user is created)
     */
    @Transactional
    public Map<String, Object> approveOrRejectRegistration(Long id, TenantApprovalRequest request,
                                                            String authHeader) {
        TenantRegistrationRequest reg = findRegistrationByIdOrThrow(id);

        if (reg.getApprovalStatus() != ApprovalStatus.P) {
            throw new IllegalArgumentException(
                    String.format("Registration id=%d is already '%s'. Only Pending (P) can be processed.",
                            id, reg.getApprovalStatus()));
        }

        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            reg.setAdminRemarks(request.getRemarks());
        }

        if (Boolean.TRUE.equals(request.getApproved())) {
            return handleApproval(reg, authHeader);
        } else {
            return handleRejection(reg, request.getRemarks());
        }
    }

    // ------------------------------------------------------------------
    // Private — Approval flow
    // ------------------------------------------------------------------

    private Map<String, Object> handleApproval(TenantRegistrationRequest reg, String authHeader) {
        reg.setApprovalStatus(ApprovalStatus.A);
        registrationRepository.save(reg);

        // 1. Create TenantInfo
        TenantInfo tenantInfo = TenantInfo.builder()
                .tenantBusinessName(reg.getTenantBusinessName())
                .domainPrefix(reg.getDomainPrefix())
                .mailingAddress1(reg.getMailingAddress1())
                .mailingAddress2(reg.getMailingAddress2())
                .contactPerson(reg.getContactPerson())
                .contactNumber1(reg.getContactNumber1())
                .contactNumber2(reg.getContactNumber2())
                .emailAddress(reg.getEmailAddress())
                .maxInventoryItems(reg.getMaxInventoryItems())
                .packageCode(reg.getPackageCode())
                .status(TenantStatus.A)
                .registrationRequestId(reg.getId())
                .build();
        TenantInfo savedTenant = tenantInfoRepository.save(tenantInfo);
        log.info("TenantInfo created: tenantId={}, domain={}", savedTenant.getTenantId(), savedTenant.getDomainPrefix());

        // 2. Create user in auth_service
        String generatedPassword = null;
        boolean userCreated = false;

        try {
            generatedPassword = generateDefaultPassword();
            Long newUserId = authServiceClient.createUser(
                    reg.getContactPerson(),
                    reg.getContactNumber1(),
                    reg.getEmailAddress(),
                    generatedPassword,
                    savedTenant.getTenantId(),
                    authHeader
            );

            if (newUserId != null) {
                userCreated = true;
                log.info("User created in auth_service: userId={}, shopId(tenantId)={}, email={}",
                        newUserId, savedTenant.getTenantId(), reg.getEmailAddress());

                // 3. Assign SHOP_ADMIN role
                Long shopAdminRoleId = authServiceClient.findShopAdminRoleId(authHeader);
                if (shopAdminRoleId != null) {
                    authServiceClient.assignRoleToUser(newUserId, shopAdminRoleId, authHeader);
                    log.info("SHOP_ADMIN role assigned to userId={}", newUserId);
                } else {
                    log.warn("SHOP_ADMIN role not found in auth_service — user {} created without role.", newUserId);
                }
            } else {
                // Should not happen: email uniqueness is enforced at registration submission time.
                // If auth_service returns null here it means a duplicate slipped through — log it prominently.
                generatedPassword = null;
                log.error("auth_service returned no userId for email='{}' (registration id={}) — user was NOT created.",
                        reg.getEmailAddress(), reg.getId());
            }
        } catch (Exception ex) {
            log.error("User creation in auth_service FAILED for registration id={}: {}",
                    reg.getId(), ex.getMessage(), ex);
            generatedPassword = null;
        }

        // 4. Save notification
        String notifMessage = buildApprovalMessage(reg.getContactPerson(), userCreated, generatedPassword);
        saveNotification(reg, null, notifMessage);

        // 5. Build response
        Map<String, Object> result = new HashMap<>();
        result.put("registrationRequest", toRegistrationResponse(reg));
        result.put("tenantInfo", toTenantInfoResponse(savedTenant));
        result.put("userCreated", userCreated);
        if (userCreated && generatedPassword != null) {
            result.put("generatedPassword", generatedPassword);
        }
        return result;
    }

    private Map<String, Object> handleRejection(TenantRegistrationRequest reg, String reason) {
        reg.setApprovalStatus(ApprovalStatus.R);
        registrationRepository.save(reg);
        log.info("Tenant registration rejected: id={}", reg.getId());

        String notifMessage = buildRejectionMessage(reg.getContactPerson(), reason);
        saveNotification(reg, reason, notifMessage);

        Map<String, Object> result = new HashMap<>();
        result.put("registrationRequest", toRegistrationResponse(reg));
        return result;
    }

    // ------------------------------------------------------------------
    // Private — Notification
    // ------------------------------------------------------------------

    private void saveNotification(TenantRegistrationRequest reg, String reason, String message) {
        try {
            TenantRegistrationNotification notification = TenantRegistrationNotification.builder()
                    .emailAddress(reg.getEmailAddress())
                    .mobileNumber(reg.getContactNumber1())
                    .reasonForRejection(reason)
                    .message(message)
                    .status(NotificationStatus.PENDING)
                    .registrationRequestId(reg.getId())
                    .build();
            notificationRepository.save(notification);
            log.info("Notification saved for registration id={}", reg.getId());
        } catch (Exception ex) {
            log.error("Failed to save notification for registration {}: {}", reg.getId(), ex.getMessage());
        }
    }

    private String buildApprovalMessage(String name, boolean userCreated, String password) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(name).append(",\n\n");
        sb.append("Your tenant account registration is approved.\n");
        if (userCreated && password != null) {
            sb.append("Your default password is: ").append(password).append(".\n");
            sb.append("Please log in and change your password immediately.\n");
        } else {
            sb.append("You can login with your existing password by selecting the Business.\n");
        }
        sb.append("\nThank you");
        return sb.toString();
    }

    private String buildRejectionMessage(String name, String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(name).append(",\n\n");
        sb.append("Your tenant account registration is rejected.\n");
        if (reason != null && !reason.isBlank()) {
            sb.append("Reason of rejection: ").append(reason).append("\n");
        }
        sb.append("\nThank you");
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Private — Password Generation
    // ------------------------------------------------------------------

    /**
     * Generates a cryptographically random 10-character password containing
     * at least one uppercase letter, one lowercase letter, one digit, and one special character.
     */
    private String generateDefaultPassword() {
        String upper   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower   = "abcdefghijklmnopqrstuvwxyz";
        String digits  = "0123456789";
        String special = "@#$!";
        String all     = upper + lower + digits + special;

        List<Character> chars = new ArrayList<>();
        chars.add(upper.charAt(SECURE_RANDOM.nextInt(upper.length())));
        chars.add(lower.charAt(SECURE_RANDOM.nextInt(lower.length())));
        chars.add(digits.charAt(SECURE_RANDOM.nextInt(digits.length())));
        chars.add(special.charAt(SECURE_RANDOM.nextInt(special.length())));
        for (int i = 4; i < 10; i++) {
            chars.add(all.charAt(SECURE_RANDOM.nextInt(all.length())));
        }
        Collections.shuffle(chars, SECURE_RANDOM);

        StringBuilder sb = new StringBuilder(10);
        chars.forEach(sb::append);
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Helper Methods
    // ------------------------------------------------------------------

    private TenantRegistrationRequest findRegistrationByIdOrThrow(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TenantRegistrationRequest", "id", id));
    }

    /**
     * Maps entity → response DTO without the receipt image (used for list endpoints).
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
     * Maps entity → response DTO including the Base64-encoded receipt image (used for single-record fetch).
     */
    public TenantRegistrationResponse toRegistrationResponseWithReceipt(TenantRegistrationRequest entity) {
        String receiptBase64 = null;
        if (entity.getRegistrationFeePmtReceipt() != null && entity.getRegistrationFeePmtReceipt().length > 0) {
            receiptBase64 = Base64.getEncoder().encodeToString(entity.getRegistrationFeePmtReceipt());
        }
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
                .registrationFeePmtReceiptBase64(receiptBase64)
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
