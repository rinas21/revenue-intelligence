package com.rinas.revenue.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Issues and verifies HS256 access tokens.
 *
 * <p>The token carries the user id, business id and role as claims. The server
 * still loads the user for authorization decisions that need current state, but
 * the business id on the token is what scopes every request — a caller cannot
 * choose it.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final SecurityProperties properties;

    public JwtService(SecurityProperties properties) {
        this.properties = properties;
        byte[] secret = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException(
                "security.jwt.secret must be at least 32 bytes for HS256; configure a stronger secret");
        }
        this.key = Keys.hmacShaKeyFor(secret);
    }

    public String generateAccessToken(AppUserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(principal.getUsername())
            .claim("uid", principal.getUserId().toString())
            .claim("bid", principal.getBusinessId().toString())
            .claim("role", principal.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(properties.getAccessTokenTtl())))
            .signWith(key)
            .compact();
    }

    /** Returns the subject username if the token is valid, otherwise empty. */
    public Optional<String> extractUsername(String token) {
        try {
            return Optional.of(parse(token).getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public long getAccessTokenTtlSeconds() {
        return properties.getAccessTokenTtl().toSeconds();
    }

    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.get("uid", String.class));
    }

    public UUID getBusinessId(Claims claims) {
        return UUID.fromString(claims.get("bid", String.class));
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
