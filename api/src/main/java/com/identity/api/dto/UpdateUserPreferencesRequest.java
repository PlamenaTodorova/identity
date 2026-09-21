package com.identity.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateUserPreferencesRequest(
        @NotEmpty @Valid List<AppPreferenceUpdate> apps
) {

    public record AppPreferenceUpdate(
            @jakarta.validation.constraints.NotBlank String appId,
            boolean enabled
    ) {
    }
}
