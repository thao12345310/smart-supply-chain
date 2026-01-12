package com.distribution.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Security utility class for accessing current user information
 */
@Component
public class SecurityUtils {

    /**
     * Get the current authenticated user details
     */
    public static Optional<CustomUserDetails> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof CustomUserDetails) {
            return Optional.of((CustomUserDetails) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    /**
     * Get the current user ID
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(CustomUserDetails::getId);
    }

    /**
     * Get the current username
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(CustomUserDetails::getUsername);
    }

    /**
     * Check if current user has a specific role
     */
    public static boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.hasRole(role))
                .orElse(false);
    }

    /**
     * Check if current user has any of the specified roles
     */
    public static boolean hasAnyRole(String... roles) {
        return getCurrentUser()
                .map(user -> user.hasAnyRole(roles))
                .orElse(false);
    }

    /**
     * Check if current user is an admin
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user can approve orders
     */
    public static boolean canApprove() {
        return getCurrentUser()
                .map(CustomUserDetails::canApprove)
                .orElse(false);
    }

    /**
     * Check if current user has restricted view (shipper)
     */
    public static boolean hasRestrictedView() {
        return getCurrentUser()
                .map(CustomUserDetails::hasRestrictedView)
                .orElse(true); // Default to restricted if not authenticated
    }

    /**
     * Check if current user can see unapproved orders
     */
    public static boolean canSeeUnapprovedOrders() {
        return getCurrentUser()
                .map(CustomUserDetails::canSeeUnapprovedOrders)
                .orElse(false);
    }

    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails;
    }
}
