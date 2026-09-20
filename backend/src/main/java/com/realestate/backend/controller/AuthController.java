package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.*;
import com.realestate.backend.dto.response.AuthResponse;
import com.realestate.backend.dto.response.RefreshTokenResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/register/user")
    @Operation(summary = "Register a new user.")
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
    @Operation(summary = "Register a new agency owner (also agency itself).")
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
    @Operation(summary = "Login to account.")
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
    @Operation(summary = "Refresh the token.")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        RefreshTokenResponse response = authService.refreshToken(request, servletRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", response)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    @Operation(summary = "Log out from the account.")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(request);

        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully")
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    @Operation(summary = "Get the current user.")
    public ResponseEntity<ApiResponse<AuthResponse>> me(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        AuthResponse response = authService.currentUser(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Current user fetched successfully", response)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/change-password")
    @Operation(summary = "Change the password.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {

        authService.changePassword(request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Password successfully changed.", null)
        );
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send an otp to the email.")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("If the email has been registered, an OTP has been sent.", null)
        );
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Verify the otp and reset the password.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password has been changed successfully.", null)
        );
    }

    @PostMapping("/me/reactivate")
    @Operation(summary = "Re-activate the own account.")
    public ResponseEntity<ApiResponse<AuthResponse>> reactivateAccount(
            @Valid @RequestBody AccountReactivationRequest request,
            HttpServletRequest servletRequest
    ) {

        AuthResponse response = authService.reactivateAccount(request, servletRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Account successfully re-activated and logged in.", response)
        );
    }

    @PostMapping("/me/deactivate")
    @Operation(summary = "Deactivate the own account.")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @Valid @RequestBody AccountPasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        authService.deactivateAccount(request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Account successfully deactivated.", null)
        );
    }

}