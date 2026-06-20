package com.realestate.backend.mapper.agency;

import com.realestate.backend.dto.agency.response.AgencyMemberResponse;
import com.realestate.backend.entity.AgencyMemberEntity;
import org.springframework.stereotype.Component;

@Component
public class AgencyMemberMapper {

    public AgencyMemberResponse toResponse(AgencyMemberEntity member) {
        if (member == null) {
            return null;
        }

        return AgencyMemberResponse.builder()
                .id(member.getId())
                .agencyId(member.getAgency().getId())
                .agencyName(member.getAgency().getName())
                .userId(member.getUser().getId())
                .userFullName(member.getUser().getFullName())
                .userEmail(member.getUser().getEmail())
                .position(member.getPosition())
                .memberType(member.getMemberType())
                .active(member.isActive())
                .build();
    }
}