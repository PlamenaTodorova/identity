package com.identity.core.service.model;

import com.identity.core.domain.User;

public record AuthTokens(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        User user
) {
}
