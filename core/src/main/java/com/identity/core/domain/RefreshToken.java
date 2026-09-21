package com.identity.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @Column(length = 64)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    public RefreshToken(String token, Long userId, Instant expiresAt) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
}
