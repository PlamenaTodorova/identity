package com.identity.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "identity")
public record IdentityProperties(
        Jwt jwt,
        Apps apps
) {

    public record Jwt(
            String secret,
            String issuer,
            long accessTokenTtlSeconds,
            long refreshTokenTtlSeconds
    ) {
    }

    public record Apps(
            java.util.List<String> known
    ) {
    }
}
