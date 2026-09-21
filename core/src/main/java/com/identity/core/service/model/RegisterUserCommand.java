package com.identity.core.service.model;

public record RegisterUserCommand(
        String email,
        String password,
        String displayName
) {
}
