package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminUserFilterRequest;
import com.realestate.backend.dto.request.UserStatusRequest;
import com.realestate.backend.dto.response.UserResponse;
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(
        name = "Admin - User Management",
        description = """
                Administrative endpoints for managing platform users, including user retrieval,
                filtering, account status management, role assignment, and account deletion.
                """
)
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping
    @Operation(
            summary = "Get all users",
            description = """
                    Retrieves a paginated list of platform users with optional filtering by user attributes,
                    roles, and account status. Supports sorting and pagination.
                    """
    )
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @ModelAttribute AdminUserFilterRequest filter,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt"
            )
            Pageable pageable
    ) {

        Page<UserResponse> response = adminUserService.getAllUsers(filter, pageable);

        ApiResponse<Page<UserResponse>> apiResponse =
                ApiResponse.success("Users fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves detailed information about a specific platform user using their unique identifier."
    )
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
    @Operation(
            summary = "Change user account status",
            description = """
                    Updates the activation status of a specific user account using the provided status request.
                    Allows administrators to activate or deactivate user accounts.
                    """
    )
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
    @Operation(
            summary = "Assign admin role to user",
            description = """
                    Assigns the ADMIN role to a specific user, granting administrative access
                    to authorized platform management operations.
                    This operation is restricted to super administrators.
                    """
    )
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
    @Operation(
            summary = "Soft-delete user account",
            description = """
                    Soft-deletes a specific user account without permanently removing its database record.
                    The operation is restricted to super administrators and uses the authenticated
                    administrator's identity to perform the deletion.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> softDeleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        adminUserService.softDeleteUser(userId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully.", null)
        );
    }

}
