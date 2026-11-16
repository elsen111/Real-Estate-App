package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.AccountReactivationRequest;
import com.realestate.backend.dto.response.AuthUserResponse;
import com.realestate.backend.dto.request.AccountPasswordRequest;
import com.realestate.backend.dto.request.UpdateProfileRequest;
import com.realestate.backend.dto.response.UserProfilePhotoResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.*;
import com.realestate.backend.mapper.UserMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.MediaService;
import com.realestate.backend.service.UserService;
import com.realestate.backend.storage.MediaUploadPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    private final AgencyRepository agencyRepository;
    private final AgencyMemberRepository agencyMemberRepository;
    private final PropertyRepository propertyRepository;

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

        UUID assignerId = currentUser.getId();

        boolean isUserSuperAdmin = user.getRoles()
                .stream()
                .map(RoleEntity::getRoleName)
                .anyMatch(role -> role == Role.SUPER_ADMIN);

        boolean isUserAgent = user.getRoles()
                .stream()
                .map(RoleEntity::getRoleName)
                .anyMatch(role -> role == Role.AGENT);

        boolean isUserAgencyOwner = user.getRoles()
                .stream()
                .map(RoleEntity::getRoleName)
                .anyMatch(role -> role == Role.AGENCY_OWNER);

        if (isUserSuperAdmin) {
            throw new ForbiddenException("Cannot delete super admin.");
        }

        if (isUserAgencyOwner || isUserAgent) {

            AgencyEntity agency = agencyRepository.findById(user.getAgency().getId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Agency not found with id: " + user.getAgency().getId() + "!")
                    );

            boolean hasActiveListings = propertyRepository.existsByAgencyIdAndStatus(
                    agency.getId(), PropertyStatus.ACTIVE);

            if (hasActiveListings) {
                throw new BusinessException("Cannot delete the user (agency owner) whose agency has active listings!");
            }

            AgencyMemberEntity membership = agencyMemberRepository.findByUserAndActiveTrue(user)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Agency member not found with id: " + user.getId())
                    );

            membership.setActive(false);
            membership.setRemovedBy(user);

            if(isUserAgencyOwner) {
                agency.setIsDeleted(true);
            }

        }

        userMediaRepository.findByUserId(user.getId()).ifPresent(userMedia -> {
            userMediaRepository.delete(userMedia);
            mediaService.delete(userMedia.getMedia());
        });

        user.setEnabled(false);
        user.setDeleted(true);

        user.setEmailVerified(false);
        user.setDeletedBy(user);
        user.setDeletedAt(LocalDateTime.now());

        user.setFullName("Deleted User");
        user.setEmail("deleted+" + user.getId() + "@example.com");
        user.setPhoneNumber(null);
        user.setProfilePhotoUrl(null);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

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
