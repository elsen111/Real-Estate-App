package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.*;
import com.realestate.backend.dto.response.AuthResponse;
import com.realestate.backend.dto.response.RefreshTokenResponse;
import com.realestate.backend.security.core.AuthSessionResponder;
import com.realestate.backend.security.core.RefreshTokenCookieService;
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookie;
    private final AuthSessionResponder responder;

    @PostMapping("/register/user")
    @Operation(summary = "Register a new user.")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(
            @Valid @RequestBody UserRegisterRequest request,
            @RequestParam(name = "registrationType", defaultValue = "buyer") String type,
            HttpServletRequest servletRequest
    ) {
        return responder.authenticated(
                HttpStatus.CREATED,
                "User registered successfully as a " + type,
                authService.registerUser(request, type, servletRequest)
        );
    }

    @PostMapping("/register/agency-owner")
    @Operation(summary = "Register a new agency owner (also agency itself).")
    public ResponseEntity<ApiResponse<AuthResponse>> registerAgencyOwner(
            @Valid @RequestBody AgencyOwnerRegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        return responder.authenticated(
                HttpStatus.CREATED,
                "Agency owner registered successfully",
                authService.registerAgencyOwner(request, servletRequest)
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Login to account.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        return responder.authenticated(
                HttpStatus.OK,
                "Logged in successfully",
                authService.login(request, servletRequest)
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh the token (cookie, or X-Refresh-Token header).")
    @Parameter(in = ParameterIn.HEADER, name = RefreshTokenCookieService.HEADER_NAME, required = false)
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            HttpServletRequest servletRequest
    ) {
        RefreshTokenResponse tokens = authService.refreshToken(
                refreshTokenCookie.resolve(servletRequest),
                servletRequest
        );

        return responder.refreshed("Token refreshed successfully", tokens);
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out and clear the refresh-token cookie.")
    @Parameter(in = ParameterIn.HEADER, name = RefreshTokenCookieService.HEADER_NAME, required = false)
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest servletRequest
    ) {
        authService.logout(refreshTokenCookie.resolve(servletRequest));

        return responder.loggedOut("Logged out successfully");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    @Operation(summary = "Get the current user.")
    public ResponseEntity<ApiResponse<AuthResponse>> me(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Current user fetched successfully", authService.currentUser(currentUser))
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

        return ResponseEntity.ok(ApiResponse.success("Password successfully changed.", null));
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

        return ResponseEntity.ok(ApiResponse.success("Password has been changed successfully.", null));
    }

    @PostMapping("/me/reactivate")
    @Operation(summary = "Re-activate the own account.")
    public ResponseEntity<ApiResponse<AuthResponse>> reactivateAccount(
            @Valid @RequestBody AccountReactivationRequest request,
            HttpServletRequest servletRequest
    ) {
        return responder.authenticated(
                HttpStatus.OK,
                "Account successfully re-activated and logged in.",
                authService.reactivateAccount(request, servletRequest)
        );
    }

    @PostMapping("/me/deactivate")
    @Operation(summary = "Deactivate the own account.")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @Valid @RequestBody AccountPasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        authService.deactivateAccount(request, currentUser);

        return ResponseEntity.ok(ApiResponse.success("Account successfully deactivated.", null));
    }
}