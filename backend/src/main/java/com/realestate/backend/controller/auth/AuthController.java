package com.realestate.backend.controller.auth;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.agency.request.AgencyOwnerRegisterRequest;
import com.realestate.backend.dto.auth.request.*;
import com.realestate.backend.dto.auth.response.AuthResponse;
import com.realestate.backend.dto.auth.response.RefreshTokenResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.auth.AuthService;
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

    private final AuthService authService;

    @PostMapping("/register/user")
    public ResponseEntity<ApiResponse<AuthResponse>> registerClient(
            @Valid @RequestBody UserRegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        AuthResponse response = authService.registerClient(request, servletRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client registered successfully", response));
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
}