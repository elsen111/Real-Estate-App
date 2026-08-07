package com.realestate.backend.mapper;

import com.realestate.backend.dto.response.AgencyMemberResponse;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AgencyMemberMapper {

    @Mapping(target = "agencyId", source = "agency.id")
    @Mapping(target = "agencyName", source = "agency.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "position", source = "user.roles", qualifiedByName = "mapRolesToPosition")
    AgencyMemberResponse toResponse(AgencyMemberEntity agencyMember);

    @Named("mapRolesToPosition")
    default String mapRolesToPosition(Set<RoleEntity> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.joining(", "));
    }

}