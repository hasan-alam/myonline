package com.myonline.authservice.dto.request;

import com.myonline.authservice.enums.PortalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for updating an existing permission.
 * All fields are optional — only provided fields will be updated.
 */
@Data
@Schema(description = "Request to update an existing permission (all fields optional)")
public class UpdatePermissionRequest {

    @Size(max = 100, message = "Permission title cannot exceed 100 characters")
    @Schema(description = "New unique title for the permission", example = "PRODUCT_EDIT")
    private String permissionTitle;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Updated description", example = "Allows editing existing products")
    private String permissionDescription;

    @Schema(description = "Updated portal assignment (SHPADMP / SYSADMP / BOTH)", example = "BOTH")
    private PortalType permissionFor;
}
