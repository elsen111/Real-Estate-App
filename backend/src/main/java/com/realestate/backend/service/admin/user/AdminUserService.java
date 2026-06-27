package com.realestate.backend.service.admin.user;

import com.realestate.backend.dto.admin.user.request.AdminUserFilterRequest;
import com.realestate.backend.dto.admin.user.response.AdminUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AdminUserService {

    Page<AdminUserResponse> getAllUsers(
            AdminUserFilterRequest request,
            Pageable pageable
    );

    AdminUserResponse getUserById(UUID userId);

    String toggleUserStatus(UUID userId);

    String assignAdminRoleToUser(UUID userId);

}
