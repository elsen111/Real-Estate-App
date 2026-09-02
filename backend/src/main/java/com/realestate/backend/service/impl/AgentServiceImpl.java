package com.realestate.backend.service.impl;


import com.realestate.backend.dto.request.InquiryFilterRequest;
import com.realestate.backend.dto.response.AgentResponse;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.response.InquiryResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.PropertyStatus;
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
import com.realestate.backend.repository.specification.InquirySpecification;
import com.realestate.backend.repository.specification.PropertySpecification;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    private final AgencyMemberRepository agencyMemberRepository;

    private final RefreshTokenServiceImpl refreshTokenService;
    private final InquiryRepository inquiryRepository;
    private final InquiryMapper inquiryMapper;


    @Override
    public AgentResponse getAgentByUserId(UUID userId) {

        AgencyMemberEntity member = userRepository.findAgentMemberByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with user id: " + userId));

        return userMapper.toAgentWithUserIdResponse(member);

    }

    @Override
    public Page<PropertyResponse> getPublicAgentProperties(UUID userId, PropertyFilterRequest filter, Pageable pageable) {

        userRepository.findAgentMemberByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with user id: " + userId));

        Specification<PropertyEntity> specification = PropertySpecification.withPublicFilter(filter)
                .and(PropertySpecification.hasAssignedAgentId(userId));

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toPublicAgencyPropertyResponse);
    }

    @Override
    public Page<PropertyResponse> getOwnAssignedProperties(
            CustomUserDetails currentUser,
            PropertyFilterRequest filter,
            Pageable pageable
    ) {

        Specification<PropertyEntity> specification = PropertySpecification
                .withFilter(filter)
                .and(PropertySpecification.hasAssignedAgentId(currentUser.getId()))
                .and(PropertySpecification.hasStatusIn(
                        List.of(
                                PropertyStatus.PENDING,
                                PropertyStatus.ACTIVE,
                                PropertyStatus.SOLD,
                                PropertyStatus.RENTED
                        )
                ));

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toAdminPropertyResponse);

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

        AgencyEntity agency = membership.getAgency();

        agent.setAgency(null);
        agent.getRoles().removeIf(role -> role.getRoleName() == Role.AGENT);
        userRepository.save(agent);

        log.atInfo()
                .setMessage("Agent has been removed from agency.")
                .addKeyValue("agentId", agent.getId())
                .addKeyValue("agentEmail", agent.getEmail())
                .addKeyValue("agencyId",agency.getId())
                .addKeyValue("agencyName", agency.getName())
                .log();

    }

    @Override
    public Page<InquiryResponse> getOwnInquiries(
            CustomUserDetails currentUser,
            InquiryFilterRequest filter,
            Pageable pageable
    ) {

        Specification<InquiryEntity> specification = InquirySpecification.withAgentFilter(
                currentUser.getId(),
                filter
        );

        return inquiryRepository.findAll(specification, pageable)
                .map(inquiryMapper::toResponse);

    }


    //    HELPER METHODS
    private void ensureCanRemoveAgent(UUID agencyId, CustomUserDetails currentUser) {
        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            return;
        }

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        if (user.getAgency() == null ||
                !user.getAgency().getId().equals(agencyId)) {
            throw new ForbiddenException(
                    "You don't have permission to remove the agent belonging to another agency."
            );
        }

        agencyMemberRepository
                .findByAgency_IdAndUser_IdAndActiveTrue(agencyId, currentUser.getId())
                .filter(member -> member.getUser().getRoles().stream()
                        .anyMatch(r -> r.getRoleName() == Role.AGENCY_OWNER))
                .orElseThrow(() -> new ForbiddenException(
                        "Only the agency's owner or a super admin can remove this agent"));
    }
}
