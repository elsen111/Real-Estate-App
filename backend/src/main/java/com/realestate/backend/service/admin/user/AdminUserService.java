package com.realestate.backend.service.admin.user;

import com.realestate.backend.dto.admin.user.request.AdminUserFilterRequest;
import com.realestate.backend.dto.user.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {

    Page<UserResponse> getAllUsers(
            AdminUserFilterRequest request,
            Pageable pageable
    );

    UserResponse getUserById(UUID userId);

    String toggleUserStatus(UUID userId);

    String assignAdminRoleToUser(UUID userId);

}
