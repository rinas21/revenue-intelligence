package com.rinas.revenue.security;

import com.rinas.revenue.domain.Role;
import com.rinas.revenue.domain.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The authenticated identity carried on every request. Crucially it carries the
 * user's {@code businessId}, so controllers never accept a business id from the
 * request to decide what the caller may read or write.
 */
public class AppUserPrincipal implements UserDetails {

    private final UUID userId;
    private final UUID businessId;
    private final String username;
    private final String passwordHash;
    private final Role role;

    public AppUserPrincipal(UUID userId, UUID businessId, String username, String passwordHash, Role role) {
        this.userId = userId;
        this.businessId = businessId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public static AppUserPrincipal of(User user) {
        return new AppUserPrincipal(
            user.getId(), user.getBusinessId(), user.getUsername(), user.getPasswordHash(), user.getRole());
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public Role getRole() {
        return role;
    }

    public boolean hasRole(Role required) {
        return role == required;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // A single role per user today. Spring Security expects the ROLE_ prefix
        // for hasRole(...) checks, so it is applied here rather than at each use.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
