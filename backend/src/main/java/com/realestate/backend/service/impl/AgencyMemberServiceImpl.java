package com.realestate.backend.service.impl;

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
import com.realestate.backend.service.AgencyMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgencyMemberServiceImpl implements AgencyMemberService {

    private final AgencyMemberRepository agencyMemberRepository;
    private final AgencyMemberMapper agencyMemberMapper;

    private final AgencyRepository agencyRepository;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public AgencyMemberResponse assignAgent(
            UUID agencyId,
            UUID userId,
            CustomUserDetails currentUser
    ) {
        ensureCanAssignAgent(agencyId, currentUser);

        return assignMember(
                agencyId,
                userId,
                Role.AGENT,
                "Agent"
        );
    }

    @Override
    @Transactional
    public AgencyMemberResponse assignMember(
            UUID agencyId,
            UUID userId,
            Role role,
            String defaultPosition
    ) {
        AgencyEntity agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found"));

        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found. Ask the user to register first, then assign them to the agency."
                ));

        if (targetUser.getAgency() != null) {
            throw new ConflictException("User is already assigned to an agency ("
                    + targetUser.getAgency().getName() + ") and cannot join another.");
        }

        if (agencyMemberRepository.existsByUser_IdAndActiveTrue(targetUser.getId())) {
            throw new ConflictException("An active agency membership record already exists for this user.");
        }

        if (agencyMemberRepository.existsByAgency_IdAndUser_Id(agencyId, targetUser.getId())) {
            throw new ConflictException("User is already a member of this agency");
        }

        RoleEntity roleEntity = roleRepository.findByRoleName(role)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + role));

        targetUser.getRoles().add(roleEntity);
        targetUser.setAgency(agency);
        userRepository.save(targetUser);

        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .agency(agency)
                .user(targetUser)
                .active(true)
                .build();

        return agencyMemberMapper.toResponse(agencyMemberRepository.save(member));
    }


    //    HELPER METHODS
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

        Set<Role> allowedTypes = Set.of(
                Role.AGENCY_OWNER
        );

        if (!hasAnyRole(member.getUser(),
                Set.of(Role.AGENCY_OWNER))) {

            throw new ForbiddenException(
                    "Only agency owners can assign agents"
            );
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

    private boolean hasAnyRole(UserEntity user, Set<Role> roles) {
        return user.getRoles()
                .stream()
                .map(RoleEntity::getRoleName)
                .anyMatch(roles::contains);
    }

}