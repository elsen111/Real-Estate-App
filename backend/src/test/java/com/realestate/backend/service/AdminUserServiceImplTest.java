package com.realestate.backend.service;

import com.realestate.backend.dto.request.UserStatusRequest;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.repository.*;
import com.realestate.backend.service.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private AgencyRepository agencyRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks private AdminUserServiceImpl service;

    @Test
    void changeUserStatus_enablesDisabledUser() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("a@b.com")
                .enabled(false)
                .build();

        UserStatusRequest request = new UserStatusRequest(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String result = service.changeUserStatus(userId, request);

        assertThat(user.getEnabled()).isTrue();
        assertThat(result).contains("enabled");

        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    void changeUserStatus_throws_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        UserStatusRequest request = new UserStatusRequest(true);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.changeUserStatus(userId, request)
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changeUserStatus_disablesEnabledUser() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("a@b.com")
                .enabled(true)
                .build();

        UserStatusRequest request = new UserStatusRequest(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String result = service.changeUserStatus(userId, request);

        assertThat(user.getEnabled()).isFalse();
        assertThat(result).contains("disabled");

        verify(userRepository).save(user);
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

    @Test
    void softDeleteUser_throws_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.softDeleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository, never()).save(any());
    }

    @Test
    void softDeleteUser_throws_whenUserIsSuperAdmin() {
        UUID userId = UUID.randomUUID();
        RoleEntity superAdminRole = RoleEntity.builder().roleName(Role.SUPER_ADMIN).build();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>(Set.of(superAdminRole)))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.softDeleteUser(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("super admin");

        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteAllByUser(any());
    }

    @Test
    void softDeleteUser_throws_whenAgencyOwnerHasActiveListings() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder().id(agencyId).build();
        RoleEntity ownerRole = RoleEntity.builder().roleName(Role.AGENCY_OWNER).build();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>(Set.of(ownerRole)))
                .agency(agency)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));
        when(propertyRepository.existsByAgencyIdAndStatus(agencyId, PropertyStatus.ACTIVE))
                .thenReturn(true);

        assertThatThrownBy(() -> service.softDeleteUser(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("active listings");

        verify(agencyRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteAllByUser(any());
    }

    @Test
    void softDeleteUser_throws_whenAgencyOwnerAgencyNotFound() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder().id(agencyId).build();
        RoleEntity ownerRole = RoleEntity.builder().roleName(Role.AGENCY_OWNER).build();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>(Set.of(ownerRole)))
                .agency(agency)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(agencyRepository.findById(agencyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.softDeleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(agencyId.toString());

        verify(userRepository, never()).save(any());
    }

    @Test
    void softDeleteUser_softDeletesAgency_whenAgencyOwnerHasNoActiveListings() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder().id(agencyId).isDeleted(false).build();
        RoleEntity ownerRole = RoleEntity.builder().roleName(Role.AGENCY_OWNER).build();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .fullName("Jane Owner")
                .phoneNumber("+994501234567")
                .roles(new HashSet<>(Set.of(ownerRole)))
                .agency(agency)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));
        when(propertyRepository.existsByAgencyIdAndStatus(agencyId, PropertyStatus.ACTIVE))
                .thenReturn(false);

        service.softDeleteUser(userId);

        assertThat(agency.getIsDeleted()).isTrue();
        verify(agencyRepository).save(agency);

        assertThat(user.getEnabled()).isFalse();
        assertThat(user.getDeleted()).isTrue();
        assertThat(user.getFullName()).isEqualTo("Deleted user");
        assertThat(user.getPhoneNumber()).isNull();
        assertThat(user.getProfilePhotoUrl()).isNull();

        verify(refreshTokenRepository).deleteAllByUser(user);
        verify(userRepository).save(user);
    }

    @Test
    void softDeleteUser_softDeletesUser_whenRegularUser() {
        UUID userId = UUID.randomUUID();
        RoleEntity clientRole = RoleEntity.builder().roleName(Role.CLIENT).build();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .fullName("Regular Client")
                .phoneNumber("+994501112233")
                .roles(new HashSet<>(Set.of(clientRole)))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        service.softDeleteUser(userId);

        assertThat(user.getEnabled()).isFalse();
        assertThat(user.getDeleted()).isTrue();
        assertThat(user.getFullName()).isEqualTo("Deleted user");
        assertThat(user.getPhoneNumber()).isNull();

        verify(refreshTokenRepository).deleteAllByUser(user);
        verify(userRepository).save(user);
        verify(agencyRepository, never()).findById(any());
        verify(propertyRepository, never()).existsByAgencyIdAndStatus(any(), any());
    }

}