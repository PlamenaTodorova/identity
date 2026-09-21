package com.identity.api.dto;

import com.identity.core.service.model.AuthTokens;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String tokenType,
        UserResponse user
) {

    public static AuthResponse from(AuthTokens tokens) {
        return new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresInSeconds(),
                "Bearer",
                UserResponse.from(tokens.user())
        );
    }
}
