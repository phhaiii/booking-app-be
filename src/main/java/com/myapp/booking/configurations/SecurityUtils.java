package com.myapp.booking.configurations;

import com.myapp.booking.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtils {


    public static Long getUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        } else if (principal instanceof Jwt jwt) {
            Object userIdClaim = jwt.getClaim("userId");
            if (userIdClaim instanceof Number) {
                return ((Number) userIdClaim).longValue();
            } else if (userIdClaim instanceof String) {
                return Long.parseLong((String) userIdClaim);
            }
        } else if (principal instanceof UserDetails userDetails) {

            throw new RuntimeException("Cannot extract user ID from UserDetails");
        }

        throw new RuntimeException("Cannot extract user ID from authentication: " + principal.getClass());
    }

    /**
     * ✅ NEW: Get user role from authentication
     */
    public static String getUserRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        // Get the first role (assuming single role per user)
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.startsWith("ROLE_"))
                .map(role -> role.substring(5)) // Remove "ROLE_" prefix
                .findFirst()
                .orElse("USER"); // Default to USER
    }

    /**
     * ✅ NEW: Check if user has specific role
     */
    public static boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String roleWithPrefix = "ROLE_" + role.toUpperCase();

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleWithPrefix));
    }

    /**
     * ✅ NEW: Check if user is admin
     */
    public static boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ADMIN");
    }

    /**
     * ✅ NEW: Check if user is vendor
     */
    public static boolean isVendor(Authentication authentication) {
        return hasRole(authentication, "VENDOR");
    }
}