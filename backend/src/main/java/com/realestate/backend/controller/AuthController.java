package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.*;
import com.realestate.backend.dto.response.AuthResponse;
import com.realestate.backend.dto.response.RefreshTokenResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/register/user")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(
            @Valid @RequestBody UserRegisterRequest request,
            @RequestParam(required = false, name = "registrationType", defaultValue = "buyer") String type,
            HttpServletRequest servletRequest
    ) {
        AuthResponse response = authService.registerUser(request, type, servletRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully as a " + type, response));
    }

    @PostMapping("/register/agency-owner")
    public ResponseEntity<ApiResponse<AuthResponse>> registerAgencyOwner(
            @Valid @RequestBody AgencyOwnerRegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        AuthResponse response = authService.registerAgencyOwner(request, servletRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Agency owner registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        AuthResponse response = authService.login(request, servletRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Logged in successfully", response)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        RefreshTokenResponse response = authService.refreshToken(request, servletRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", response)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(request);

        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> me(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        AuthResponse response = authService.currentUser(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Current user fetched successfully", response)
        );
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {

        authService.changePassword(request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Password successfully changed.", null)
        );
    }

}