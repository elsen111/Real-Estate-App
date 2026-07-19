package com.realestate.backend.service.impl;

import com.realestate.backend.dto.response.AuthUserResponse;
import com.realestate.backend.dto.request.DeleteAccountRequest;
import com.realestate.backend.dto.request.UpdateProfileRequest;
import com.realestate.backend.dto.response.UserProfilePhotoResponse;
import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.entity.UserMediaEntity;
import com.realestate.backend.enums.MediaFolder;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.exception.UnauthorizedException;
import com.realestate.backend.mapper.UserMapper;
import com.realestate.backend.repository.RefreshTokenRepository;
import com.realestate.backend.repository.UserMediaRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.MediaService;
import com.realestate.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private  final PasswordEncoder passwordEncoder;

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserMediaRepository userMediaRepository;

    private final MediaService mediaService;

    @Override
    @Transactional
    public AuthUserResponse updateProfile(
            UpdateProfileRequest request,
            CustomUserDetails currentUser
    ) {

        UserEntity user = userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with this email"));

        updateFullName(user, request);
        updateEmail(user, request);
        updatePhoneNumber(user, request);

        user = userRepository.save(user);

        return userMapper.toSummary(user);

    }

    @Override
    @Transactional
    public void deleteAccount(DeleteAccountRequest request, CustomUserDetails currentUser) {

        UserEntity user = userRepository.findByEmail(
                currentUser.getEmail()
        ).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new UnauthorizedException(
                    "Incorrect password provided"
            );
        }

        user.setEnabled(false);

        userRepository.save(user);

        refreshTokenRepository.deleteAllByUser(user);

    }

    @Override
    @Transactional
    public UserProfilePhotoResponse uploadProfilePhoto(
            MultipartFile file,
            CustomUserDetails currentUser
    ) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with this id")
                );

        Optional<UserMediaEntity> existingPhoto =
                userMediaRepository.findByUserId(user.getId());

        MediaFileEntity uploadedMedia =
                mediaService.upload(file, MediaFolder.USER_PROFILE);

        if (existingPhoto.isPresent()) {

            UserMediaEntity userMedia = existingPhoto.get();

            userMediaRepository.delete(userMedia);

            mediaService.delete(userMedia.getMedia());

        }

        UserMediaEntity userMedia = UserMediaEntity.builder()
                .user(user)
                .media(uploadedMedia)
                .build();

        userMediaRepository.save(userMedia);

        return UserProfilePhotoResponse.builder()
                .photoUrl(uploadedMedia.getFileUrl())
                .build();

    }

    @Override
    @Transactional
    public void removeProfilePhoto(CustomUserDetails currentUser) {

        UserMediaEntity userMedia = userMediaRepository
                .findByUserId(currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Profile photo not found."
                        ));

        userMediaRepository.delete(userMedia);

        mediaService.delete(userMedia.getMedia());

    }



//    HELPER METHODS
    private void updatePhoneNumber(
            UserEntity user,
            UpdateProfileRequest request
    ) {

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

    }

    private void updateEmail(
            UserEntity user,
            UpdateProfileRequest request
    ) {

        if (request.getEmail() == null) {
            return;
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if(email.equals(user.getEmail())) {
            return;
        }

        if(userRepository.existsByEmail(email)) {
            throw new ConflictException(
                    "User already exists with this email"
            );
        }

        user.setEmail(email);

    }

    private void updateFullName(
            UserEntity user,
            UpdateProfileRequest request
    ) {

        if(request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }

    }
}
