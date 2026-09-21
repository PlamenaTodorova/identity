package com.identity.core.security;

public record AuthenticatedUser(
        Long id,
        String email
) {
}
