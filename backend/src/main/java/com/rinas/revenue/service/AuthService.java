package com.rinas.revenue.service;

import com.rinas.revenue.common.exception.ConflictException;
import com.rinas.revenue.domain.Business;
import com.rinas.revenue.domain.Role;
import com.rinas.revenue.domain.User;
import com.rinas.revenue.dto.auth.AuthResponse;
import com.rinas.revenue.dto.auth.LoginRequest;
import com.rinas.revenue.dto.auth.RegisterRequest;
import com.rinas.revenue.repository.BusinessRepository;
import com.rinas.revenue.repository.UserRepository;
import com.rinas.revenue.security.AppUserPrincipal;
import com.rinas.revenue.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration, login, refresh and logout. Registration creates a new business
 * and its first OWNER in one transaction; there is no path that adds a user to
 * a business the caller does not own.
 */
@Service
public class AuthService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(BusinessRepository businessRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
            JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw ConflictException.duplicate("Username '" + request.username() + "'");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw ConflictException.duplicate("Email '" + request.email() + "'");
        }

        Business business = businessRepository.save(new Business(request.businessName()));
        User owner = userRepository.save(new User(
            request.username(),
            request.email(),
            passwordEncoder.encode(request.password()),
            business,
            Role.OWNER));

        return issueTokens(owner);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByIdAndBusinessId(principal.getUserId(), principal.getBusinessId())
            .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        User user = refreshTokenService.rotate(rawRefreshToken);
        return issueTokens(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revoke(rawRefreshToken);
        }
    }

    private AuthResponse issueTokens(User user) {
        AppUserPrincipal principal = AppUserPrincipal.of(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = refreshTokenService.issue(user);
        Business business = user.getBusiness();
        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            jwtService.getAccessTokenTtlSeconds(),
            new AuthResponse.UserSummary(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                business.getId().toString(),
                business.getName()));
    }
}
