package com.myonline.authservice.dto.response;

import com.myonline.authservice.enums.PortalType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response payload representing a Permission resource.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Permission details")
public class PermissionResponse {

    @Schema(description = "Unique ID of the permission", example = "1")
    private Long id;

    @Schema(description = "Permission title/code", example = "PRODUCT_CREATE")
    private String permissionTitle;

    @Schema(description = "Description of the permission", example = "Allows creating new products")
    private String permissionDescription;

    @Schema(description = "Status: 1 = Active, 0 = Inactive", example = "1")
    private Integer permissionStatus;

    @Schema(description = "Portal this permission applies to", example = "SHPADMP")
    private PortalType permissionFor;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
