package com.myonline.tenantservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting a new tenant registration request.
 *
 * <p>This endpoint is publicly accessible (no authentication required).
 * Fee information (registrationFee, monthlyPayment) is auto-populated
 * from the selected packageCode in {@code tenant_fees}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Public tenant registration request submission")
public class SubmitRegistrationRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 255, message = "Business name must not exceed 255 characters")
    @Schema(description = "Name of the business", example = "My Online Shop")
    private String tenantBusinessName;

    @NotBlank(message = "Domain prefix is required")
    @Size(max = 100, message = "Domain prefix must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$",
        message = "Domain prefix must contain only lowercase letters, digits, and hyphens")
    @Schema(description = "Desired subdomain (e.g., 'myshop' → myshop.myonline.com)", example = "myshop")
    private String domainPrefix;

    @NotBlank(message = "Mailing address 1 is required")
    @Size(max = 255, message = "Mailing address 1 must not exceed 255 characters")
    @Schema(description = "Primary mailing address", example = "123 Main Street, Dhaka")
    private String mailingAddress1;

    @Size(max = 255, message = "Mailing address 2 must not exceed 255 characters")
    @Schema(description = "Secondary mailing address (optional)")
    private String mailingAddress2;

    @NotBlank(message = "Contact person name is required")
    @Size(max = 150, message = "Contact person name must not exceed 150 characters")
    @Schema(description = "Name of the primary contact person", example = "John Doe")
    private String contactPerson;

    @NotBlank(message = "Primary contact number is required")
    @Size(max = 20, message = "Contact number 1 must not exceed 20 characters")
    @Schema(description = "Primary contact phone number", example = "01712345678")
    private String contactNumber1;

    @Size(max = 20, message = "Contact number 2 must not exceed 20 characters")
    @Schema(description = "Secondary contact phone number (optional)", example = "01812345678")
    private String contactNumber2;

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be a valid email")
    @Size(max = 150, message = "Email address must not exceed 150 characters")
    @Schema(description = "Business email address", example = "owner@myshop.com")
    private String emailAddress;

    @NotNull(message = "Max inventory items is required")
    @Min(value = 1, message = "Max inventory items must be at least 1")
    @Schema(description = "Maximum number of inventory items needed", example = "50")
    private Integer maxInventoryItems;

    @NotBlank(message = "Package code is required")
    @Size(max = 25, message = "Package code must not exceed 25 characters")
    @Schema(description = "Selected subscription package code", example = "STARTER")
    private String packageCode;

    @Size(max = 100, message = "Payment channel must not exceed 100 characters")
    @Schema(description = "Payment channel used for registration fee (optional)", example = "bKash")
    private String registrationFeePmtChannel;

    @Size(max = 100, message = "Payment reference must not exceed 100 characters")
    @Schema(description = "Transaction reference for registration fee payment (optional)", example = "TXN123456")
    private String registrationFeePmtRef;

    /**
     * Base64-encoded screenshot/receipt of the registration fee payment.
     * Optional at submission time; can be null.
     */
    @Schema(description = "Base64-encoded payment receipt image (optional)")
    private String registrationFeePmtReceiptBase64;
}
