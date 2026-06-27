package com.realestate.backend.mapper.user;

import com.realestate.backend.dto.admin.user.response.AdminUserResponse;
import com.realestate.backend.dto.auth.response.UserResponse;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private static final List<Role> ROLE_PRIORITY_LIST = List.of(
            Role.SUPER_ADMIN,
            Role.ADMIN,
            Role.AGENCY_OWNER,
            Role.AGENT,
            Role.LANDLORD,
            Role.CLIENT
    );

    public UserResponse toSummary(UserEntity user) {
        if(user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(String.valueOf(user.getPhoneNumber()))
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .roles(toRoleNames(user))
                .position(resolvePosition(user))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public AdminUserResponse toAdminResponse(UserEntity user) {
        if(user == null) {
            return null;
        }

        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(String.valueOf(user.getPhoneNumber()))
                .agency(user.getAgency() != null ? user.getAgency().getName() : null)
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .roles(toRoleNames(user))
                .position(resolvePosition(user))
                .createdAt(user.getCreatedAt())
                .build();
    }

    public Page<AdminUserResponse> toAdminResponse(Page<UserEntity> users) {
        return users.map(this::toAdminResponse);
    }


    private Set<String> toRoleNames(UserEntity user) {
        return user.getRoles()
                .stream()
                .map(RoleEntity::getRoleName)
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    private String resolvePosition(UserEntity user) {

        Set<Role> roles = user.getRoles()
                .stream()
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toSet());

        return ROLE_PRIORITY_LIST.stream()
                .filter(roles::contains)
                .findFirst()
                .map(Role::getLabel)
                .orElse(null);
    }

}
