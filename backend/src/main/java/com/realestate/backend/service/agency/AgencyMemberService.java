package com.realestate.backend.service.agency;

import com.realestate.backend.dto.agency.request.AgencyMemberAssignmentRequest;
import com.realestate.backend.dto.agency.response.AgencyMemberResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.AgencyMemberType;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.agency.AgencyMemberMapper;
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.AgencyRepository;
import com.realestate.backend.repository.RoleRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgencyMemberService {

    private final AgencyRepository agencyRepository;
    private final AgencyMemberRepository agencyMemberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AgencyMemberMapper agencyMemberMapper;

    @Transactional
    public AgencyMemberResponse assignAgencyAdmin(
            UUID agencyId,
            AgencyMemberAssignmentRequest request,
            CustomUserDetails currentUser
    ) {
        ensureCanAssignAdmin(agencyId, currentUser);

        return assignMember(
                agencyId,
                request,
                Role.AGENCY_ADMIN,
                AgencyMemberType.AGENCY_OWNER,
                "Agency Admin"
        );
    }

    @Transactional
    public AgencyMemberResponse assignAgent(
            UUID agencyId,
            AgencyMemberAssignmentRequest request,
            CustomUserDetails currentUser
    ) {
        ensureCanAssignAgent(agencyId, currentUser);

        return assignMember(
                agencyId,
                request,
                Role.AGENT,
                AgencyMemberType.AGENCY_OWNER,
                "Agent"
        );
    }

    private AgencyMemberResponse assignMember(
            UUID agencyId,
            AgencyMemberAssignmentRequest request,
            Role role,
            AgencyMemberType memberType,
            String defaultPosition
    ) {
        AgencyEntity agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));

        UserEntity targetUser = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found. Ask the user to register first, then assign them to the agency."
                ));

        if (agencyMemberRepository.existsByAgency_IdAndUser_Id(agencyId, targetUser.getId())) {
            throw new ConflictException("User is already a member of this agency");
        }

        RoleEntity roleEntity = roleRepository.findByRoleName(role)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + role));

        targetUser.getRoles().add(roleEntity);
        userRepository.save(targetUser);

        String position = request.getPosition() == null || request.getPosition().isBlank()
                ? defaultPosition
                : request.getPosition().trim();

        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .agency(agency)
                .user(targetUser)
                .position(position)
                .memberType(memberType)
                .active(true)
                .build();

        return agencyMemberMapper.toResponse(agencyMemberRepository.save(member));
    }

    private void ensureCanAssignAdmin(UUID agencyId, CustomUserDetails currentUser) {
        if (hasRole(currentUser)) {
            return;
        }

        boolean isOwner = agencyMemberRepository
                .existsByAgency_IdAndUser_IdAndMemberTypeAndActiveTrue(
                        agencyId,
                        currentUser.getId(),
                        AgencyMemberType.AGENCY_OWNER
                );

        if (!isOwner) {
            throw new ForbiddenException("Only agency owner can assign agency admins");
        }
    }

    private void ensureCanAssignAgent(UUID agencyId, CustomUserDetails currentUser) {
        if (hasRole(currentUser)) {
            return;
        }

        AgencyMemberEntity member = agencyMemberRepository
                .findByAgency_IdAndUser_IdAndActiveTrue(
                        agencyId,
                        currentUser.getId()
                )
                .orElseThrow(() -> new ForbiddenException("You are not a member of this agency"));

        Set<AgencyMemberType> allowedTypes = Set.of(
                AgencyMemberType.AGENCY_OWNER,
                AgencyMemberType.AGENCY_ADMIN
        );

        if (!allowedTypes.contains(member.getMemberType())) {
            throw new ForbiddenException("Only agency owner or admin can assign agents");
        }
    }

    private boolean hasRole(CustomUserDetails currentUser) {
        String authority = "ROLE_" + Role.SUPER_ADMIN.name();

        return currentUser.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority ->
                        Objects.equals(grantedAuthority.getAuthority(), authority)
                );
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}