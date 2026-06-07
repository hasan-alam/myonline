package com.myonline.authservice.security;

import com.myonline.authservice.entity.Permission;
import com.myonline.authservice.entity.Role;
import com.myonline.authservice.entity.User;
import com.myonline.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security UserDetailsService implementation.
 *
 * <p>Loads user details by email address during authentication.
 * Grants authorities based on the user's assigned roles and permissions.
 *
 * <p>Authority format:
 * <ul>
 *   <li>Roles: {@code ROLE_SUPER_ADMIN}, {@code ROLE_SHOP_ADMIN}</li>
 *   <li>Permissions: {@code PRODUCT_CREATE}, {@code USER_MANAGE}</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load a user by their email address.
     *
     * @param email the email address used as the username
     * @return UserDetails with authorities derived from the user's roles and permissions
     * @throws UsernameNotFoundException if no user with the given email exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // Build granted authorities from roles and their permissions
        List<GrantedAuthority> authorities = buildAuthorities(user);

        log.debug("User '{}' loaded with {} authorities", email, authorities.size());

        // Return Spring Security UserDetails
        // enabled = userStatus == 1 (Active)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getUserStatus() != 1) // disabled if status is Inactive (0)
                .build();
    }

    /**
     * Build a list of GrantedAuthority objects from the user's roles and permissions.
     *
     * <p>Roles are prefixed with "ROLE_" (Spring Security convention).
     * Permissions are added as-is (e.g., "PRODUCT_CREATE").
     *
     * @param user the authenticated user entity
     * @return list of authorities
     */
    private List<GrantedAuthority> buildAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                // Skip inactive roles
                if (role.getRoleStatus() != 1) {
                    continue;
                }
                // Add role authority with ROLE_ prefix
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));

                // Add permission authorities from each role
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        if (permission.getPermissionStatus() == 1) {
                            authorities.add(new SimpleGrantedAuthority(permission.getPermissionTitle()));
                        }
                    }
                }
            }
        }

        return authorities;
    }
}
