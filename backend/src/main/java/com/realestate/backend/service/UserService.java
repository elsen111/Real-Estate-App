package com.realestate.backend.service;

import com.realestate.backend.dto.request.AccountReactivationRequest;
import com.realestate.backend.dto.response.AuthUserResponse;
import com.realestate.backend.dto.request.AccountPasswordRequest;
import com.realestate.backend.dto.request.UpdateProfileRequest;
import com.realestate.backend.dto.response.UserProfilePhotoResponse;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    AuthUserResponse updateProfile(
            UpdateProfileRequest request,
            CustomUserDetails currentUser
    );

    void deleteAccount(
            AccountPasswordRequest request,
            CustomUserDetails currentUser
    );

    UserEntity disableAccount(
            AccountPasswordRequest request,
            CustomUserDetails currentUser
    );

    UserEntity enableAccount(
            AccountReactivationRequest request
    );

    UserProfilePhotoResponse uploadProfilePhoto(
            MultipartFile file,
            CustomUserDetails currentUser
    );

    void removeProfilePhoto(
            CustomUserDetails currentUser
    );

}
