package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.AccountReactivationRequest;
import com.realestate.backend.dto.response.AuthUserResponse;
import com.realestate.backend.dto.request.AccountPasswordRequest;
import com.realestate.backend.dto.request.UpdateProfileRequest;
import com.realestate.backend.dto.response.UserProfilePhotoResponse;
import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.entity.UserMediaEntity;
import com.realestate.backend.exception.AccountStateException;
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
import com.realestate.backend.storage.MediaUploadPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

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
                        new ResourceNotFoundException("User not found."));

        updateFullName(user, request);
        updateEmail(user, request);
        updatePhoneNumber(user, request);

        log.atInfo()
                .setMessage("User profile updated")
                .addKeyValue("userId", user.getId())
                .addKeyValue("userEmail", user.getEmail())
                .log();

        return userMapper.toSummary(user);

    }

    @Override
    @Transactional
    public void deleteAccount(AccountPasswordRequest request, CustomUserDetails currentUser) {

        UserEntity user = userRepository.findByEmail(
                currentUser.getEmail()
        ).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new UnauthorizedException(
                    "Invalid password."
            );
        }

        user.setEnabled(false);
        user.setDeleted(true);

        refreshTokenRepository.deleteAllByUser(user);

        log.atInfo()
                .setMessage("User account deleted")
                .addKeyValue("userId", user.getId())
                .log();

    }

    @Override
    @Transactional
    public UserEntity disableAccount(AccountPasswordRequest request, CustomUserDetails currentUser) {

        UserEntity user = userRepository.findByEmail(
                currentUser.getEmail()
        ).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new UnauthorizedException(
                    "Invalid password."
            );
        }

        if (!user.getEnabled()) {
            throw new AccountStateException("Account is already disabled.");
        }

        user.setEnabled(false);

        refreshTokenRepository.deleteAllByUser(user);

        log.atInfo()
                .setMessage("User account disabled")
                .addKeyValue("userId", user.getId())
                .log();

        return user;

    }

    @Transactional
    @Override
    public UserEntity enableAccount(AccountReactivationRequest request) {

        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        ;

        UserEntity user = userRepository
                .findByEmailAndDeletedFalse(email)
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid email or password"));

        if (user.getEnabled()) {
            throw new AccountStateException("Account is already enabled");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new UnauthorizedException("Invalid email or password");
        }

        user.setEnabled(true);

        log.atInfo()
                .setMessage("User account reactivated")
                .addKeyValue("userId", user.getId())
                .addKeyValue("userEmail", user.getEmail())
                .log();

        return user;
    }

    @Override
    @Transactional
    public UserProfilePhotoResponse uploadProfilePhoto(
            MultipartFile file,
            CustomUserDetails currentUser
    ) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        Optional<UserMediaEntity> existingPhoto =
                userMediaRepository.findByUserId(user.getId());

        MediaFileEntity uploadedMedia =
                mediaService.upload(file, MediaUploadPolicy.USER_PROFILE);

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

        log.atInfo()
                .setMessage("Profile photo uploaded for user")
                .addKeyValue("userId", user.getId())
                .log();

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
                                "Profile image not found for user: " + currentUser.getId()
                        ));

        userMediaRepository.delete(userMedia);

        mediaService.delete(userMedia.getMedia());

        log.atInfo()
                .setMessage("Profile photo removed for user")
                .addKeyValue("userId", currentUser.getId())
                .log();

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

        if (email.equals(user.getEmail())) {
            return;
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(
                    "User already exists with the email: " + email
            );
        }

        user.setEmail(email);

    }

    private void updateFullName(
            UserEntity user,
            UpdateProfileRequest request
    ) {

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }

    }
}
