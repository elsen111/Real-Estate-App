package com.realestate.backend.service.user;

import com.realestate.backend.dto.auth.response.UserResponse;
import com.realestate.backend.dto.user.request.DeleteAccountRequest;
import com.realestate.backend.dto.user.request.UpdateProfileRequest;
import com.realestate.backend.dto.user.response.UserProfilePhotoResponse;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponse updateProfile(
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

}
