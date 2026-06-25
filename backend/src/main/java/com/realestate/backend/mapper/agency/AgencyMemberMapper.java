package com.realestate.backend.mapper.agency;

import com.realestate.backend.dto.agency.response.AgencyMemberResponse;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class AgencyMemberMapper {

    public AgencyMemberResponse toResponse(AgencyMemberEntity member) {
        if (member == null) {
            return null;
        }

        Role role = member.getUser()
                .getRoles()
                .stream()
                .filter(r -> r.getRoleName() != Role.SUPER_ADMIN)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Role not found"))
                .getRoleName();

        return AgencyMemberResponse.builder()
                .id(member.getId())
                .agencyId(member.getAgency().getId())
                .agencyName(member.getAgency().getName())
                .userId(member.getUser().getId())
                .userFullName(member.getUser().getFullName())
                .userEmail(member.getUser().getEmail())
                .position(role.getLabel())
                .active(member.isActive())
                .build();
    }
}