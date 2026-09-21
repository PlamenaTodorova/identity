package com.identity.core.service;

import com.identity.core.config.IdentityProperties;
import com.identity.core.domain.User;
import com.identity.core.exception.InvalidTokenException;
import com.identity.core.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final IdentityProperties properties;
    private final SecretKey secretKey;

    public TokenService(IdentityProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.jwt().accessTokenTtlSeconds());

        return Jwts.builder()
                .issuer(properties.jwt().issuer())
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public AuthenticatedUser parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(properties.jwt().issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);
            return new AuthenticatedUser(userId, email);
        } catch (RuntimeException exception) {
            throw new InvalidTokenException("Invalid or expired access token");
        }
    }

    public long accessTokenTtlSeconds() {
        return properties.jwt().accessTokenTtlSeconds();
    }

    public long refreshTokenTtlSeconds() {
        return properties.jwt().refreshTokenTtlSeconds();
    }
}
