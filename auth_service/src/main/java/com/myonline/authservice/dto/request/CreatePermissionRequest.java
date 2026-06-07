package com.myonline.authservice.dto.request;

import com.myonline.authservice.enums.PortalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for creating a new permission.
 */
@Data
@Schema(description = "Request to create a new permission")
public class CreatePermissionRequest {

    @NotBlank(message = "Permission title is required")
    @Size(max = 100, message = "Permission title cannot exceed 100 characters")
    @Schema(description = "Unique title/code for the permission", example = "PRODUCT_CREATE")
    private String permissionTitle;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Description of what this permission allows", example = "Allows creating new products")
    private String permissionDescription;

    @NotNull(message = "permissionFor is required")
    @Schema(description = "Portal this permission applies to (SHPADMP / SYSADMP / BOTH)", example = "SHPADMP")
    private PortalType permissionFor;
}
