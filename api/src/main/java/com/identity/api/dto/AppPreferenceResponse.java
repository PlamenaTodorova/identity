package com.identity.api.dto;

import com.identity.core.domain.UserAppPreference;

public record AppPreferenceResponse(
        String appId,
        boolean enabled
) {

    public static AppPreferenceResponse from(UserAppPreference preference) {
        return new AppPreferenceResponse(preference.getAppId(), preference.isEnabled());
    }
}
