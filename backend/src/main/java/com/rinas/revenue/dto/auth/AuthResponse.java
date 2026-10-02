package com.rinas.revenue.dto.auth;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserSummary user
) {

    public record UserSummary(
        String id,
        String username,
        String email,
        String role,
        String businessId,
        String businessName
    ) {
    }
}
