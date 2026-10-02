package com.rinas.revenue.service;

import com.rinas.revenue.common.exception.ResourceNotFoundException;
import com.rinas.revenue.domain.RefreshToken;
import com.rinas.revenue.domain.User;
import com.rinas.revenue.repository.RefreshTokenRepository;
import com.rinas.revenue.security.SecurityProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues, rotates and revokes opaque refresh tokens. Only a SHA-256 hash is
 * persisted, so a database disclosure does not yield usable sessions. Rotation
 * on every refresh means a stolen token is single-use at best, and reuse of a
 * revoked token is rejected.
 */
@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityProperties properties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, SecurityProperties properties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.properties = properties;
    }

    @Transactional
    public String issue(User user) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiry = Instant.now().plus(properties.getRefreshTokenTtl());
        refreshTokenRepository.save(new RefreshToken(user, hash(raw), expiry));
        return raw;
    }

    /** Validates a refresh token, revokes it and returns its owning user. */
    @Transactional
    public User rotate(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(rawToken))
            .orElseThrow(() -> new ResourceNotFoundException("Refresh token is not valid"));
        if (!token.isActive()) {
            throw new ResourceNotFoundException("Refresh token is not valid");
        }
        token.revoke();
        return token.getUser();
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(RefreshToken::revoke);
    }

    @Transactional
    public int purgeExpired() {
        return refreshTokenRepository.deleteExpiredOrRevoked(Instant.now());
    }

    static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }
}
