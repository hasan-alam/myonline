package com.myonline.authservice.service;

import com.myonline.authservice.dto.request.AssignRolesRequest;
import com.myonline.authservice.dto.request.CreateUserRequest;
import com.myonline.authservice.dto.response.UserResponse;
import com.myonline.authservice.entity.Role;
import com.myonline.authservice.entity.User;
import com.myonline.authservice.exception.DuplicateResourceException;
import com.myonline.authservice.exception.ResourceNotFoundException;
import com.myonline.authservice.repository.RefreshTokenRepository;
import com.myonline.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing users (create, activate, deactivate, delete, role assignment).
 *
 * <p>Users are associated with a portal (SHPADMP or SYSADMP) and optionally a shop (tenant).
 * Passwords are always stored as BCrypt hashes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    // =============================================
    // Create
    // =============================================

    /**
     * Create a new user account.
     *
     * @param request the user creation request
     * @return the created user as a response DTO
     * @throws DuplicateResourceException if the email or mobile already exists
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        // Validate uniqueness of email and mobile
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new DuplicateResourceException("User", "mobile", request.getMobile());
        }

        User user = User.builder()
                .name(request.getName())
                .mobile(request.getMobile())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hash
                .userFor(request.getUserFor())
                .shopId(request.getShopId())
                .userStatus(1) // Active by default
                .roles(new ArrayList<>())
                .build();

        User saved = userRepository.save(user);
        log.info("User created successfully with ID: {}", saved.getUserId());
        return toResponse(saved);
    }

    // =============================================
    // Read
    // =============================================

    /**
     * Count users with the given email address.
     * Since email is unique, the result is always 0 (not found) or 1 (exists).
     *
     * @param email the email address to check
     * @return 0 if no user has this email, 1 if a user exists
     */
    @Transactional(readOnly = true)
    public int countByEmail(String email) {
        return userRepository.countByEmail(email);
    }

    /**
     * Count users with the given mobile number.
     * Since mobile is unique, the result is always 0 (not found) or 1 (exists).
     *
     * @param mobile the mobile number to check
     * @return 0 if no user has this mobile, 1 if a user exists
     */
    @Transactional(readOnly = true)
    public int countByMobile(String mobile) {
        return userRepository.countByMobile(mobile);
    }

    /**
     * Retrieve all users.
     *
     * @return list of all users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a user by ID.
     *
     * @param id the user ID
     * @return the user response DTO
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user with ID: {}", id);
        return toResponse(findUserById(id));
    }

    /**
     * Retrieve all users belonging to a specific shop/tenant.
     *
     * @param shopId the shop/tenant ID
     * @return list of users for the shop
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByShop(Long shopId) {
        log.debug("Fetching users for shopId: {}", shopId);
        return userRepository.findByShopId(shopId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =============================================
    // Activate / Deactivate
    // =============================================

    /**
     * Activate a user account (set status to 1).
     *
     * @param id the user ID
     * @return updated user response DTO
     */
    @Transactional
    public UserResponse activateUser(Long id) {
        log.info("Activating user with ID: {}", id);
        User user = findUserById(id);
        user.setUserStatus(1);
        return toResponse(userRepository.save(user));
    }

    /**
     * Deactivate a user account (set status to 0).
     * Also revokes all active refresh tokens for the user.
     *
     * @param id the user ID
     * @return updated user response DTO
     */
    @Transactional
    public UserResponse deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);
        User user = findUserById(id);
        user.setUserStatus(0);
        // Revoke active tokens so the user is immediately logged out
        refreshTokenRepository.revokeAllByUser(user);
        return toResponse(userRepository.save(user));
    }

    // =============================================
    // Role Assignment
    // =============================================

    /**
     * Assign roles to a user.
     * Roles already assigned are not duplicated.
     *
     * @param userId  the user ID
     * @param request contains the list of role IDs to assign
     * @return the updated user response DTO
     */
    @Transactional
    public UserResponse assignRoles(Long userId, AssignRolesRequest request) {
        log.info("Assigning {} role(s) to user ID: {}", request.getRoleIds().size(), userId);

        User user = findUserById(userId);
        List<Role> currentRoles = user.getRoles();
        if (currentRoles == null) {
            currentRoles = new ArrayList<>();
        }

        for (Long roleId : request.getRoleIds()) {
            Role role = roleService.findRoleById(roleId);
            boolean alreadyAssigned = currentRoles.stream()
                    .anyMatch(r -> r.getRoleId().equals(roleId));
            if (!alreadyAssigned) {
                currentRoles.add(role);
            }
        }

        user.setRoles(currentRoles);
        User updated = userRepository.save(user);
        log.info("Roles assigned to user ID: {}", userId);
        return toResponse(updated);
    }

    /**
     * Remove specific roles from a user.
     *
     * @param userId  the user ID
     * @param request contains the list of role IDs to remove
     * @return the updated user response DTO
     */
    @Transactional
    public UserResponse removeRoles(Long userId, AssignRolesRequest request) {
        log.info("Removing {} role(s) from user ID: {}", request.getRoleIds().size(), userId);

        User user = findUserById(userId);
        List<Role> currentRoles = user.getRoles();
        if (currentRoles != null) {
            currentRoles.removeIf(r -> request.getRoleIds().contains(r.getRoleId()));
            user.setRoles(currentRoles);
        }

        User updated = userRepository.save(user);
        log.info("Roles removed from user ID: {}", userId);
        return toResponse(updated);
    }

    // =============================================
    // Delete
    // =============================================

    /**
     * Permanently delete a user account.
     * Also deletes all associated refresh tokens.
     *
     * @param id the user ID
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        User user = findUserById(id);
        // Clean up refresh tokens before deleting user
        refreshTokenRepository.deleteByUser(user);
        userRepository.delete(user);
        log.info("User ID {} deleted", id);
    }

    // =============================================
    // Private Helpers
    // =============================================

    /**
     * Find a user by ID or throw ResourceNotFoundException.
     */
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    // =============================================
    // Mapper
    // =============================================

    /**
     * Map a User entity to its response DTO.
     * Password is never included in the response.
     */
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .userFor(user.getUserFor())
                .userStatus(user.getUserStatus())
                .shopId(user.getShopId())
                .roles(user.getRoles() != null
                        ? user.getRoles().stream()
                        .map(roleService::toResponse)
                        .collect(Collectors.toList())
                        : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
