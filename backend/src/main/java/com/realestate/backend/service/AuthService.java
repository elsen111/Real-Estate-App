package com.realestate.backend.service;

import com.realestate.backend.dto.request.*;
import com.realestate.backend.dto.response.AuthResponse;
import com.realestate.backend.dto.response.RefreshTokenResponse;
import com.realestate.backend.security.CustomUserDetails;
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
            RefreshTokenRequest request,
            HttpServletRequest servletRequest
    );

    void logout(LogoutRequest request);

    AuthResponse currentUser(CustomUserDetails currentUser);

    void changePassword(
            ChangePasswordRequest request,
            CustomUserDetails currentUser
    );

}