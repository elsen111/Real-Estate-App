package com.realestate.backend.service;

import com.realestate.backend.dto.response.AgencyMemberResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.AgencyMemberMapper;
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.AgencyRepository;
import com.realestate.backend.repository.RoleRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.AgencyMemberServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyMemberServiceImplTest {

    @Mock
    private AgencyMemberRepository agencyMemberRepository;

    @Mock
    private AgencyMemberMapper agencyMemberMapper;

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AgencyMemberServiceImpl service;

    @Test
    void assignAgent_throws_whenCallerNotOwnerOrSuperAdmin() {
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserEntity currentUserEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .roles(Set.of(
                        RoleEntity.builder()
                                .roleName(Role.AGENT)
                                .build()
                ))
                .build();

        CustomUserDetails currentUser =
                CustomUserDetails.from(currentUserEntity);

        when(
                agencyMemberRepository
                        .findByAgency_IdAndUser_IdAndActiveTrue(
                                agencyId,
                                currentUser.getId()
                        )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.assignAgent(
                        agencyId,
                        userId,
                        currentUser
                )
        )
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You are not a member of this agency");
    }

    @Test
    void assignAgent_succeeds_whenCallerIsAgencyOwner() {
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assignerId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Acme Realty")
                .build();

        UserEntity assigner = UserEntity.builder()
                .id(assignerId)
                .fullName("Agency Owner")
                .email("owner@acme.com")
                .roles(new HashSet<>())
                .build();

        assigner.getRoles().add(
                RoleEntity.builder()
                        .roleName(Role.AGENCY_OWNER)
                        .build()
        );

        UserEntity targetUser = UserEntity.builder()
                .id(userId)
                .fullName("John Agent")
                .email("john@example.com")
                .roles(new HashSet<>())
                .build();

        CustomUserDetails currentUser =
                CustomUserDetails.from(assigner);

        AgencyMemberEntity existingMembership = AgencyMemberEntity.builder()
                .agency(agency)
                .user(assigner)
                .active(true)
                .role(Role.AGENCY_OWNER)
                .build();

        AgencyMemberEntity savedMember = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(targetUser)
                .active(true)
                .role(Role.AGENT)
                .addedBy(assigner)
                .build();

        AgencyMemberResponse response =
                AgencyMemberResponse.builder()
                        .build();

        RoleEntity agentRole = RoleEntity.builder()
                .roleName(Role.AGENT)
                .build();

        when(
                agencyMemberRepository
                        .findByAgency_IdAndUser_IdAndActiveTrue(
                                agencyId,
                                assignerId
                        )
        ).thenReturn(Optional.of(existingMembership));

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(targetUser));

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.of(assigner));

        when(
                agencyMemberRepository
                        .existsByUser_IdAndActiveTrue(userId)
        ).thenReturn(false);

        when(
                agencyMemberRepository
                        .existsByAgency_IdAndUser_Id(
                                agencyId,
                                userId
                        )
        ).thenReturn(false);

        when(roleRepository.findByRoleName(Role.AGENT))
                .thenReturn(Optional.of(agentRole));

        when(agencyMemberRepository.save(any(AgencyMemberEntity.class)))
                .thenReturn(savedMember);

        when(agencyMemberMapper.toResponse(savedMember))
                .thenReturn(response);

        AgencyMemberResponse result =
                service.assignAgent(
                        agencyId,
                        userId,
                        currentUser
                );

        assertThat(result).isSameAs(response);

        assertThat(targetUser.getAgency())
                .isEqualTo(agency);

        assertThat(targetUser.getRoles())
                .contains(agentRole);

        ArgumentCaptor<AgencyMemberEntity> captor =
                ArgumentCaptor.forClass(AgencyMemberEntity.class);

        verify(agencyMemberRepository).save(captor.capture());

        AgencyMemberEntity createdMember = captor.getValue();

        assertThat(createdMember.getAgency())
                .isEqualTo(agency);

        assertThat(createdMember.getUser())
                .isEqualTo(targetUser);

        assertThat(createdMember.getRole())
                .isEqualTo(Role.AGENT);

        assertThat(createdMember.getAddedBy())
                .isEqualTo(assigner);

        assertThat(createdMember.isActive())
                .isTrue();
    }

    @Test
    void assignMember_throws_whenAgencyDoesNotExist() {
        UUID assignerId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.assignMember(
                        assignerId,
                        agencyId,
                        userId,
                        Role.AGENT,
                        "Agent"
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Agency not found");

        verifyNoInteractions(userRepository);
    }

    @Test
    void assignMember_throws_whenTargetUserDoesNotExist() {
        UUID assignerId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Acme")
                .build();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.assignMember(
                        assignerId,
                        agencyId,
                        userId,
                        Role.AGENT,
                        "Agent"
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "User not found. Ask the user to register first, then assign them to the agency."
                );
    }

    @Test
    void assignMember_throws_whenTargetUserAlreadyInAnotherAgency() {
        UUID assignerId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Acme")
                .build();

        AgencyEntity otherAgency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .name("Other Agency")
                .build();

        UserEntity target = UserEntity.builder()
                .id(userId)
                .agency(otherAgency)
                .build();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(target));

        assertThatThrownBy(
                () -> service.assignMember(
                        assignerId,
                        agencyId,
                        userId,
                        Role.AGENT,
                        "Agent"
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Other Agency");
    }

    @Test
    void assignMember_throws_whenUserAlreadyHasActiveMembership() {
        UUID assignerId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Acme")
                .build();

        UserEntity target = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>())
                .build();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(target));

        when(
                agencyMemberRepository
                        .existsByUser_IdAndActiveTrue(userId)
        ).thenReturn(true);

        assertThatThrownBy(
                () -> service.assignMember(
                        assignerId,
                        agencyId,
                        userId,
                        Role.AGENT,
                        "Agent"
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "An active agency membership record already exists for this user."
                );
    }

    @Test
    void assignMember_throws_whenUserAlreadyMemberOfSameAgency() {
        UUID assignerId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Acme")
                .build();

        UserEntity target = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>())
                .build();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(target));

        when(
                agencyMemberRepository
                        .existsByUser_IdAndActiveTrue(userId)
        ).thenReturn(false);

        when(
                agencyMemberRepository
                        .existsByAgency_IdAndUser_Id(
                                agencyId,
                                userId
                        )
        ).thenReturn(true);

        assertThatThrownBy(
                () -> service.assignMember(
                        assignerId,
                        agencyId,
                        userId,
                        Role.AGENT,
                        "Agent"
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage("User is already a member of this agency");
    }

    @Test
    void assignMember_throws_whenAssignerDoesNotExist() {
        UUID assignerId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Acme")
                .build();

        UserEntity target = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>())
                .build();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(target));

        when(
                agencyMemberRepository
                        .existsByUser_IdAndActiveTrue(userId)
        ).thenReturn(false);

        when(
                agencyMemberRepository
                        .existsByAgency_IdAndUser_Id(
                                agencyId,
                                userId
                        )
        ).thenReturn(false);

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.assignMember(
                        assignerId,
                        agencyId,
                        userId,
                        Role.AGENT,
                        "Agent"
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: " + assignerId);
    }

    @Test
    void assignMember_throws_whenRoleDoesNotExist() {
        UUID assignerId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Acme")
                .build();

        UserEntity target = UserEntity.builder()
                .id(userId)
                .roles(new HashSet<>())
                .build();

        UserEntity assigner = UserEntity.builder()
                .id(assignerId)
                .fullName("Agency Owner")
                .build();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(target));

        when(
                agencyMemberRepository
                        .existsByUser_IdAndActiveTrue(userId)
        ).thenReturn(false);

        when(
                agencyMemberRepository
                        .existsByAgency_IdAndUser_Id(
                                agencyId,
                                userId
                        )
        ).thenReturn(false);

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.of(assigner));

        when(roleRepository.findByRoleName(Role.AGENT))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.assignMember(
                        assignerId,
                        agencyId,
                        userId,
                        Role.AGENT,
                        "Agent"
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Role not found: " + Role.AGENT);
    }

    @Test
    void assignMember_succeeds_whenTargetUserIsFree() {
        UUID assignerId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Acme")
                .build();

        UserEntity assigner = UserEntity.builder()
                .id(assignerId)
                .fullName("Agency Owner")
                .email("owner@acme.com")
                .roles(new HashSet<>())
                .build();

        UserEntity target = UserEntity.builder()
                .id(userId)
                .fullName("John Agent")
                .email("john@example.com")
                .roles(new HashSet<>())
                .build();

        RoleEntity agentRole = RoleEntity.builder()
                .roleName(Role.AGENT)
                .build();

        AgencyMemberEntity savedMember = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(target)
                .role(Role.AGENT)
                .addedBy(assigner)
                .active(true)
                .build();

        AgencyMemberResponse response =
                AgencyMemberResponse.builder()
                        .build();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(target));

        when(
                agencyMemberRepository
                        .existsByUser_IdAndActiveTrue(userId)
        ).thenReturn(false);

        when(
                agencyMemberRepository
                        .existsByAgency_IdAndUser_Id(
                                agencyId,
                                userId
                        )
        ).thenReturn(false);

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.of(assigner));

        when(roleRepository.findByRoleName(Role.AGENT))
                .thenReturn(Optional.of(agentRole));

        when(
                agencyMemberRepository.save(
                        any(AgencyMemberEntity.class)
                )
        ).thenReturn(savedMember);

        when(agencyMemberMapper.toResponse(savedMember))
                .thenReturn(response);

        AgencyMemberResponse result =
                service.assignMember(
                        assignerId,
                        agencyId,
                        userId,
                        Role.AGENT,
                        "Agent"
                );

        assertThat(result)
                .isSameAs(response);

        assertThat(target.getAgency())
                .isEqualTo(agency);

        assertThat(target.getRoles())
                .contains(agentRole);

        verify(agencyMemberRepository)
                .save(any(AgencyMemberEntity.class));
    }

    @Test
    void assignMember_setsMembershipRoleAndAddedBy() {
        UUID assignerId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Acme")
                .build();

        UserEntity assigner = UserEntity.builder()
                .id(assignerId)
                .fullName("Agency Owner")
                .email("owner@acme.com")
                .build();

        UserEntity target = UserEntity.builder()
                .id(userId)
                .fullName("John Agent")
                .email("john@example.com")
                .roles(new HashSet<>())
                .build();

        RoleEntity agentRole = RoleEntity.builder()
                .roleName(Role.AGENT)
                .build();

        AgencyMemberEntity savedMember = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(target)
                .role(Role.AGENT)
                .addedBy(assigner)
                .active(true)
                .build();

        AgencyMemberResponse response =
                AgencyMemberResponse.builder()
                        .build();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(target));

        when(
                agencyMemberRepository
                        .existsByUser_IdAndActiveTrue(userId)
        ).thenReturn(false);

        when(
                agencyMemberRepository
                        .existsByAgency_IdAndUser_Id(
                                agencyId,
                                userId
                        )
        ).thenReturn(false);

        when(userRepository.findById(assignerId))
                .thenReturn(Optional.of(assigner));

        when(roleRepository.findByRoleName(Role.AGENT))
                .thenReturn(Optional.of(agentRole));

        when(
                agencyMemberRepository.save(
                        any(AgencyMemberEntity.class)
                )
        ).thenReturn(savedMember);

        when(agencyMemberMapper.toResponse(savedMember))
                .thenReturn(response);

        service.assignMember(
                assignerId,
                agencyId,
                userId,
                Role.AGENT,
                "Agent"
        );

        ArgumentCaptor<AgencyMemberEntity> captor =
                ArgumentCaptor.forClass(AgencyMemberEntity.class);

        verify(agencyMemberRepository)
                .save(captor.capture());

        AgencyMemberEntity createdMember =
                captor.getValue();

        assertThat(createdMember.getAgency())
                .isEqualTo(agency);

        assertThat(createdMember.getUser())
                .isEqualTo(target);

        assertThat(createdMember.getRole())
                .isEqualTo(Role.AGENT);

        assertThat(createdMember.getAddedBy())
                .isEqualTo(assigner);

        assertThat(createdMember.getRemovedBy())
                .isNull();

        assertThat(createdMember.isActive())
                .isTrue();
    }
}