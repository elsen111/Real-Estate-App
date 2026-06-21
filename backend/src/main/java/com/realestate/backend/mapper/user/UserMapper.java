package com.realestate.backend.mapper.user;

import com.realestate.backend.dto.auth.response.UserResponse;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

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

}
