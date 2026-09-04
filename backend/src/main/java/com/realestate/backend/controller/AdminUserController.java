package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminUserFilterRequest;
import com.realestate.backend.dto.request.UserStatusRequest;
import com.realestate.backend.dto.response.UserResponse;
import com.realestate.backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @ModelAttribute AdminUserFilterRequest filter,
            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable
            ) {

        Page<UserResponse> response = adminUserService.getAllUsers(filter, pageable);

        ApiResponse<Page<UserResponse>> apiResponse =
                ApiResponse.success("Users fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID userId
    ) {

        UserResponse response = adminUserService.getUserById(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User fetched successfully", response)
        );

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Transactional
    @PatchMapping("/{userId}/status")
    @Operation(summary = "Change user activation (status)")
    public ResponseEntity<ApiResponse<Void>> changeUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UserStatusRequest request
            ) {
        String message = adminUserService.changeUserStatus(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(message, null)
        );

    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{userId}/assign-admin")
    public ResponseEntity<ApiResponse<Void>> assignAdmin(
            @PathVariable UUID userId
    ) {
        String message = adminUserService.assignAdminRoleToUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success(message, null)
        );
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> softDeleteUser(
            @PathVariable UUID userId
    ) {

        adminUserService.softDeleteUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully.", null)
        );
    }

}
