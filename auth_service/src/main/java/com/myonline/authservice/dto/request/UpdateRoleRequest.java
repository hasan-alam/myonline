package com.myonline.authservice.dto.request;

import com.myonline.authservice.enums.PortalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for updating an existing role.
 * All fields are optional — only provided fields will be updated.
 */
@Data
@Schema(description = "Request to update an existing role (all fields optional)")
public class UpdateRoleRequest {

    @Size(max = 100, message = "Role name cannot exceed 100 characters")
    @Schema(description = "New unique name for the role", example = "SENIOR_SHOP_MANAGER")
    private String roleName;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Updated description of the role", example = "Senior manager with extended permissions")
    private String roleDescription;

    @Schema(description = "Updated portal assignment (SHPADMP / SYSADMP / BOTH)", example = "SHPADMP")
    private PortalType roleFor;
}
