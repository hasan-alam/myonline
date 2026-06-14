package com.myonline.tenantservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.myonline.tenantservice.enums.TenantStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for an approved tenant account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Active tenant account details")
public class TenantInfoResponse {

    @Schema(description = "Tenant account ID", example = "1")
    private Long tenantId;

    @Schema(description = "Business name", example = "My Online Shop")
    private String tenantBusinessName;

    @Schema(description = "Subdomain prefix", example = "myshop")
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

    @Schema(description = "Subscription package code", example = "STARTER")
    private String packageCode;

    @Schema(description = "Tenant account status: A=Active, I=Inactive", example = "A")
    private TenantStatus status;

    @Schema(description = "Originating registration request ID", example = "1")
    private Long registrationRequestId;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;
}
