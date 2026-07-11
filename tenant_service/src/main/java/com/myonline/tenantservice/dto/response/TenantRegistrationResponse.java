package com.myonline.tenantservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.myonline.tenantservice.enums.ApprovalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a tenant registration request.
 * Receipt bytes are excluded from responses to avoid large payloads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Tenant registration request details")
public class TenantRegistrationResponse {

    @Schema(description = "Registration request ID", example = "1")
    private Long id;

    @Schema(description = "Business name", example = "My Online Shop")
    private String tenantBusinessName;

    @Schema(description = "Subdomain prefix (e.g., 'myshop' → myshop.myonline.com)", example = "myshop")
    private String domainPrefix;

    @Schema(description = "Primary mailing address")
    private String mailingAddress1;

    @Schema(description = "Secondary mailing address (optional)")
    private String mailingAddress2;

    @Schema(description = "Primary contact person name")
    private String contactPerson;

    @Schema(description = "Primary contact phone number")
    private String contactNumber1;

    @Schema(description = "Secondary contact phone number (optional)")
    private String contactNumber2;

    @Schema(description = "Business email address")
    private String emailAddress;

    @Schema(description = "Maximum inventory items allowed", example = "50")
    private Integer maxInventoryItems;

    @Schema(description = "Selected subscription package code", example = "STARTER")
    private String packageCode;

    @Schema(description = "Registration fee (populated from tenant_fees)", example = "5000")
    private Integer registrationFee;

    @Schema(description = "Monthly fee (populated from tenant_fees)", example = "1000")
    private Integer monthlyPayment;

    @Schema(description = "Approval status: P=Pending, A=Approved, R=Rejected", example = "P")
    private ApprovalStatus approvalStatus;

    @Schema(description = "Payment channel for registration fee", example = "bKash")
    private String registrationFeePmtChannel;

    @Schema(description = "Payment reference number", example = "TXN123456")
    private String registrationFeePmtRef;

    @Schema(description = "Admin remarks on approval/rejection")
    private String adminRemarks;

    /**
     * Base64-encoded payment receipt image.
     * Only populated when fetching a single registration by ID (not included in list responses).
     */
    @Schema(description = "Base64-encoded payment receipt image (only returned in single-record fetch)")
    private String registrationFeePmtReceiptBase64;

    @Schema(description = "Submission timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}
