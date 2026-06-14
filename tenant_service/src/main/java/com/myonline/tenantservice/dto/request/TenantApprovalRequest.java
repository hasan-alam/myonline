package com.myonline.tenantservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for approving or rejecting a tenant registration request.
 *
 * <p>Used by system admins with the {@code TENANT_PAYMENT_APPROVAL} permission.
 * The {@code approved} flag determines whether to approve or reject the request:
 * <ul>
 *   <li>{@code true} → Approval: creates a {@code TenantInfo} record with Active status.</li>
 *   <li>{@code false} → Rejection: updates request status to Rejected; no tenant is created.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to approve or reject a tenant registration request")
public class TenantApprovalRequest {

    @NotNull(message = "Approval decision is required (true = approve, false = reject)")
    @Schema(description = "true to approve, false to reject", example = "true")
    private Boolean approved;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    @Schema(description = "Optional remarks from the admin (visible in the registration request record)",
        example = "Payment verified. Approved.")
    private String remarks;
}
