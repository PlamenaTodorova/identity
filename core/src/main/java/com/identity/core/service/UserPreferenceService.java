package com.identity.core.service;

import com.identity.core.domain.UserAppPreference;
import com.identity.core.exception.ResourceNotFoundException;
import com.identity.core.repository.AppRepository;
import com.identity.core.repository.UserAppPreferenceRepository;
import com.identity.core.repository.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final AppRepository appRepository;
    private final UserAppPreferenceRepository preferenceRepository;

    public UserPreferenceService(
            UserRepository userRepository,
            AppRepository appRepository,
            UserAppPreferenceRepository preferenceRepository
    ) {
        this.userRepository = userRepository;
        this.appRepository = appRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @Transactional(readOnly = true)
    public List<UserAppPreference> getPreferences(Long userId) {
        ensureUserExists(userId);
        return normalizePreferences(userId);
    }

    @Transactional
    public List<UserAppPreference> updatePreferences(Long userId, Map<String, Boolean> updates) {
        ensureUserExists(userId);

        for (Map.Entry<String, Boolean> entry : updates.entrySet()) {
            String appId = entry.getKey();
            if (!appRepository.existsById(appId)) {
                throw new ResourceNotFoundException("Unknown app: " + appId);
            }

                if (Boolean.TRUE.equals(entry.getValue())) {
                UserAppPreference preference = preferenceRepository
                    .findByUserIdAndAppId(userId, appId)
                    .orElseGet(() -> new UserAppPreference(userId, appId, true));
                preference.setEnabled(true);
                preferenceRepository.save(preference);
                } else {
                preferenceRepository.findByUserIdAndAppId(userId, appId)
                    .ifPresent(preferenceRepository::delete);
                }
        }

        return normalizePreferences(userId);
    }

    private List<UserAppPreference> normalizePreferences(Long userId) {
        List<UserAppPreference> existing = preferenceRepository.findByUserIdOrderByAppIdAsc(userId);
        Map<String, UserAppPreference> byAppId = new LinkedHashMap<>();
        for (UserAppPreference preference : existing) {
            byAppId.put(preference.getAppId(), preference);
        }

        List<UserAppPreference> normalized = new ArrayList<>();
        for (String appId : appRepository.findAllByOrderByAppIdAsc().stream().map(app -> app.getAppId()).toList()) {
            normalized.add(byAppId.getOrDefault(appId, new UserAppPreference(userId, appId, false)));
        }
        return normalized;
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User " + userId + " not found");
        }
    }
}
