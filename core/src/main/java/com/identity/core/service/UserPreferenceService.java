package com.identity.core.service;

import com.identity.core.config.IdentityProperties;
import com.identity.core.domain.UserAppPreference;
import com.identity.core.exception.ResourceNotFoundException;
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
    private final UserAppPreferenceRepository preferenceRepository;
    private final List<String> knownApps;

    public UserPreferenceService(
            UserRepository userRepository,
            UserAppPreferenceRepository preferenceRepository,
            IdentityProperties properties
    ) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.knownApps = List.copyOf(properties.apps().known());
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
            if (!knownApps.contains(appId)) {
                throw new ResourceNotFoundException("Unknown app: " + appId);
            }

            UserAppPreference preference = preferenceRepository
                    .findByUserIdAndAppId(userId, appId)
                    .orElseGet(() -> new UserAppPreference(userId, appId, true));
            preference.setEnabled(entry.getValue());
            preferenceRepository.save(preference);
        }

        return normalizePreferences(userId);
    }

    @Transactional
    public void createDefaultPreferences(Long userId) {
        List<UserAppPreference> defaults = knownApps.stream()
                .map(appId -> new UserAppPreference(userId, appId, true))
                .toList();
        preferenceRepository.saveAll(defaults);
    }

    private List<UserAppPreference> normalizePreferences(Long userId) {
        List<UserAppPreference> existing = preferenceRepository.findByUserIdOrderByAppIdAsc(userId);
        Map<String, UserAppPreference> byAppId = new LinkedHashMap<>();
        for (UserAppPreference preference : existing) {
            byAppId.put(preference.getAppId(), preference);
        }

        List<UserAppPreference> normalized = new ArrayList<>();
        for (String appId : knownApps) {
            normalized.add(byAppId.getOrDefault(appId, new UserAppPreference(userId, appId, true)));
        }
        return normalized;
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User " + userId + " not found");
        }
    }
}
