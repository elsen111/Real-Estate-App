package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.AdminUserFilterRequest;
import com.realestate.backend.dto.request.UserStatusRequest;
import com.realestate.backend.dto.response.UserResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.UserMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.repository.specification.UserSpecification;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final AgencyRepository agencyRepository;
    private final PropertyRepository propertyRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AgencyMemberRepository agencyMemberRepository;


    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(
            AdminUserFilterRequest request,
            Pageable pageable
    ) {

        Specification<UserEntity> specification = UserSpecification.withFilter(request);

        return userRepository.findAll(specification, pageable)
                .map(userMapper::toAdminResponse);

    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                )
        );

        return userMapper.toAdminResponse(user);
    }

    @Override
    @Transactional
    public String changeUserStatus(UUID userId, UserStatusRequest request) {

        UserEntity user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                )
        );

        if (user.getDeleted()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        user.setEnabled(request.enabled());
        Boolean isEnabled = user.getEnabled();

        log.atInfo()
                .setMessage("User status changed")
                .addKeyValue("userId", user.getId())
                .addKeyValue("userEmail", user.getEmail())
                .addKeyValue("isEnabled", isEnabled)
                .log();

        return isEnabled
                ? "User has been enabled successfully"
                : "User has been disabled successfully";

    }

    @Override
    @Transactional
    public String assignAdminRoleToUser(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                )
        );

        if (!user.getEnabled()) {
            throw new BusinessException(
                    "This user profile is not enabled. Firstly, activate this profile, then try again."
            );
        }

        RoleEntity adminRoleEntity = roleRepository.findByRoleName(Role.ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with name: " + Role.ADMIN.name()
                ));

        user.getRoles().add(adminRoleEntity);

        log.atInfo()
                .setMessage("Admin role assigned to the user")
                .addKeyValue("userId", user.getId())
                .addKeyValue("userEmail", user.getEmail())
                .log();

        return "Admin role successfully assigned to user: " + userId;
    }

    @Override
    @Transactional
    public void softDeleteUser(UUID userId, CustomUserDetails currentUser) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + userId)
                );

        UUID assignerId = currentUser.getId();

        UserEntity assigner = userRepository.findById(assignerId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "User not found with id: " + assignerId
                        )
                );

        boolean isUserSuperAdmin = user.getRoles()
                .stream()
                .map(RoleEntity::getRoleName)
                .anyMatch(role -> role == Role.SUPER_ADMIN);

        boolean isUserAgencyOwner = user.getRoles()
                .stream()
                .map(RoleEntity::getRoleName)
                .anyMatch(role -> role == Role.AGENCY_OWNER);

        if (isUserSuperAdmin) {
            throw new ForbiddenException("Cannot delete super admin.");
        }

        if (isUserAgencyOwner) {

            AgencyEntity agency = agencyRepository.findById(user.getAgency().getId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Agency not found with id: " + user.getAgency().getId())
                    );

            boolean hasActiveListings = propertyRepository.existsByAgencyIdAndStatus(
                    agency.getId(), PropertyStatus.ACTIVE);

            if (hasActiveListings) {
                throw new BusinessException("Cannot delete the user (agency owner) whose agency has active listings.");
            }

            AgencyMemberEntity membership = agencyMemberRepository.findByUserAndActiveTrue(user)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Agency member not found with id: " + user.getId())
                    );

            membership.setActive(false);
            membership.setRemovedBy(assigner);

            agency.setIsDeleted(true);

        }

        refreshTokenRepository.deleteAllByUser(user);

        user.setEnabled(false);
        user.setDeleted(true);
        user.setFullName("Deleted user");
        user.setPhoneNumber(null);
        user.setProfilePhotoUrl(null);

        log.info("User deleted");
    }

}
