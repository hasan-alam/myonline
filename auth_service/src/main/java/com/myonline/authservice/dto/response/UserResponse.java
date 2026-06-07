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
 * Response payload representing a User resource.
 * Note: Password is never included in responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User details (password is never exposed)")
public class UserResponse {

    @Schema(description = "Unique ID of the user", example = "1")
    private Long userId;

    @Schema(description = "Full name", example = "John Doe")
    private String name;

    @Schema(description = "Mobile number", example = "+8801700000000")
    private String mobile;

    @Schema(description = "Email address", example = "john@shopname.com")
    private String email;

    @Schema(description = "Portal this user belongs to", example = "SHPADMP")
    private PortalType userFor;

    @Schema(description = "Status: 1 = Active, 0 = Inactive", example = "1")
    private Integer userStatus;

    @Schema(description = "Tenant/shop ID (null for system admins)", example = "1")
    private Long shopId;

    @Schema(description = "Roles assigned to this user")
    private List<RoleResponse> roles;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
