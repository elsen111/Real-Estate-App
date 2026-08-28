package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.DeleteAccountRequest;
import com.realestate.backend.dto.request.UpdateProfileRequest;
import com.realestate.backend.dto.response.AuthUserResponse;
import com.realestate.backend.dto.response.UserProfilePhotoResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.ratelimit.RateLimitFilter;
import com.realestate.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RateLimitFilter rateLimitFilter;

    @InjectMocks
    private UserController controller;

    private final CustomUserDetails currentUser = mock(CustomUserDetails.class);

    private AuthUserResponse buildAuthUser(UUID id) {
        return AuthUserResponse.builder()
                .id(id)
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .phoneNumber("+994501112233")
                .enabled(true)
                .emailVerified(true)
                .roles(Set.of("CLIENT"))
                .build();
    }

    @Test
    void updateProfile_returnsCreated_withUpdatedProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Jane Updated");
        request.setEmail("jane.updated@example.com");
        request.setPhoneNumber("+994501112233");

        AuthUserResponse expected = buildAuthUser(UUID.randomUUID());

        when(userService.updateProfile(request, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<AuthUserResponse>> response =
                controller.updateProfile(request, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("User profile successfully updated");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(userService).updateProfile(request, currentUser);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void updateProfile_propagatesException_whenEmailAlreadyTaken() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("taken@example.com");

        when(userService.updateProfile(request, currentUser))
                .thenThrow(new RuntimeException("Email is already in use"));

        try {
            controller.updateProfile(request, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Email is already in use");
        }

        verify(userService).updateProfile(request, currentUser);
    }

    @Test
    void deleteAccount_returnsOk_withNoData() {
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword("currentPassword123");

        ResponseEntity<ApiResponse<Void>> response =
                controller.deleteAccount(request, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Account successfully deleted");
        assertThat(response.getBody().getData()).isNull();

        verify(userService).deleteAccount(request, currentUser);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void deleteAccount_propagatesException_whenPasswordIncorrect() {
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword("wrongPassword");

        org.mockito.Mockito.doThrow(new RuntimeException("Incorrect password"))
                .when(userService).deleteAccount(request, currentUser);

        try {
            controller.deleteAccount(request, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Incorrect password");
        }

        verify(userService).deleteAccount(request, currentUser);
    }

    @Test
    void uploadProfilePhoto_returnsOk_withPhotoUrl() {
        MultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "avatar-bytes".getBytes());

        UserProfilePhotoResponse expected = UserProfilePhotoResponse.builder()
                .photoUrl("https://cdn.example.com/avatars/avatar.png")
                .build();

        when(userService.uploadProfilePhoto(file, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<UserProfilePhotoResponse>> response =
                controller.uploadProfilePhoto(file, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Profile photo uploaded successfully");
        assertThat(response.getBody().getData().photoUrl()).isEqualTo("https://cdn.example.com/avatars/avatar.png");

        verify(userService).uploadProfilePhoto(file, currentUser);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void removeProfilePhoto_returnsOk_withNoData() {
        ResponseEntity<ApiResponse<Void>> response =
                controller.removeProfilePhoto(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Profile photo removed successfully");
        assertThat(response.getBody().getData()).isNull();

        verify(userService).removeProfilePhoto(currentUser);
        verifyNoMoreInteractions(userService);
    }
}