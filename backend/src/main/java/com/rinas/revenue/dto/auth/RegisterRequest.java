package com.rinas.revenue.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Creates a brand-new business and its first OWNER. Registration never joins an
 * existing business: that would let anyone claim a tenant they do not own.
 */
public record RegisterRequest(
    @NotBlank @Size(max = 255) String businessName,
    @NotBlank @Size(min = 3, max = 255) @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
        message = "username may contain letters, digits, dot, underscore and hyphen") String username,
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(min = 8, max = 128, message = "password must be at least 8 characters") String password
) {
}
