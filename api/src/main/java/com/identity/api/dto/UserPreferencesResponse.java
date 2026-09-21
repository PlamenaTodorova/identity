package com.identity.api.dto;

import com.identity.core.domain.UserAppPreference;
import java.util.List;

public record UserPreferencesResponse(
        List<AppPreferenceResponse> apps
) {

    public static UserPreferencesResponse from(List<UserAppPreference> preferences) {
        return new UserPreferencesResponse(
                preferences.stream().map(AppPreferenceResponse::from).toList()
        );
    }
}
