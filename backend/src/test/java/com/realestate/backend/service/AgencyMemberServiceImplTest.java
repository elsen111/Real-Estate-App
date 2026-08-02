package com.realestate.backend.service;

import com.realestate.backend.dto.response.AgencyMemberResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.mapper.AgencyMemberMapper;
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.AgencyRepository;
import com.realestate.backend.repository.RoleRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.AgencyMemberServiceImpl;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyMemberServiceImplTest {

    @Mock private AgencyMemberRepository agencyMemberRepository;
    @Mock private AgencyMemberMapper agencyMemberMapper;
    @Mock private AgencyRepository agencyRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    @InjectMocks private AgencyMemberServiceImpl service;

    @Test
    void assignAgent_throws_whenCallerNotOwnerOrSuperAdmin() {
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CustomUserDetails currentUser = CustomUserDetails.from(
                UserEntity.builder().id(UUID.randomUUID()).roles(Set.of(
                        RoleEntity.builder().roleName(Role.AGENT).build())).build());

        when(agencyMemberRepository.findByAgency_IdAndUser_IdAndActiveTrue(agencyId, currentUser.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignAgent(agencyId, userId, currentUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void assignMember_throws_whenTargetUserAlreadyInAnotherAgency() {
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(agencyId).name("Acme").build();
        AgencyEntity otherAgency = AgencyEntity.builder().id(UUID.randomUUID()).name("Other Agency").build();
        UserEntity target = UserEntity.builder().id(userId).agency(otherAgency).build();

        when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));
        when(userRepository.findById(userId)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> service.assignMember(agencyId, userId, Role.AGENT, "Agent"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Other Agency");
    }

    @Test
    void assignMember_succeeds_whenTargetUserIsFree() {
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(agencyId).name("Acme").build();
        UserEntity target = UserEntity.builder().id(userId).roles(new HashSet<>()).build();
        RoleEntity agentRole = RoleEntity.builder().roleName(Role.AGENT).build();
        AgencyMemberEntity savedMember = AgencyMemberEntity.builder().agency(agency).user(target).active(true).build();

        when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));
        when(userRepository.findById(userId)).thenReturn(Optional.of(target));
        when(agencyMemberRepository.existsByUser_IdAndActiveTrue(userId)).thenReturn(false);
        when(agencyMemberRepository.existsByAgency_IdAndUser_Id(agencyId, userId)).thenReturn(false);
        when(roleRepository.findByRoleName(Role.AGENT)).thenReturn(Optional.of(agentRole));
        when(agencyMemberRepository.save(any())).thenReturn(savedMember);
        when(agencyMemberMapper.toResponse(savedMember)).thenReturn(AgencyMemberResponse.builder().build());

        AgencyMemberResponse response = service.assignMember(agencyId, userId, Role.AGENT, "Agent");

        assertThat(response).isNotNull();
        assertThat(target.getAgency()).isEqualTo(agency);
        assertThat(target.getRoles()).contains(agentRole);
    }
}