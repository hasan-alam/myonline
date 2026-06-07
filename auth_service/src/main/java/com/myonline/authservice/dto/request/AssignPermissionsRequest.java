package com.myonline.authservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Request payload for assigning or removing permissions from a role.
 */
@Data
@Schema(description = "Request to assign or remove permissions from a role")
public class AssignPermissionsRequest {

    @NotEmpty(message = "At least one permission ID is required")
    @Schema(description = "List of permission IDs to assign or remove", example = "[1, 2, 3]")
    private List<Long> permissionIds;
}
