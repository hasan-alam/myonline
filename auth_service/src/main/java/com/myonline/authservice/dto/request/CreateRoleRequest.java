package com.myonline.authservice.dto.request;

import com.myonline.authservice.enums.PortalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for creating a new role.
 */
@Data
@Schema(description = "Request to create a new role")
public class CreateRoleRequest {

    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name cannot exceed 100 characters")
    @Schema(description = "Unique name for the role", example = "SHOP_MANAGER")
    private String roleName;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Detailed description of the role", example = "Manages shop inventory and orders")
    private String roleDescription;

    @NotNull(message = "roleFor is required")
    @Schema(description = "Portal this role applies to (SHPADMP / SYSADMP / BOTH)", example = "SHPADMP")
    private PortalType roleFor;

    @Schema(description = "Tenant/shop ID (required for tenant-specific roles; null for system roles)", example = "1")
    private Long shopId;
}
