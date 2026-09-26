package com.realestate.backend.service;

import com.realestate.backend.dto.request.InquiryFilterRequest;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.response.AgentResponse;
import com.realestate.backend.dto.response.InquiryResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.InquiryEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.InquiryMapper;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.mapper.UserMapper;
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.InquiryRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.AgentServiceImpl;
import com.realestate.backend.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AgencyMemberRepository agencyMemberRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private PropertyMapper propertyMapper;

    @Mock
    private RefreshTokenServiceImpl refreshTokenService;

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryMapper inquiryMapper;

    @InjectMocks
    private AgentServiceImpl service;

    private InquiryFilterRequest emptyInquiryFilter() {
        return new InquiryFilterRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void getAgentByUserId_returnsMappedAgent_whenAgentExists() {
        UUID userId = UUID.randomUUID();

        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .build();

        AgentResponse mappedResponse = AgentResponse.builder()
                .build();

        when(userRepository.findAgentMemberByUserId(userId))
                .thenReturn(Optional.of(member));

        when(userMapper.toAgentWithUserIdResponse(member))
                .thenReturn(mappedResponse);

        AgentResponse result =
                service.getAgentByUserId(userId);

        assertThat(result)
                .isSameAs(mappedResponse);

        verify(userRepository)
                .findAgentMemberByUserId(userId);

        verify(userMapper)
                .toAgentWithUserIdResponse(member);
    }

    @Test
    void getAgentByUserId_throws_whenAgentNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findAgentMemberByUserId(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.getAgentByUserId(userId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "Agent not found with user id: " + userId
                );

        verify(userRepository)
                .findAgentMemberByUserId(userId);

        verifyNoInteractions(userMapper);
    }

    @Test
    void getPublicAgentProperties_returnsMappedPage_whenAgentExists() {
        UUID userId = UUID.randomUUID();

        PropertyFilterRequest filter =
                new PropertyFilterRequest();

        Pageable pageable =
                Pageable.ofSize(10);

        AgencyMemberEntity membership =
                AgencyMemberEntity.builder()
                        .id(UUID.randomUUID())
                        .active(true)
                        .build();

        PropertyEntity property =
                PropertyEntity.builder()
                        .id(UUID.randomUUID())
                        .title("Beautiful Apartment")
                        .city("Baku")
                        .build();

        PropertyResponse response =
                PropertyResponse.builder()
                        .id(property.getId())
                        .title(property.getTitle())
                        .city(property.getCity())
                        .build();

        when(userRepository.findAgentMemberByUserId(userId))
                .thenReturn(Optional.of(membership));

        when(propertyRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of(property))
        );

        when(propertyMapper.toPublicAgencyPropertyResponse(property))
                .thenReturn(response);

        Page<PropertyResponse> result =
                service.getPublicAgentProperties(
                        userId,
                        filter,
                        pageable
                );

        assertThat(result)
                .isNotNull();

        assertThat(result.getContent())
                .containsExactly(response);

        verify(userRepository)
                .findAgentMemberByUserId(userId);

        verify(propertyRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );

        verify(propertyMapper)
                .toPublicAgencyPropertyResponse(property);
    }

    @Test
    void getPublicAgentProperties_throws_whenAgentNotFound() {
        UUID userId = UUID.randomUUID();

        PropertyFilterRequest filter =
                new PropertyFilterRequest();

        Pageable pageable =
                Pageable.ofSize(10);

        when(userRepository.findAgentMemberByUserId(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.getPublicAgentProperties(
                        userId,
                        filter,
                        pageable
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "Agent not found with user id: " + userId
                );

        verify(userRepository)
                .findAgentMemberByUserId(userId);

        verifyNoInteractions(propertyRepository);
        verifyNoInteractions(propertyMapper);
    }

    @Test
    void getPublicAgentProperties_returnsEmptyPage_whenAgentHasNoProperties() {
        UUID userId = UUID.randomUUID();

        PropertyFilterRequest filter =
                new PropertyFilterRequest();

        Pageable pageable =
                Pageable.ofSize(10);

        AgencyMemberEntity membership =
                AgencyMemberEntity.builder()
                        .id(UUID.randomUUID())
                        .active(true)
                        .build();

        when(userRepository.findAgentMemberByUserId(userId))
                .thenReturn(Optional.of(membership));

        when(propertyRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of())
        );

        Page<PropertyResponse> result =
                service.getPublicAgentProperties(
                        userId,
                        filter,
                        pageable
                );

        assertThat(result)
                .isNotNull();

        assertThat(result.getContent())
                .isEmpty();

        verify(propertyRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );

        verify(propertyMapper, never())
                .toPublicAgencyPropertyResponse(any());
    }

    @Test
    void deleteAgentFromAgency_throws_whenCallerNotOwnerOrSuperAdmin() {
        UUID agentId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Acme Realty")
                .build();

        UserEntity agent = UserEntity.builder()
                .id(agentId)
                .build();

        AgencyMemberEntity membership =
                AgencyMemberEntity.builder()
                        .agency(agency)
                        .user(agent)
                        .active(true)
                        .build();

        UserEntity currentUserEntity =
                UserEntity.builder()
                        .id(currentUserId)
                        .agency(agency)
                        .roles(Set.of(
                                RoleEntity.builder()
                                        .roleName(Role.AGENT)
                                        .build()
                        ))
                        .build();

        CustomUserDetails currentUser =
                CustomUserDetails.from(currentUserEntity);

        when(agencyMemberRepository
                .findByUser_IdAndActiveTrue(agentId))
                .thenReturn(Optional.of(membership));

        when(userRepository.findById(currentUserId))
                .thenReturn(Optional.of(currentUserEntity));

        when(agencyMemberRepository
                .findByAgency_IdAndUser_IdAndActiveTrue(
                        agencyId,
                        currentUserId
                ))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.deleteAgentFromAgency(
                        agentId,
                        currentUser
                )
        )
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(
                        "You don't have permission to perform this procedure."
                );

        verify(propertyRepository, never())
                .unassignAgentFromAllProperties(agentId);

        verify(refreshTokenService, never())
                .revokeAllUserRefreshTokens(agentId);
    }

    @Test
    void deleteAgentFromAgency_throws_whenActiveMembershipNotFound() {
        UUID agentId = UUID.randomUUID();

        when(agencyMemberRepository
                .findByUser_IdAndActiveTrue(agentId))
                .thenReturn(Optional.empty());

        CustomUserDetails currentUser =
                CustomUserDetails.from(
                        UserEntity.builder()
                                .id(UUID.randomUUID())
                                .roles(Set.of())
                                .build()
                );

        assertThatThrownBy(
                () -> service.deleteAgentFromAgency(
                        agentId,
                        currentUser
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "Active agent not found with user id: " + agentId
                );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(propertyRepository);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void deleteAgentFromAgency_allowsSuperAdmin() {
        UUID agentId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID superAdminId = UUID.randomUUID();

        AgencyEntity agency =
                AgencyEntity.builder()
                        .id(agencyId)
                        .name("Acme Realty")
                        .build();

        UserEntity agent =
                UserEntity.builder()
                        .id(agentId)
                        .roles(new HashSet<>())
                        .agency(agency)
                        .build();

        UserEntity superAdmin =
                UserEntity.builder()
                        .id(superAdminId)
                        .roles(Set.of(
                                RoleEntity.builder()
                                        .roleName(Role.SUPER_ADMIN)
                                        .build()
                        ))
                        .build();

        AgencyMemberEntity membership =
                AgencyMemberEntity.builder()
                        .id(UUID.randomUUID())
                        .agency(agency)
                        .user(agent)
                        .active(true)
                        .build();

        CustomUserDetails currentUser =
                CustomUserDetails.from(superAdmin);

        when(agencyMemberRepository
                .findByUser_IdAndActiveTrue(agentId))
                .thenReturn(Optional.of(membership));

        when(userRepository.findById(superAdminId))
                .thenReturn(Optional.of(superAdmin));

        doNothing()
                .when(propertyRepository)
                .unassignAgentFromAllProperties(agentId);

        doNothing()
                .when(refreshTokenService)
                .revokeAllUserRefreshTokens(agentId);

        service.deleteAgentFromAgency(
                agentId,
                currentUser
        );

        assertThat(membership.isActive())
                .isFalse();

        assertThat(membership.getRemovedBy())
                .isEqualTo(superAdmin);

        assertThat(agent.getAgency())
                .isNull();

        assertThat(agent.getRoles())
                .doesNotContain(
                        RoleEntity.builder()
                                .roleName(Role.AGENT)
                                .build()
                );

        verify(propertyRepository)
                .unassignAgentFromAllProperties(agentId);

        verify(refreshTokenService)
                .revokeAllUserRefreshTokens(agentId);
    }

    @Test
    void deleteAgentFromAgency_succeeds_whenCallerIsAgencyOwner() {
        UUID agentId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        AgencyEntity agency =
                AgencyEntity.builder()
                        .id(agencyId)
                        .name("Acme Realty")
                        .build();

        RoleEntity agentRole =
                RoleEntity.builder()
                        .roleName(Role.AGENT)
                        .build();

        RoleEntity anotherRole =
                RoleEntity.builder()
                        .roleName(Role.CLIENT)
                        .build();

        UserEntity agent =
                UserEntity.builder()
                        .id(agentId)
                        .fullName("John Agent")
                        .email("john@example.com")
                        .agency(agency)
                        .roles(new HashSet<>(
                                Set.of(
                                        agentRole,
                                        anotherRole
                                )
                        ))
                        .build();

        UserEntity owner =
                UserEntity.builder()
                        .id(ownerId)
                        .fullName("Agency Owner")
                        .email("owner@acme.com")
                        .agency(agency)
                        .roles(new HashSet<>(
                                Set.of(
                                        RoleEntity.builder()
                                                .roleName(Role.AGENCY_OWNER)
                                                .build()
                                )
                        ))
                        .build();

        AgencyMemberEntity membership =
                AgencyMemberEntity.builder()
                        .id(UUID.randomUUID())
                        .agency(agency)
                        .user(agent)
                        .active(true)
                        .role(Role.AGENT)
                        .build();

        AgencyMemberEntity ownerMembership =
                AgencyMemberEntity.builder()
                        .id(UUID.randomUUID())
                        .agency(agency)
                        .user(owner)
                        .active(true)
                        .role(Role.AGENCY_OWNER)
                        .build();

        CustomUserDetails currentUser =
                CustomUserDetails.from(owner);

        when(agencyMemberRepository
                .findByUser_IdAndActiveTrue(agentId))
                .thenReturn(Optional.of(membership));

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(agencyMemberRepository
                .findByAgency_IdAndUser_IdAndActiveTrue(
                        agencyId,
                        ownerId
                ))
                .thenReturn(Optional.of(ownerMembership));

        doNothing()
                .when(propertyRepository)
                .unassignAgentFromAllProperties(agentId);

        doNothing()
                .when(refreshTokenService)
                .revokeAllUserRefreshTokens(agentId);

        service.deleteAgentFromAgency(
                agentId,
                currentUser
        );

        // Membership lifecycle state
        assertThat(membership.isActive())
                .isFalse();

        assertThat(membership.getRemovedBy())
                .isEqualTo(owner);

        // User agency relationship
        assertThat(agent.getAgency())
                .isNull();

        // Agent role removed
        assertThat(
                agent.getRoles()
                        .stream()
                        .map(RoleEntity::getRoleName)
        )
                .doesNotContain(Role.AGENT);

        // Other roles remain untouched
        assertThat(
                agent.getRoles()
                        .stream()
                        .map(RoleEntity::getRoleName)
        )
                .contains(Role.CLIENT);

        // Properties are unassigned
        verify(propertyRepository)
                .unassignAgentFromAllProperties(agentId);

        // Refresh tokens are revoked
        verify(refreshTokenService)
                .revokeAllUserRefreshTokens(agentId);
    }

    @Test
    void getOwnAssignedProperties_returnsMappedPage_forCurrentAgent() {
        UUID currentUserId = UUID.randomUUID();

        CustomUserDetails currentUser =
                CustomUserDetails.from(
                        UserEntity.builder()
                                .id(currentUserId)
                                .roles(Set.of(
                                        RoleEntity.builder()
                                                .roleName(Role.AGENT)
                                                .build()
                                ))
                                .build()
                );

        PropertyFilterRequest filter =
                new PropertyFilterRequest();

        filter.setCity("Baku");

        Pageable pageable =
                Pageable.ofSize(10);

        PropertyEntity entity =
                PropertyEntity.builder()
                        .id(UUID.randomUUID())
                        .title("Assigned to me")
                        .city("Baku")
                        .build();

        Page<PropertyEntity> entityPage =
                new PageImpl<>(List.of(entity));

        PropertyResponse mappedResponse =
                PropertyResponse.builder()
                        .id(entity.getId())
                        .title(entity.getTitle())
                        .city(entity.getCity())
                        .build();

        when(propertyRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(entityPage);

        when(propertyMapper.toAdminPropertyResponse(entity))
                .thenReturn(mappedResponse);

        Page<PropertyResponse> result =
                service.getOwnAssignedProperties(
                        currentUser,
                        filter,
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().getFirst())
                .isEqualTo(mappedResponse);

        verify(propertyRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );

        verify(propertyMapper)
                .toAdminPropertyResponse(entity);
    }

    @Test
    void getOwnAssignedProperties_returnsEmptyPage_whenAgentHasNoAssignedProperties() {
        CustomUserDetails currentUser =
                CustomUserDetails.from(
                        UserEntity.builder()
                                .id(UUID.randomUUID())
                                .roles(Set.of(
                                        RoleEntity.builder()
                                                .roleName(Role.AGENT)
                                                .build()
                                ))
                                .build()
                );

        PropertyFilterRequest filter =
                new PropertyFilterRequest();

        Pageable pageable =
                Pageable.ofSize(10);

        when(propertyRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of())
        );

        Page<PropertyResponse> result =
                service.getOwnAssignedProperties(
                        currentUser,
                        filter,
                        pageable
                );

        assertThat(result.getContent())
                .isEmpty();

        verify(propertyRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );

        verify(propertyMapper, never())
                .toAdminPropertyResponse(any());
    }

    @Test
    void getOwnAssignedProperties_queriesByCurrentUsersId() {
        UUID currentUserId = UUID.randomUUID();

        CustomUserDetails currentUser =
                CustomUserDetails.from(
                        UserEntity.builder()
                                .id(currentUserId)
                                .roles(Set.of(
                                        RoleEntity.builder()
                                                .roleName(Role.AGENT)
                                                .build()
                                ))
                                .build()
                );

        PropertyFilterRequest filter =
                new PropertyFilterRequest();

        Pageable pageable =
                Pageable.ofSize(10);

        when(propertyRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of())
        );

        service.getOwnAssignedProperties(
                currentUser,
                filter,
                pageable
        );

        verify(propertyRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );
    }

    @Test
    void getOwnInquiries_returnsMappedPage_forCurrentAgent() {
        UUID currentUserId = UUID.randomUUID();

        CustomUserDetails currentUser =
                CustomUserDetails.from(
                        UserEntity.builder()
                                .id(currentUserId)
                                .roles(Set.of(
                                        RoleEntity.builder()
                                                .roleName(Role.AGENT)
                                                .build()
                                ))
                                .build()
                );

        InquiryFilterRequest filter =
                emptyInquiryFilter();

        Pageable pageable =
                Pageable.ofSize(10);

        InquiryEntity inquiryEntity =
                InquiryEntity.builder()
                        .id(UUID.randomUUID())
                        .message(
                                "I am interested in this property"
                        )
                        .build();

        Page<InquiryEntity> inquiryPage =
                new PageImpl<>(List.of(inquiryEntity));

        InquiryResponse inquiryResponse =
                InquiryResponse.builder()
                        .id(inquiryEntity.getId())
                        .message(inquiryEntity.getMessage())
                        .build();

        when(inquiryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(inquiryPage);

        when(inquiryMapper.toResponse(inquiryEntity))
                .thenReturn(inquiryResponse);

        Page<InquiryResponse> result =
                service.getOwnInquiries(
                        currentUser,
                        filter,
                        pageable
                );

        assertThat(result)
                .isNotNull();

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().getFirst())
                .isEqualTo(inquiryResponse);

        verify(inquiryRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );

        verify(inquiryMapper)
                .toResponse(inquiryEntity);
    }

    @Test
    void getOwnInquiries_returnsEmptyPage_whenAgentHasNoInquiries() {
        CustomUserDetails currentUser =
                CustomUserDetails.from(
                        UserEntity.builder()
                                .id(UUID.randomUUID())
                                .roles(Set.of(
                                        RoleEntity.builder()
                                                .roleName(Role.AGENT)
                                                .build()
                                ))
                                .build()
                );

        InquiryFilterRequest filter =
                emptyInquiryFilter();

        Pageable pageable =
                Pageable.ofSize(10);

        when(inquiryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of())
        );

        Page<InquiryResponse> result =
                service.getOwnInquiries(
                        currentUser,
                        filter,
                        pageable
                );

        assertThat(result)
                .isNotNull();

        assertThat(result.getContent())
                .isEmpty();

        verify(inquiryRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );

        verify(inquiryMapper, never())
                .toResponse(any());
    }

    @Test
    void getOwnInquiries_queriesUsingCurrentAgentId() {
        UUID currentUserId = UUID.randomUUID();

        CustomUserDetails currentUser =
                CustomUserDetails.from(
                        UserEntity.builder()
                                .id(currentUserId)
                                .roles(Set.of(
                                        RoleEntity.builder()
                                                .roleName(Role.AGENT)
                                                .build()
                                ))
                                .build()
                );

        InquiryFilterRequest filter =
                emptyInquiryFilter();

        Pageable pageable =
                Pageable.ofSize(10);

        when(inquiryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of())
        );

        service.getOwnInquiries(
                currentUser,
                filter,
                pageable
        );

        verify(inquiryRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );
    }
}