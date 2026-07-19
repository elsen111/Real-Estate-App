package com.realestate.backend.service;

import com.realestate.backend.dto.response.AuthUserResponse;
import com.realestate.backend.dto.request.DeleteAccountRequest;
import com.realestate.backend.dto.request.UpdateProfileRequest;
import com.realestate.backend.dto.response.UserProfilePhotoResponse;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    AuthUserResponse updateProfile(
            UpdateProfileRequest request,
            CustomUserDetails currentUser
    );

    void deleteAccount(
            DeleteAccountRequest request,
            CustomUserDetails currentUser
    );

    UserProfilePhotoResponse uploadProfilePhoto(
            MultipartFile file,
            CustomUserDetails currentUser
    );

    void removeProfilePhoto(CustomUserDetails currentUser);

}
