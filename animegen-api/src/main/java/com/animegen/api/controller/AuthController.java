package com.animegen.api.controller;

import com.animegen.common.ApiResponse;
import com.animegen.service.AuthService;
import com.animegen.service.auth.GuestTokenRequest;
import com.animegen.service.auth.GuestTokenResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/guest")
    public ApiResponse<GuestTokenResponse> guestToken(@Valid @RequestBody GuestTokenRequest request) {
        return ApiResponse.ok(authService.issueGuestToken(request.getDeviceId()));
    }
}
