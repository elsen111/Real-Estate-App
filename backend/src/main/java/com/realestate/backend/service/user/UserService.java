package com.realestate.backend.service.user;

import com.realestate.backend.dto.auth.response.UserResponse;
import com.realestate.backend.dto.user.request.DeleteAccountRequest;
import com.realestate.backend.dto.user.request.UpdateProfileRequest;
import com.realestate.backend.security.CustomUserDetails;

public interface UserService {

    UserResponse updateProfile(
            UpdateProfileRequest request,
            CustomUserDetails currentUser
    );

    void deleteAccount(
            DeleteAccountRequest request,
            CustomUserDetails currentUser
    );

}
