package com.identity.core.service;

import com.identity.core.domain.App;
import com.identity.core.domain.UserAppPreference;
import com.identity.core.exception.ResourceNotFoundException;
import com.identity.core.repository.AppRepository;
import com.identity.core.repository.UserAppPreferenceRepository;
import com.identity.core.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

    @Test
    void enablingAppCreatesPreference() {
        when(appRepository.findAllByOrderByAppIdAsc()).thenReturn(List.of(
                new App("bingable", "Bingable", "http://localhost:3001")
        ));
        when(userRepository.existsById(42L)).thenReturn(true);
        when(appRepository.existsById("bingable")).thenReturn(true);
        when(preferenceRepository.findByUserIdAndAppId(42L, "bingable"))
                .thenReturn(Optional.empty());

        UserPreferenceService service = new UserPreferenceService(
                userRepository,
                appRepository,
                preferenceRepository
        );

        service.updatePreferences(42L, Map.of("bingable", true));

        verify(preferenceRepository).save(argThat(preference ->
                preference.getUserId().equals(42L)
                        && preference.getAppId().equals("bingable")
                        && preference.isEnabled()));
    }

    @Test
    void disablingAppRemovesPreference() {
        UserAppPreference preference = new UserAppPreference(42L, "bingable", true);
        when(userRepository.existsById(42L)).thenReturn(true);
        when(appRepository.existsById("bingable")).thenReturn(true);
        when(preferenceRepository.findByUserIdAndAppId(42L, "bingable"))
                .thenReturn(Optional.of(preference));
        when(appRepository.findAllByOrderByAppIdAsc()).thenReturn(List.of(
                new App("bingable", "Bingable", "http://localhost:3001")
        ));

        UserPreferenceService service = new UserPreferenceService(
                userRepository,
                appRepository,
                preferenceRepository
        );

        service.updatePreferences(42L, Map.of("bingable", false));

        verify(preferenceRepository).delete(preference);
    }
}