package com.realestate.backend.controller.admin;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.admin.user.request.AdminUserFilterRequest;
import com.realestate.backend.dto.admin.user.response.AdminUserResponse;
import com.realestate.backend.dto.auth.response.UserResponse;
import com.realestate.backend.service.admin.user.AdminUserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserServiceImpl adminUserService;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getAllUsers(
            @ModelAttribute AdminUserFilterRequest filter,
            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable
            ) {

        Page<AdminUserResponse> response = adminUserService.getAllUsers(filter, pageable);

        ApiResponse<Page<AdminUserResponse>> apiResponse =
                ApiResponse.success("Users fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(
            @PathVariable UUID userId
    ) {

        AdminUserResponse response = adminUserService.getUserById(userId);

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

}
