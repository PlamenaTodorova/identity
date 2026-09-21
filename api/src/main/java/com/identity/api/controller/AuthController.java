package com.identity.api.controller;

import com.identity.api.dto.AuthResponse;
import com.identity.api.dto.LoginRequest;
import com.identity.api.dto.RefreshTokenRequest;
import com.identity.api.dto.RegisterRequest;
import com.identity.core.service.AuthService;
import com.identity.core.service.model.RegisterUserCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return AuthResponse.from(authService.register(new RegisterUserCommand(
                request.email(),
                request.password(),
                request.displayName()
        )));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(authService.login(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return AuthResponse.from(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
    }
}
