package com.identity.core.service;

import com.identity.core.config.IdentityProperties;
import com.identity.core.domain.User;
import com.identity.core.exception.DuplicateEntryException;
import com.identity.core.exception.InvalidCredentialsException;
import com.identity.core.repository.RefreshTokenRepository;
import com.identity.core.repository.UserRepository;
import com.identity.core.service.model.RegisterUserCommand;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserPreferenceService userPreferenceService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        IdentityProperties properties = new IdentityProperties(
                new IdentityProperties.Jwt(
                        "dev-only-change-me-use-at-least-32-characters-long-secret",
                        "adjutant-identity",
                        900,
                        604800
                ),
                new IdentityProperties.Apps(List.of("bingable", "glutton"))
        );
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                userPreferenceService,
                new TokenService(properties),
                new BCryptPasswordEncoder()
        );
    }

    @Test
    void registerCreatesUserAndDefaultPreferences() {
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42L);
            return user;
        });

        var tokens = authService.register(new RegisterUserCommand(
                "alice@example.com",
                "password123",
                "Alice"
        ));

        assertThat(tokens.user().getId()).isEqualTo(42L);
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("alice@example.com");
        verify(userPreferenceService).createDefaultPreferences(42L);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterUserCommand(
                "alice@example.com",
                "password123",
                "Alice"
        ))).isInstanceOf(DuplicateEntryException.class);
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = new User("alice@example.com", new BCryptPasswordEncoder().encode("password123"), "Alice");
        user.setId(1L);
        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(java.util.Optional.of(user));

        assertThatThrownBy(() -> authService.login("alice@example.com", "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
