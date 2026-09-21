package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminUserFilterRequest;
import com.realestate.backend.dto.request.UserStatusRequest;
import com.realestate.backend.dto.response.UserResponse;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private CustomUserDetails currentUser;

    @InjectMocks
    private AdminUserController controller;

    private UserResponse buildUser(UUID id) {
        return UserResponse.builder()
                .id(id)
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .phoneNumber("+994501234567")
                .roles(Set.of(Role.CLIENT.name()))
                .enabled(true)
                .emailVerified(true)
                .build();
    }

    @Test
    void getAllUsers_returnsOk_withPagedUsers() {

        AdminUserFilterRequest filter = new AdminUserFilterRequest();
        filter.setRole(Role.CLIENT);
        filter.setEnabled(true);

        Pageable pageable = Pageable.ofSize(10);

        Page<UserResponse> page =
                new PageImpl<>(List.of(buildUser(UUID.randomUUID())));

        when(adminUserService.getAllUsers(filter, pageable))
                .thenReturn(page);

        ResponseEntity<ApiResponse<Page<UserResponse>>> response =
                controller.getAllUsers(filter, pageable);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().isSuccess())
                .isTrue();

        assertThat(response.getBody().getMessage())
                .isEqualTo("Users fetched successfully");

        assertThat(response.getBody().getData())
                .isEqualTo(page);

        assertThat(response.getBody().getData().getContent())
                .hasSize(1);

        verify(adminUserService)
                .getAllUsers(filter, pageable);

        verifyNoMoreInteractions(adminUserService);
    }

    @Test
    void getAllUsers_returnsOk_withEmptyPage_whenNoUsersMatchFilter() {

        AdminUserFilterRequest filter = new AdminUserFilterRequest();
        Pageable pageable = Pageable.ofSize(10);

        Page<UserResponse> emptyPage =
                new PageImpl<>(List.of());

        when(adminUserService.getAllUsers(any(), any()))
                .thenReturn(emptyPage);

        ResponseEntity<ApiResponse<Page<UserResponse>>> response =
                controller.getAllUsers(filter, pageable);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().isSuccess())
                .isTrue();

        assertThat(response.getBody().getData())
                .isNotNull();

        assertThat(response.getBody().getData().getContent())
                .isEmpty();

        verify(adminUserService)
                .getAllUsers(filter, pageable);

        verifyNoMoreInteractions(adminUserService);
    }

    @Test
    void getUserById_returnsOk_withUser() {

        UUID userId = UUID.randomUUID();

        UserResponse expected = buildUser(userId);

        when(adminUserService.getUserById(userId))
                .thenReturn(expected);

        ResponseEntity<ApiResponse<UserResponse>> response =
                controller.getUserById(userId);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().isSuccess())
                .isTrue();

        assertThat(response.getBody().getMessage())
                .isEqualTo("User fetched successfully");

        assertThat(response.getBody().getData())
                .isEqualTo(expected);

        verify(adminUserService)
                .getUserById(userId);

        verifyNoMoreInteractions(adminUserService);
    }

    @Test
    void changeUserStatus_returnsOk_withServiceMessage() {

        UUID userId = UUID.randomUUID();

        UserStatusRequest request =
                new UserStatusRequest(true);

        String message =
                "User enabled successfully";

        when(adminUserService.changeUserStatus(userId, request))
                .thenReturn(message);

        ResponseEntity<ApiResponse<Void>> response =
                controller.changeUserStatus(userId, request);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().isSuccess())
                .isTrue();

        assertThat(response.getBody().getMessage())
                .isEqualTo(message);

        assertThat(response.getBody().getData())
                .isNull();

        verify(adminUserService)
                .changeUserStatus(userId, request);

        verifyNoMoreInteractions(adminUserService);
    }

    @Test
    void assignAdmin_returnsOk_withServiceMessage() {

        UUID userId = UUID.randomUUID();

        String message =
                "User assigned as Admin successfully";

        when(adminUserService.assignAdminRoleToUser(userId))
                .thenReturn(message);

        ResponseEntity<ApiResponse<Void>> response =
                controller.assignAdmin(userId);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().isSuccess())
                .isTrue();

        assertThat(response.getBody().getMessage())
                .isEqualTo(message);

        assertThat(response.getBody().getData())
                .isNull();

        verify(adminUserService)
                .assignAdminRoleToUser(userId);

        verifyNoMoreInteractions(adminUserService);
    }

    @Test
    void assignAdmin_propagatesException_whenServiceThrows() {

        UUID userId = UUID.randomUUID();

        when(adminUserService.assignAdminRoleToUser(userId))
                .thenThrow(new RuntimeException("User not found"));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> controller.assignAdmin(userId)
                );

        assertThat(exception.getMessage())
                .isEqualTo("User not found");

        verify(adminUserService)
                .assignAdminRoleToUser(userId);

        verifyNoMoreInteractions(adminUserService);
    }

    @Test
    void softDeleteUser_returnsSuccessMessage_whenUserExists() {

        UUID userId = UUID.randomUUID();

        doNothing()
                .when(adminUserService)
                .softDeleteUser(userId, currentUser);

        ResponseEntity<ApiResponse<Void>> response =
                controller.softDeleteUser(userId, currentUser);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().isSuccess())
                .isTrue();

        assertThat(response.getBody().getMessage())
                .isEqualTo("User deleted successfully.");

        assertThat(response.getBody().getData())
                .isNull();

        verify(adminUserService)
                .softDeleteUser(userId, currentUser);

        verifyNoMoreInteractions(adminUserService);
    }

    @Test
    void softDeleteUser_throwsResourceNotFoundException_whenUserDoesNotExist() {

        UUID userId = UUID.randomUUID();

        doThrow(
                new ResourceNotFoundException(
                        "User not found with id " + userId
                )
        )
                .when(adminUserService)
                .softDeleteUser(userId, currentUser);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> controller.softDeleteUser(userId, currentUser)
                );

        assertThat(exception.getMessage())
                .isEqualTo("User not found with id " + userId);

        verify(adminUserService)
                .softDeleteUser(userId, currentUser);

        verifyNoMoreInteractions(adminUserService);
    }

    @Test
    void softDeleteUser_throwsBusinessException_whenDeletionIsNotAllowed() {

        UUID userId = UUID.randomUUID();

        doThrow(
                new BusinessException("Cannot delete super admin.")
        )
                .when(adminUserService)
                .softDeleteUser(userId, currentUser);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> controller.softDeleteUser(userId, currentUser)
                );

        assertThat(exception.getMessage())
                .isEqualTo("Cannot delete super admin.");

        verify(adminUserService)
                .softDeleteUser(userId, currentUser);

        verifyNoMoreInteractions(adminUserService);
    }
}