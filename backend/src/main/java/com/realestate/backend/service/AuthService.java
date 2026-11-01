package com.realestate.backend.service;

import com.realestate.backend.dto.request.*;
import com.realestate.backend.dto.response.AuthResponse;
import com.realestate.backend.dto.response.RefreshTokenResponse;
import com.realestate.backend.security.principal.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse registerUser(
            UserRegisterRequest request,
            String type,
            HttpServletRequest servletRequest
    );

    AuthResponse registerAgencyOwner(
            AgencyOwnerRegisterRequest request,
            HttpServletRequest servletRequest
    );

    AuthResponse login(
            LoginRequest request,
            HttpServletRequest servletRequest
    );

    RefreshTokenResponse refreshToken(
            String request,
            HttpServletRequest servletRequest
    );

    void logout(String request);

    AuthResponse currentUser(CustomUserDetails currentUser);

    void changePassword(
            ChangePasswordRequest request,
            CustomUserDetails currentUser
    );

    void forgotPassword(
            ForgotPasswordRequest request
    );

    void resetPassword(
            ResetPasswordRequest request
    );

    AuthResponse reactivateAccount(
            AccountReactivationRequest request,
            HttpServletRequest servletRequest
    );

    void deactivateAccount(
            AccountPasswordRequest request,
            CustomUserDetails currentUser
    );

}