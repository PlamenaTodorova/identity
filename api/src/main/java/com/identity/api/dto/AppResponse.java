package com.identity.api.dto;

import com.identity.core.domain.App;

public record AppResponse(
        String appId,
        String name,
        String baseUrl
) {

    public static AppResponse from(App app) {
        return new AppResponse(app.getAppId(), app.getName(), app.getBaseUrl());
    }
}