package com.myonline.authservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Request payload for assigning roles to a user.
 */
@Data
@Schema(description = "Request to assign roles to a user")
public class AssignRolesRequest {

    @NotEmpty(message = "At least one role ID is required")
    @Schema(description = "List of role IDs to assign to the user", example = "[1, 2]")
    private List<Long> roleIds;
}
