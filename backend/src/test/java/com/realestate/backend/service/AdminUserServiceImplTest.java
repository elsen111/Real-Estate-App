package com.realestate.backend.service;

import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.repository.RoleRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.service.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    @InjectMocks private AdminUserServiceImpl service;

    @Test
    void toggleUserStatus_enablesDisabledUser() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).email("a@b.com").enabled(false).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String result = service.toggleUserStatus(userId);

        assertThat(user.getEnabled()).isTrue();
        assertThat(result).contains("enabled");
    }

    @Test
    void toggleUserStatus_throws_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.toggleUserStatus(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void assignAdminRoleToUser_throws_whenUserDisabled() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).enabled(false).roles(new HashSet<>()).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.assignAdminRoleToUser(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not enabled");
    }

    @Test
    void assignAdminRoleToUser_succeeds_whenUserEnabled() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).enabled(true).roles(new HashSet<>()).build();
        RoleEntity adminRole = RoleEntity.builder().roleName(Role.ADMIN).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(Role.ADMIN)).thenReturn(Optional.of(adminRole));

        String result = service.assignAdminRoleToUser(userId);

        assertThat(user.getRoles()).contains(adminRole);
        assertThat(result).contains(userId.toString());
    }
}