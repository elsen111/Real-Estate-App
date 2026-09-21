package com.realestate.backend.service;

import com.realestate.backend.dto.request.UserStatusRequest;
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
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.AgencyRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.RefreshTokenRepository;
import com.realestate.backend.repository.RoleRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AgencyMemberRepository agencyMemberRepository;

    @Mock
    private CustomUserDetails currentUser;

    @InjectMocks
    private AdminUserServiceImpl service;

    @Test
    void changeUserStatus_enablesDisabledUser() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("user@example.com")
                .enabled(false)
                .deleted(false)
                .build();

        UserStatusRequest request = new UserStatusRequest(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        String result = service.changeUserStatus(userId, request);

        assertThat(user.getEnabled()).isTrue();
        assertThat(result).isEqualTo("User has been enabled successfully");

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void changeUserStatus_throws_whenUserNotFound() {
        UUID userId = UUID.randomUUID();

        UserStatusRequest request = new UserStatusRequest(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.changeUserStatus(userId, request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changeUserStatus_throws_whenUserIsDeleted() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("deleted@example.com")
                .enabled(false)
                .deleted(true)
                .build();

        UserStatusRequest request = new UserStatusRequest(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                service.changeUserStatus(userId, request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        assertThat(user.getEnabled()).isFalse();

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changeUserStatus_disablesEnabledUser() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("user@example.com")
                .enabled(true)
                .deleted(false)
                .build();

        UserStatusRequest request = new UserStatusRequest(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        String result = service.changeUserStatus(userId, request);

        assertThat(user.getEnabled()).isFalse();
        assertThat(result).isEqualTo("User has been disabled successfully");

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void assignAdminRoleToUser_throws_whenUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.assignAdminRoleToUser(userId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository).findById(userId);
        verify(roleRepository, never()).findByRoleName(any());
    }

    @Test
    void assignAdminRoleToUser_throws_whenUserDisabled() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .enabled(false)
                .roles(new HashSet<>())
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                service.assignAdminRoleToUser(userId)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not enabled");

        verify(userRepository).findById(userId);
        verify(roleRepository, never()).findByRoleName(any());
    }

    @Test
    void assignAdminRoleToUser_throws_whenAdminRoleNotFound() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roleRepository.findByRoleName(Role.ADMIN))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.assignAdminRoleToUser(userId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(Role.ADMIN.name());

        verify(userRepository).findById(userId);
        verify(roleRepository).findByRoleName(Role.ADMIN);
    }

    @Test
    void assignAdminRoleToUser_succeeds_whenUserEnabled() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        RoleEntity adminRole = RoleEntity.builder()
                .roleName(Role.ADMIN)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(roleRepository.findByRoleName(Role.ADMIN))
                .thenReturn(Optional.of(adminRole));

        String result = service.assignAdminRoleToUser(userId);

        assertThat(user.getRoles())
                .contains(adminRole);

        assertThat(result)
                .isEqualTo("Admin role successfully assigned to user: " + userId);

        verify(userRepository).findById(userId);
        verify(roleRepository).findByRoleName(Role.ADMIN);
    }

    @Test
    void softDeleteUser_throws_whenUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.softDeleteUser(userId, currentUser)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository).findById(userId);
        verify(currentUser, never()).getId();
        verify(refreshTokenRepository, never()).deleteAllByUser(any());
    }

    @Test
    void softDeleteUser_throws_whenAssignerNotFound() {
        UUID userId = UUID.randomUUID();
        UUID assignerId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>())
                .build();

        when(currentUser.getId())
                .thenReturn(assignerId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.softDeleteUser(userId, currentUser)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(assignerId.toString());

        verify(currentUser).getId();
        verify(userRepository).findById(userId);
        verify(userRepository).findById(assignerId);

        verify(refreshTokenRepository, never())
                .deleteAllByUser(any());
    }

    @Test
    void softDeleteUser_throws_whenUserIsSuperAdmin() {
        UUID userId = UUID.randomUUID();
        UUID assignerId = UUID.randomUUID();

        RoleEntity superAdminRole = RoleEntity.builder()
                .roleName(Role.SUPER_ADMIN)
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>(Set.of(superAdminRole)))
                .build();

        UserEntity assigner = UserEntity.builder()
                .id(assignerId)
                .build();

        when(currentUser.getId())
                .thenReturn(assignerId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.of(assigner));

        assertThatThrownBy(() ->
                service.softDeleteUser(userId, currentUser)
        )
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Cannot delete super admin.");

        verify(currentUser).getId();
        verify(userRepository).findById(userId);
        verify(userRepository).findById(assignerId);

        verify(refreshTokenRepository, never())
                .deleteAllByUser(any());

        verifyNoInteractions(
                agencyRepository,
                propertyRepository,
                agencyMemberRepository
        );
    }

    @Test
    void softDeleteUser_throws_whenAgencyOwnerHasActiveListings() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID assignerId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        RoleEntity ownerRole = RoleEntity.builder()
                .roleName(Role.AGENCY_OWNER)
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>(Set.of(ownerRole)))
                .agency(agency)
                .build();

        UserEntity assigner = UserEntity.builder()
                .id(assignerId)
                .build();

        when(currentUser.getId())
                .thenReturn(assignerId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.of(assigner));

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(propertyRepository.existsByAgencyIdAndStatus(
                agencyId,
                PropertyStatus.ACTIVE
        ))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.softDeleteUser(userId, currentUser)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("active listings");

        verify(currentUser).getId();
        verify(userRepository).findById(userId);
        verify(userRepository).findById(assignerId);
        verify(agencyRepository).findById(agencyId);
        verify(propertyRepository)
                .existsByAgencyIdAndStatus(
                        agencyId,
                        PropertyStatus.ACTIVE
                );

        verify(agencyMemberRepository, never())
                .findByUserAndActiveTrue(any());

        verify(refreshTokenRepository, never())
                .deleteAllByUser(any());
    }

    @Test
    void softDeleteUser_throws_whenAgencyOwnerAgencyNotFound() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID assignerId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        RoleEntity ownerRole = RoleEntity.builder()
                .roleName(Role.AGENCY_OWNER)
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>(Set.of(ownerRole)))
                .agency(agency)
                .build();

        UserEntity assigner = UserEntity.builder()
                .id(assignerId)
                .build();

        when(currentUser.getId())
                .thenReturn(assignerId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.of(assigner));

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.softDeleteUser(userId, currentUser)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(agencyId.toString());

        verify(currentUser).getId();
        verify(userRepository).findById(userId);
        verify(userRepository).findById(assignerId);
        verify(agencyRepository).findById(agencyId);

        verify(propertyRepository, never())
                .existsByAgencyIdAndStatus(any(), any());

        verify(agencyMemberRepository, never())
                .findByUserAndActiveTrue(any());

        verify(refreshTokenRepository, never())
                .deleteAllByUser(any());
    }

    @Test
    void softDeleteUser_throws_whenAgencyOwnerMembershipNotFound() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID assignerId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        RoleEntity ownerRole = RoleEntity.builder()
                .roleName(Role.AGENCY_OWNER)
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>(Set.of(ownerRole)))
                .agency(agency)
                .build();

        UserEntity assigner = UserEntity.builder()
                .id(assignerId)
                .build();

        when(currentUser.getId())
                .thenReturn(assignerId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.of(assigner));

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(propertyRepository.existsByAgencyIdAndStatus(
                agencyId,
                PropertyStatus.ACTIVE
        ))
                .thenReturn(false);

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.softDeleteUser(userId, currentUser)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(currentUser).getId();
        verify(userRepository).findById(userId);
        verify(userRepository).findById(assignerId);
        verify(agencyRepository).findById(agencyId);
        verify(propertyRepository)
                .existsByAgencyIdAndStatus(
                        agencyId,
                        PropertyStatus.ACTIVE
                );
        verify(agencyMemberRepository)
                .findByUserAndActiveTrue(user);

        verify(refreshTokenRepository, never())
                .deleteAllByUser(any());
    }

    @Test
    void softDeleteUser_softDeletesAgencyAndMembership_whenAgencyOwnerHasNoActiveListings() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID assignerId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .isDeleted(false)
                .build();

        RoleEntity ownerRole = RoleEntity.builder()
                .roleName(Role.AGENCY_OWNER)
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .fullName("Jane Owner")
                .phoneNumber("+994501234567")
                .roles(new HashSet<>(Set.of(ownerRole)))
                .enabled(true)
                .deleted(false)
                .agency(agency)
                .build();

        UserEntity assigner = UserEntity.builder()
                .id(assignerId)
                .email("admin@example.com")
                .build();

        AgencyMemberEntity membership = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(true)
                .role(Role.AGENCY_OWNER)
                .build();

        when(currentUser.getId())
                .thenReturn(assignerId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.of(assigner));

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(propertyRepository.existsByAgencyIdAndStatus(
                agencyId,
                PropertyStatus.ACTIVE
        ))
                .thenReturn(false);

        when(agencyMemberRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(membership));

        service.softDeleteUser(userId, currentUser);

        assertThat(membership.isActive()).isFalse();
        assertThat(membership.getRemovedBy()).isSameAs(assigner);
        assertThat(agency.getIsDeleted()).isTrue();

        assertThat(user.getEnabled()).isFalse();
        assertThat(user.getDeleted()).isTrue();
        assertThat(user.getFullName()).isEqualTo("Deleted user");
        assertThat(user.getPhoneNumber()).isNull();
        assertThat(user.getProfilePhotoUrl()).isNull();

        verify(currentUser).getId();
        verify(userRepository).findById(userId);
        verify(userRepository).findById(assignerId);
        verify(agencyRepository).findById(agencyId);
        verify(propertyRepository)
                .existsByAgencyIdAndStatus(
                        agencyId,
                        PropertyStatus.ACTIVE
                );
        verify(agencyMemberRepository)
                .findByUserAndActiveTrue(user);
        verify(refreshTokenRepository)
                .deleteAllByUser(user);
    }

    @Test
    void softDeleteUser_softDeletesUser_whenRegularUser() {
        UUID userId = UUID.randomUUID();
        UUID assignerId = UUID.randomUUID();

        RoleEntity clientRole = RoleEntity.builder()
                .roleName(Role.CLIENT)
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .fullName("Regular Client")
                .phoneNumber("+994501112233")
                .roles(new HashSet<>(Set.of(clientRole)))
                .enabled(true)
                .deleted(false)
                .build();

        UserEntity assigner = UserEntity.builder()
                .id(assignerId)
                .email("admin@example.com")
                .build();

        when(currentUser.getId())
                .thenReturn(assignerId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.of(assigner));

        service.softDeleteUser(userId, currentUser);

        assertThat(user.getEnabled()).isFalse();
        assertThat(user.getDeleted()).isTrue();
        assertThat(user.getFullName()).isEqualTo("Deleted user");
        assertThat(user.getPhoneNumber()).isNull();
        assertThat(user.getProfilePhotoUrl()).isNull();

        verify(currentUser).getId();
        verify(userRepository).findById(userId);
        verify(userRepository).findById(assignerId);
        verify(refreshTokenRepository).deleteAllByUser(user);

        verify(agencyRepository, never()).findById(any());
        verify(propertyRepository, never())
                .existsByAgencyIdAndStatus(any(), any());
        verify(agencyMemberRepository, never())
                .findByUserAndActiveTrue(any());
    }
}