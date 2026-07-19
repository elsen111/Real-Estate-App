package com.realestate.backend.service;

import com.realestate.backend.dto.request.AdminUserFilterRequest;
import com.realestate.backend.dto.response.UserResponse;
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
