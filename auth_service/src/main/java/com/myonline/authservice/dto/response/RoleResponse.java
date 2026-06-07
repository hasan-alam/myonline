package com.myonline.authservice.dto.response;

import com.myonline.authservice.enums.PortalType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload representing a Role resource, including its assigned permissions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role details including assigned permissions")
public class RoleResponse {

    @Schema(description = "Unique ID of the role", example = "1")
    private Long roleId;

    @Schema(description = "Role name", example = "SUPER_ADMIN")
    private String roleName;

    @Schema(description = "Role description", example = "Platform super administrator with full access")
    private String roleDescription;

    @Schema(description = "Status: 1 = Active, 0 = Inactive", example = "1")
    private Integer roleStatus;

    @Schema(description = "Portal this role applies to", example = "SYSADMP")
    private PortalType roleFor;

    @Schema(description = "Tenant/shop ID (null for system roles)", example = "null")
    private Long shopId;

    @Schema(description = "Permissions assigned to this role")
    private List<PermissionResponse> permissions;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
