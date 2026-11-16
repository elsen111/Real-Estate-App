package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.*;
import com.realestate.backend.dto.response.AuthResponse;
import com.realestate.backend.dto.response.RefreshTokenResponse;
import com.realestate.backend.dto.response.VerifyResetOtpResponse;
import com.realestate.backend.security.core.AuthSessionResponder;
import com.realestate.backend.security.core.RefreshTokenCookieService;
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.AuthService;
import com.realestate.backend.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Authentication",
        description = """
                APIs for user registration, authentication, session management, token
                refreshing, password management, password recovery, and account lifecycle
                operations such as account reactivation and deactivation.
                """
)
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenCookieService refreshTokenCookie;
    private final AuthSessionResponder responder;

    @PostMapping("/register/user")
    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new user account using the provided registration details.
                    Supports different registration types through the registrationType query parameter.
                    Returns authentication credentials and establishes an authenticated session
                    upon successful registration.
                    """
    )
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
    @Operation(
            summary = "Authenticate a user",
            description = """
                    Authenticates a user using their email and password.
                    Returns an access token and establishes a refresh-token session
                    upon successful authentication.
                    Account status and agency approval requirements are validated during authentication.
                    """
    )
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
    @Operation(
            summary = "Authenticate a user",
            description = """
                    Authenticates a user using their email and password.
                    Returns an access token and establishes a refresh-token session
                    upon successful authentication.
                    Account status and agency approval requirements are validated during authentication.
                    """
    )
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
    @Operation(
            summary = "Refresh authentication tokens",
            description = """
                    Generates a new access token and refresh token using the existing refresh token.
                    The refresh token can be provided through the HttpOnly refresh-token cookie
                    or the X-Refresh-Token request header.
                    The previous refresh token is invalidated as part of token rotation.
                    """
    ) @Parameter(in = ParameterIn.HEADER, name = RefreshTokenCookieService.HEADER_NAME, required = false)
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
    @Parameter(in = ParameterIn.HEADER, name = RefreshTokenCookieService.HEADER_NAME, required = false)
    @Operation(
            summary = "Log out the current session",
            description = """
                    Invalidates the current refresh-token session and clears the refresh-token cookie.
                    The refresh token can be provided through the HttpOnly cookie
                    or the X-Refresh-Token request header.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest servletRequest
    ) {
        authService.logout(refreshTokenCookie.resolve(servletRequest));

        return responder.loggedOut("Logged out successfully");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    @Operation(
            summary = "Get the authenticated user's profile",
            description = """
                    Retrieves the profile and account information of the currently authenticated user.
                    Requires a valid access token.
                    """
    )
    public ResponseEntity<ApiResponse<AuthResponse>> me(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Current user fetched successfully", authService.currentUser(currentUser))
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/change-password")
    @Operation(
            summary = "Change the authenticated user's password",
            description = """
                    Changes the password of the currently authenticated user.
                    Requires the current password and a valid new password.
                    Requires authentication.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(request, currentUser);

        return ResponseEntity.ok(ApiResponse.success("Password successfully changed.", null));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password recovery",
            description = """
                    Initiates the password recovery process by sending a one-time password (OTP)
                    to the provided email address.
                    Returns a generic response regardless of whether an account exists
                    to prevent user enumeration.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        passwordResetService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "If an account exists with this email, password reset instructions will be sent."
                )
        );
    }

    @PostMapping("/verify-reset-otp")
    @Operation(
            summary = "Verify password reset OTP",
            description = """
                    Validates the OTP submitted during the password recovery process.
                    Upon successful verification, returns a short-lived password reset credential
                    that can be used to authorize the password reset operation.
                    """
    )
    public ResponseEntity<ApiResponse<VerifyResetOtpResponse>> verifyResetOtp(
            @Valid @RequestBody VerifyResetOtpRequest request
    ) {

        VerifyResetOtpResponse response =
                passwordResetService.verifyResetOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success("Reset otp verified successfully.", response)
        );
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset account password",
            description = """
                    Resets the account password using the password reset credential
                    obtained after successful OTP verification.
                    Requires a valid, unexpired reset credential and a new password.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        passwordResetService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password has been reset successfully.")
        );
    }

    @PostMapping("/me/reactivate")
    @Operation(
            summary = "Reactivate the user's account",
            description = """
                    Reactivates a previously deactivated account using the required account
                    verification credentials.
                    Upon successful reactivation, establishes a new authenticated session
                    and returns authentication credentials.
                    """
    )
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
    @Operation(
            summary = "Deactivate the authenticated user's account",
            description = """
                    Deactivates the account of the currently authenticated user after
                    validating the provided password.
                    Requires a valid access token and account password.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @Valid @RequestBody AccountPasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        authService.deactivateAccount(request, currentUser);

        return ResponseEntity.ok(ApiResponse.success("Account successfully deactivated.", null));
    }
}