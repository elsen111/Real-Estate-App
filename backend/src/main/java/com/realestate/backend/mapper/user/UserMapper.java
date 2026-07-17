package com.realestate.backend.mapper.user;

import com.realestate.backend.dto.agent.response.AgentResponse;
import com.realestate.backend.dto.user.response.UserResponse;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface UserMapper {

    List<Role> ROLE_PRIORITY_LIST = List.of(
            Role.SUPER_ADMIN,
            Role.ADMIN,
            Role.AGENCY_OWNER,
            Role.AGENT,
            Role.LANDLORD,
            Role.CLIENT
    );

    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "roles", source = "user", qualifiedByName = "toRoleNames")
    @Mapping(target = "position", source = "user", qualifiedByName = "resolvePosition")
    @Mapping(target = "avatarUrl", source = "profilePhotoUrl.media.fileUrl")
    com.realestate.backend.dto.auth.response.UserResponse toSummary(UserEntity user);

    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "agency", source = "agency.name")
    @Mapping(target = "roles", source = "user", qualifiedByName = "toRoleNames")
    @Mapping(target = "position", source = "user", qualifiedByName = "resolvePosition")
    @Mapping(target = "avatarUrl", source = "profilePhotoUrl.media.fileUrl")
    UserResponse toAdminResponse(UserEntity user);

    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "agency", source = "agency.name")
    @Mapping(target = "roles", source = "user", qualifiedByName = "toRoleNames")
    @Mapping(target = "position", source = "user", qualifiedByName = "resolvePosition")
    @Mapping(target = "avatarUrl", source = "profilePhotoUrl.media.fileUrl")
    UserResponse toAgentResponse(UserEntity user);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "agency", source = "agency.name")
    @Mapping(target = "roles", source = "user", qualifiedByName = "toRoleNames")
    @Mapping(target = "position", source = "user", qualifiedByName = "resolvePosition")
    @Mapping(target = "agencyId", source = "agency.id")
    @Mapping(target = "memberId", source = "id")
    @Mapping(target = "avatarUrl", source = "user.profilePhotoUrl.media.fileUrl")
    AgentResponse toAgentWithUserIdResponse(AgencyMemberEntity agencyMember);

    default Page<UserResponse> toAdminResponse(Page<UserEntity> users) {
        return users.map(this::toAdminResponse);
    }

    @Named("toRoleNames")
    default Set<String> toRoleNames(UserEntity user) {
        if (user.getRoles() == null) return Set.of();
        return user.getRoles()
                .stream()
                .map(RoleEntity::getRoleName)
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Named("resolvePosition")
    default String resolvePosition(UserEntity user) {
        if (user.getRoles() == null) return null;
        Set<Role> roles = user.getRoles()
                .stream()
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toSet());

        return ROLE_PRIORITY_LIST.stream()
                .filter(roles::contains)
                .findFirst()
                .map(Role::getLabel)
                .orElse(null);
    }
}
