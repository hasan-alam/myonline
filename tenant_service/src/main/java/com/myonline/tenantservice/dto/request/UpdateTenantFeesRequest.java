package com.myonline.tenantservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing tenant subscription package.
 * All fields are optional — only provided fields will be updated.
 * Note: packageCode (primary key) cannot be changed via update.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to update a tenant subscription package. Only provided fields are updated.")
public class UpdateTenantFeesRequest {

    @Size(max = 100, message = "Package name must not exceed 100 characters")
    @Schema(description = "Human-readable package name", example = "Starter Package")
    private String packageName;

    @Min(value = 1, message = "Product count from must be at least 1")
    @Schema(description = "Minimum product count (inclusive)", example = "1")
    private Integer productCountFrom;

    @Min(value = 1, message = "Product count to must be at least 1")
    @Schema(description = "Maximum product count (inclusive)", example = "50")
    private Integer productCountTo;

    @Min(value = 0, message = "Registration fee must be non-negative")
    @Schema(description = "One-time registration fee", example = "5000")
    private Integer registrationFee;

    @Min(value = 0, message = "Monthly fee must be non-negative")
    @Schema(description = "Recurring monthly subscription fee", example = "1000")
    private Integer monthlyFee;
}
