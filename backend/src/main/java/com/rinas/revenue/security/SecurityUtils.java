package com.rinas.revenue.security;

import com.rinas.revenue.common.exception.ForbiddenOperationException;
import com.rinas.revenue.domain.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Reads the authenticated principal. Controllers use this to obtain the caller's
 * business id instead of accepting one from the request, which is the single
 * most important rule for tenant isolation.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AppUserPrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ForbiddenOperationException("Authentication is required");
        }
        return principal;
    }

    public static void requireRole(Role... allowed) {
        Role current = requirePrincipal().getRole();
        for (Role role : allowed) {
            if (role == current) {
                return;
            }
        }
        throw new ForbiddenOperationException("Your role is not permitted to perform this operation");
    }
}
