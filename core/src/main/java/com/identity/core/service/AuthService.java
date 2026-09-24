package com.identity.core.service;

import com.identity.core.domain.RefreshToken;
import com.identity.core.domain.User;
import com.identity.core.exception.DuplicateEntryException;
import com.identity.core.exception.InvalidCredentialsException;
import com.identity.core.exception.InvalidTokenException;
import com.identity.core.repository.RefreshTokenRepository;
import com.identity.core.repository.UserRepository;
import com.identity.core.service.model.AuthTokens;
import com.identity.core.service.model.RegisterUserCommand;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            TokenService tokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthTokens register(RegisterUserCommand command) {
        String email = normalizeEmail(command.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEntryException("Email is already registered");
        }

        User user = new User(
                email,
                passwordEncoder.encode(command.password()),
                command.displayName().trim()
        );
        user = userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthTokens login(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthTokens refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new InvalidTokenException("Refresh token expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private AuthTokens issueTokens(User user) {
        String accessToken = tokenService.createAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user.getId());
        refreshTokenRepository.save(refreshToken);

        return new AuthTokens(
                accessToken,
                refreshToken.getToken(),
                tokenService.accessTokenTtlSeconds(),
                user
        );
    }

    private RefreshToken createRefreshToken(Long userId) {
        Instant expiresAt = Instant.now().plusSeconds(tokenService.refreshTokenTtlSeconds());
        return new RefreshToken(UUID.randomUUID().toString(), userId, expiresAt);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
