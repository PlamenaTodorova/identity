package com.identity.core.service;

import com.identity.core.exception.ResourceNotFoundException;
import com.identity.core.repository.AppRepository;
import com.identity.core.repository.UserAppPreferenceRepository;
import com.identity.core.repository.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppRepository appRepository;

    @Mock
    private UserAppPreferenceRepository preferenceRepository;

    @Test
    void updateRejectsUnknownApp() {
        when(userRepository.existsById(42L)).thenReturn(true);
        when(appRepository.existsById("unknown")).thenReturn(false);

        UserPreferenceService service = new UserPreferenceService(
                userRepository,
                appRepository,
                preferenceRepository
        );

        assertThatThrownBy(() -> service.updatePreferences(42L, Map.of("unknown", true)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Unknown app: unknown");
        verify(preferenceRepository, never()).save(any());
    }
}