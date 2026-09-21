package com.identity.api.controller;

import com.identity.api.dto.UpdateUserPreferencesRequest;
import com.identity.api.dto.UserPreferencesResponse;
import com.identity.api.dto.UserResponse;
import com.identity.api.security.SecurityUtils;
import com.identity.core.security.AuthenticatedUser;
import com.identity.core.service.UserPreferenceService;
import com.identity.core.service.UserService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserPreferenceService userPreferenceService;

    public UserController(UserService userService, UserPreferenceService userPreferenceService) {
        this.userService = userService;
        this.userPreferenceService = userPreferenceService;
    }

    @GetMapping("/me")
    public UserResponse me() {
        AuthenticatedUser currentUser = SecurityUtils.currentUser();
        return UserResponse.from(userService.getUser(currentUser.id()));
    }

    @GetMapping("/me/preferences")
    public UserPreferencesResponse preferences() {
        AuthenticatedUser currentUser = SecurityUtils.currentUser();
        return UserPreferencesResponse.from(userPreferenceService.getPreferences(currentUser.id()));
    }

    @PutMapping("/me/preferences")
    public UserPreferencesResponse updatePreferences(@Valid @RequestBody UpdateUserPreferencesRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.currentUser();
        Map<String, Boolean> updates = new LinkedHashMap<>();
        for (UpdateUserPreferencesRequest.AppPreferenceUpdate update : request.apps()) {
            updates.put(update.appId(), update.enabled());
        }
        return UserPreferencesResponse.from(userPreferenceService.updatePreferences(currentUser.id(), updates));
    }
}
