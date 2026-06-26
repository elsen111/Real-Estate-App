package com.realestate.backend.service.admin.user;

import com.realestate.backend.dto.admin.user.request.AdminUserFilterRequest;
import com.realestate.backend.dto.admin.user.response.AdminUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<AdminUserResponse> getAllUsers(
            AdminUserFilterRequest request,
            Pageable pageable
    );

}
