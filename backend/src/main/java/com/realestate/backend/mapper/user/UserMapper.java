package com.realestate.backend.mapper.user;

import com.realestate.backend.dto.auth.response.UserResponse;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toSummary(UserEntity user) {
        if(user == null) {
            return null;
        }

        String position = user.getRoles()
                .stream()
                .filter(r -> r.getRoleName() != Role.SUPER_ADMIN)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Role not found"))
                .getRoleName().getLabel();

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

        if (roles.contains(Role.SUPER_ADMIN)) {
            return Role.SUPER_ADMIN.getLabel();
        }

        if (roles.contains(Role.AGENCY_OWNER)) {
            return Role.AGENCY_OWNER.getLabel();
        }

        if (roles.contains(Role.AGENT)) {
            return Role.AGENT.getLabel();
        }

        if (roles.contains(Role.LANDLORD)) {
            return Role.LANDLORD.getLabel();
        }

        if (roles.contains(Role.CLIENT)) {
            return Role.CLIENT.getLabel();
        }

        return null;
    }

}
