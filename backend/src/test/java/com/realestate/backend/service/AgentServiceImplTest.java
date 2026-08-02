package com.realestate.backend.service;

import com.realestate.backend.entity.*;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.AgentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private AgencyMemberRepository agencyMemberRepository;

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
        AgencyEntity agency = AgencyEntity.builder().id(agencyId).build();
        AgencyMemberEntity membership = AgencyMemberEntity.builder()
                .agency(agency).user(UserEntity.builder().id(agentId).roles(Set.of()).build()).active(true).build();
        CustomUserDetails currentUser = CustomUserDetails.from(
                UserEntity.builder().id(UUID.randomUUID()).roles(Set.of(
                        RoleEntity.builder().roleName(Role.AGENT).build())).build());

        when(agencyMemberRepository.findByUser_IdAndActiveTrue(agentId)).thenReturn(Optional.of(membership));
        when(agencyMemberRepository.findByAgency_IdAndUser_IdAndActiveTrue(agencyId, currentUser.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteAgentFromAgency(agentId, currentUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteAgentFromAgency_throws_whenActiveMembershipNotFound() {
        UUID agentId = UUID.randomUUID();
        when(agencyMemberRepository.findByUser_IdAndActiveTrue(agentId)).thenReturn(Optional.empty());
        CustomUserDetails currentUser = CustomUserDetails.from(UserEntity.builder().id(UUID.randomUUID()).roles(Set.of()).build());

        assertThatThrownBy(() -> service.deleteAgentFromAgency(agentId, currentUser))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}