package com.realestate.backend.controller.user;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.auth.response.UserResponse;
import com.realestate.backend.dto.user.request.DeleteAccountRequest;
import com.realestate.backend.dto.user.request.UpdateProfileRequest;
import com.realestate.backend.dto.user.response.UserProfilePhotoResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private  final UserService userService;

    @PatchMapping("/me/profile")
    @Operation(summary = "Update own profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
            ) {

        UserResponse response = userService.updateProfile(request, currentUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User profile successfully updated", response));

    }

    @Transactional
    @DeleteMapping("/account")
    @Operation(summary = "Delete own profile")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        userService.deleteAccount(
                request,
                currentUser
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Account successfully deleted"
                )
        );
    }

    @PostMapping(
            value = "/me/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(summary = "Upload/edit profile photo")
    public ResponseEntity<ApiResponse<UserProfilePhotoResponse>> uploadProfilePhoto(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                "Profile photo uploaded successfully",
                userService.uploadProfilePhoto(file, currentUser)
                )
        );

    }

    @DeleteMapping( "/me/photo")
    @Operation(summary = "Remove profile photo")
    public ResponseEntity<ApiResponse<Void>> removeProfilePhoto(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        userService.removeProfilePhoto(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile photo removed successfully",
                        null
                )
        );

    }

}
