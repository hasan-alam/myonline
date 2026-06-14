package com.myonline.tenantservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new tenant subscription package.
 * All fields are required.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to create a new tenant subscription package")
public class CreateTenantFeesRequest {

    @NotBlank(message = "Package code is required")
    @Size(max = 25, message = "Package code must not exceed 25 characters")
    @Schema(description = "Unique package identifier (e.g., STARTER, BASIC)", example = "STARTER")
    private String packageCode;

    @NotBlank(message = "Package name is required")
    @Size(max = 100, message = "Package name must not exceed 100 characters")
    @Schema(description = "Human-readable package name", example = "Starter Package")
    private String packageName;

    @NotNull(message = "Product count from is required")
    @Min(value = 1, message = "Product count from must be at least 1")
    @Schema(description = "Minimum product count (inclusive)", example = "1")
    private Integer productCountFrom;

    @NotNull(message = "Product count to is required")
    @Min(value = 1, message = "Product count to must be at least 1")
    @Schema(description = "Maximum product count (inclusive)", example = "50")
    private Integer productCountTo;

    @NotNull(message = "Registration fee is required")
    @Min(value = 0, message = "Registration fee must be non-negative")
    @Schema(description = "One-time registration fee", example = "5000")
    private Integer registrationFee;

    @NotNull(message = "Monthly fee is required")
    @Min(value = 0, message = "Monthly fee must be non-negative")
    @Schema(description = "Recurring monthly subscription fee", example = "1000")
    private Integer monthlyFee;
}
