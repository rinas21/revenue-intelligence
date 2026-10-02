package com.rinas.revenue.dto.user;

import com.rinas.revenue.domain.Role;
import com.rinas.revenue.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String email,
    String role,
    Instant createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(), user.getUsername(), user.getEmail(), user.getRole().name(), user.getCreatedAt());
    }

    /** Creates a user inside the caller's business. */
    public record CreateRequest(
        @NotBlank @Size(min = 3, max = 255) @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
            message = "username may contain letters, digits, dot, underscore and hyphen") String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotNull Role role
    ) {
    }
}
