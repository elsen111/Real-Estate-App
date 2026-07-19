package com.realestate.backend.service.impl;


import com.realestate.backend.dto.response.AgentResponse;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.mapper.UserMapper;
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.repository.specification.PropertySpecification;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    private final AgencyMemberRepository agencyMemberRepository;

    private final RefreshTokenServiceImpl refreshTokenService;


    @Override
    public AgentResponse getAgentByUserId(UUID userId) {

        AgencyMemberEntity member = userRepository.findAgentMemberByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with user id: " + userId));

        return userMapper.toAgentWithUserIdResponse(member);

    }

    @Override
    public Page<PropertyResponse> getAgentProperties(UUID userId, PropertyFilterRequest filter, Pageable pageable) {

        userRepository.findAgentMemberByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with user id: " + userId));

        Specification<PropertyEntity> specification = PropertySpecification
                .hasAssignedAgentId(userId);

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toPublicAgencyPropertyResponse);
    }

    @Override
    @Transactional
    public void deleteAgentFromAgency(UUID agentId, CustomUserDetails currentUser) {

        AgencyMemberEntity membership = agencyMemberRepository
                .findByUser_IdAndActiveTrue(agentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active agent not found with id: " + agentId));

        ensureCanRemoveAgent(membership.getAgency().getId(), currentUser);

        UserEntity agent = membership.getUser();

        propertyRepository.unassignAgentFromAllProperties(agent.getId());

        refreshTokenService.revokeAllUserRefreshTokens(agent.getId());

        membership.setActive(false);
        agencyMemberRepository.save(membership);

        agent.setAgency(null);
        agent.getRoles().removeIf(role -> role.getRoleName() == Role.AGENT);
        userRepository.save(agent);

    }



//    HELPER METHODS
    private void ensureCanRemoveAgent(UUID agencyId, CustomUserDetails currentUser) {
        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            return;
        }

        agencyMemberRepository
                .findByAgency_IdAndUser_IdAndActiveTrue(agencyId, currentUser.getId())
                .filter(member -> member.getUser().getRoles().stream()
                        .anyMatch(r -> r.getRoleName() == Role.AGENCY_OWNER))
                .orElseThrow(() -> new ForbiddenException(
                        "Only the agency's owner or a super admin can remove this agent"));
    }
}
