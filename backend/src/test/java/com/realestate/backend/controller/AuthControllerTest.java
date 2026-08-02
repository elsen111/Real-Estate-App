package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AgencyOwnerRegisterRequest;
import com.realestate.backend.dto.request.ChangePasswordRequest;
import com.realestate.backend.dto.request.ForgotPasswordRequest;
import com.realestate.backend.dto.request.LoginRequest;
import com.realestate.backend.dto.request.LogoutRequest;
import com.realestate.backend.dto.request.RefreshTokenRequest;
import com.realestate.backend.dto.request.ResetPasswordRequest;
import com.realestate.backend.dto.request.UserRegisterRequest;
import com.realestate.backend.dto.response.AuthResponse;
import com.realestate.backend.dto.response.RefreshTokenResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthServiceImpl authService;

    @InjectMocks
    private AuthController controller;

    private final CustomUserDetails currentUser = mock(CustomUserDetails.class);
    private final HttpServletRequest servletRequest = new MockHttpServletRequest();

    private AuthResponse buildAuthResponse() {
        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresInSeconds(3600L)
                .build();
    }

    @Test
    void registerUser_returnsCreated_withDefaultBuyerType() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane.doe@example.com");
        request.setPassword("password123");

        AuthResponse expected = buildAuthResponse();

        when(authService.registerUser(request, "buyer", servletRequest)).thenReturn(expected);

        ResponseEntity<ApiResponse<AuthResponse>> response =
                controller.registerUser(request, "buyer", servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("User registered successfully as a buyer");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(authService).registerUser(request, "buyer", servletRequest);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void registerUser_returnsCreated_withCustomRegistrationType() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFullName("John Smith");
        request.setEmail("john.smith@example.com");
        request.setPassword("password123");

        AuthResponse expected = buildAuthResponse();

        when(authService.registerUser(request, "landlord", servletRequest)).thenReturn(expected);

        ResponseEntity<ApiResponse<AuthResponse>> response =
                controller.registerUser(request, "landlord", servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getMessage()).isEqualTo("User registered successfully as a landlord");

        verify(authService).registerUser(request, "landlord", servletRequest);
    }

    @Test
    void registerAgencyOwner_returnsCreated_withAgencyOwner() {
        AgencyOwnerRegisterRequest request = new AgencyOwnerRegisterRequest();
        request.setFullName("Owner Name");
        request.setEmail("owner@example.com");
        request.setPassword("password123");
        request.setAgencyName("Prime Realty");
        request.setBusinessPhone("+994501112233");
        request.setCity("Baku");
        request.setAddress("Nizami St. 10");

        AuthResponse expected = buildAuthResponse();

        when(authService.registerAgencyOwner(request, servletRequest)).thenReturn(expected);

        ResponseEntity<ApiResponse<AuthResponse>> response =
                controller.registerAgencyOwner(request, servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Agency owner registered successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(authService).registerAgencyOwner(request, servletRequest);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void login_returnsOk_withAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jane.doe@example.com");
        request.setPassword("password123");

        AuthResponse expected = buildAuthResponse();

        when(authService.login(request, servletRequest)).thenReturn(expected);

        ResponseEntity<ApiResponse<AuthResponse>> response =
                controller.login(request, servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Logged in successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(authService).login(request, servletRequest);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void refreshToken_returnsOk_withNewTokens() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh-token");

        RefreshTokenResponse expected = RefreshTokenResponse.builder()
                .tokenType("Bearer")
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .expiresInSeconds(3600L)
                .build();

        when(authService.refreshToken(request, servletRequest)).thenReturn(expected);

        ResponseEntity<ApiResponse<RefreshTokenResponse>> response =
                controller.refreshToken(request, servletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Token refreshed successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(authService).refreshToken(request, servletRequest);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void logout_returnsOk_withNoData() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        ResponseEntity<ApiResponse<Void>> response = controller.logout(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Logged out successfully");
        assertThat(response.getBody().getData()).isNull();

        verify(authService).logout(request);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void me_returnsOk_withCurrentUser() {
        AuthResponse expected = buildAuthResponse();

        when(authService.currentUser(currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<AuthResponse>> response = controller.me(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Current user fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(authService).currentUser(currentUser);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void changePassword_returnsOk_withNoData() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword("newPassword123");
        request.setConfirmNewPassword("newPassword123");

        ResponseEntity<ApiResponse<Void>> response =
                controller.changePassword(currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Password successfully changed.");
        assertThat(response.getBody().getData()).isNull();

        verify(authService).changePassword(request, currentUser);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void changePassword_propagatesException_whenServiceThrows() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123");
        request.setConfirmNewPassword("newPassword123");

        org.mockito.Mockito.doThrow(new RuntimeException("Current password is incorrect"))
                .when(authService).changePassword(request, currentUser);

        try {
            controller.changePassword(currentUser, request);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Current password is incorrect");
        }

        verify(authService).changePassword(request, currentUser);
    }

    @Test
    void forgotPassword_returnsOk_withNoData() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("jane.doe@example.com");

        ResponseEntity<ApiResponse<Void>> response = controller.forgotPassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("If the email has been registered, an OTP has been sent.");
        assertThat(response.getBody().getData()).isNull();

        verify(authService).forgotPassword(request);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void resetPassword_returnsOk_withNoData() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("jane.doe@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("newPassword123");

        ResponseEntity<ApiResponse<Void>> response = controller.resetPassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Password has been changed successfully.");
        assertThat(response.getBody().getData()).isNull();

        verify(authService).resetPassword(request);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void resetPassword_propagatesException_whenOtpInvalid() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("jane.doe@example.com");
        request.setOtp("000000");
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("newPassword123");

        org.mockito.Mockito.doThrow(new RuntimeException("Invalid or expired OTP"))
                .when(authService).resetPassword(request);

        try {
            controller.resetPassword(request);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid or expired OTP");
        }

        verify(authService).resetPassword(request);
    }
}