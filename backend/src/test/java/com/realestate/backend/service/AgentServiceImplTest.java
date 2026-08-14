package com.realestate.backend.service;

import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.AgentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private AgencyMemberRepository agencyMemberRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private PropertyMapper propertyMapper;

    @InjectMocks private AgentServiceImpl service;

    @Test
    void getAgentByUserId_throws_whenAgentNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findAgentMemberByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAgentByUserId(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteAgentFromAgency_throws_whenCallerNotOwnerOrSuperAdmin() {
        UUID agentId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        UserEntity agent = UserEntity.builder()
                .id(agentId)
                .build();

        AgencyMemberEntity membership = AgencyMemberEntity.builder()
                .agency(agency)
                .user(agent)
                .active(true)
                .build();

        UserEntity currentUserEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .roles(Set.of(
                        RoleEntity.builder()
                                .roleName(Role.AGENT)
                                .build()
                ))
                .build();

        CustomUserDetails currentUser =
                CustomUserDetails.from(currentUserEntity);

        when(agencyMemberRepository.findByUser_IdAndActiveTrue(agentId))
                .thenReturn(Optional.of(membership));

        when(userRepository.findById(currentUser.getId()))
                .thenReturn(Optional.of(currentUserEntity));

        when(agencyMemberRepository.findByAgency_IdAndUser_IdAndActiveTrue(
                agencyId,
                currentUser.getId()
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.deleteAgentFromAgency(agentId, currentUser)
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteAgentFromAgency_throws_whenActiveMembershipNotFound() {
        UUID agentId = UUID.randomUUID();
        when(agencyMemberRepository.findByUser_IdAndActiveTrue(agentId)).thenReturn(Optional.empty());
        CustomUserDetails currentUser = CustomUserDetails.from(UserEntity.builder().id(UUID.randomUUID()).roles(Set.of()).build());

        assertThatThrownBy(() -> service.deleteAgentFromAgency(agentId, currentUser))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOwnAssignedProperties_returnsMappedPage_forCurrentAgent() {
        CustomUserDetails currentUser = CustomUserDetails.from(
                UserEntity.builder().id(UUID.randomUUID()).roles(Set.of(
                        RoleEntity.builder().roleName(Role.AGENT).build())).build());

        PropertyFilterRequest filter = new PropertyFilterRequest();
        filter.setCity("Baku");
        Pageable pageable = Pageable.ofSize(10);

        PropertyEntity entity = PropertyEntity.builder()
                .id(UUID.randomUUID())
                .title("Assigned to me")
                .city("Baku")
                .build();
        Page<PropertyEntity> entityPage = new PageImpl<>(List.of(entity));

        PropertyResponse mappedResponse = PropertyResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .city(entity.getCity())
                .build();

        when(propertyRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(entityPage);
        when(propertyMapper.toAdminPropertyResponse(entity)).thenReturn(mappedResponse);

        Page<PropertyResponse> result = service.getOwnAssignedProperties(currentUser, filter, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mappedResponse);

        verify(propertyRepository).findAll(any(Specification.class), eq(pageable));
        verify(propertyMapper).toAdminPropertyResponse(entity);
    }

    @Test
    void getOwnAssignedProperties_returnsEmptyPage_whenAgentHasNoAssignedProperties() {
        CustomUserDetails currentUser = CustomUserDetails.from(
                UserEntity.builder().id(UUID.randomUUID()).roles(Set.of(
                        RoleEntity.builder().roleName(Role.AGENT).build())).build());

        PropertyFilterRequest filter = new PropertyFilterRequest();
        Pageable pageable = Pageable.ofSize(10);

        when(propertyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<PropertyResponse> result = service.getOwnAssignedProperties(currentUser, filter, pageable);

        assertThat(result.getContent()).isEmpty();

        verify(propertyRepository).findAll(any(Specification.class), eq(pageable));
        verify(propertyMapper, org.mockito.Mockito.never()).toAdminPropertyResponse(any());
    }

    @Test
    void getOwnAssignedProperties_queriesByCurrentUsersId_notAnArbitraryUser() {
        UUID currentUserId = UUID.randomUUID();
        CustomUserDetails currentUser = CustomUserDetails.from(
                UserEntity.builder().id(currentUserId).roles(Set.of(
                        RoleEntity.builder().roleName(Role.AGENT).build())).build());

        PropertyFilterRequest filter = new PropertyFilterRequest();
        Pageable pageable = Pageable.ofSize(10);

        when(propertyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getOwnAssignedProperties(currentUser, filter, pageable);

        verify(propertyRepository).findAll(any(Specification.class), eq(pageable));
    }
}