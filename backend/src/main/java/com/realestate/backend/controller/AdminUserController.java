package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminUserFilterRequest;
import com.realestate.backend.dto.response.UserResponse;
import com.realestate.backend.service.impl.AdminUserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
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

    private final AdminUserServiceImpl adminUserService;

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

    @Transactional
    @PatchMapping("/{userId}/status")
    @Operation(summary = "Toggle user status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @PathVariable UUID userId
    ) {
        String message = adminUserService.toggleUserStatus(userId);

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

}
