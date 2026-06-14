package com.myonline.tenantservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for tenant subscription package/fee information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tenant subscription package details")
public class TenantFeesResponse {

    @Schema(description = "Unique package code", example = "STARTER")
    private String packageCode;

    @Schema(description = "Human-readable package name", example = "Starter Package")
    private String packageName;

    @Schema(description = "Minimum product count (inclusive)", example = "1")
    private Integer productCountFrom;

    @Schema(description = "Maximum product count (inclusive)", example = "50")
    private Integer productCountTo;

    @Schema(description = "One-time registration fee", example = "5000")
    private Integer registrationFee;

    @Schema(description = "Recurring monthly subscription fee", example = "1000")
    private Integer monthlyFee;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Record last updated timestamp")
    private LocalDateTime updatedAt;
}
