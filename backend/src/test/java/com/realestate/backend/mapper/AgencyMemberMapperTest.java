package com.realestate.backend.mapper;

import com.realestate.backend.dto.response.AgencyMemberResponse;
import com.realestate.backend.dto.response.UserSummaryResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AgencyMemberMapperTest {

    private final AgencyMemberMapper mapper =
            Mappers.getMapper(AgencyMemberMapper.class);

    private AgencyEntity buildAgency() {
        return AgencyEntity.builder()
                .id(UUID.randomUUID())
                .name("Prime Realty")
                .description("A leading agency")
                .phoneNumber("+994501234567")
                .email("contact@primerealty.com")
                .city("Baku")
                .address("Nizami Street 12")
                .build();
    }

    private UserEntity buildUser(Role... roles) {
        Set<RoleEntity> roleEntities = new LinkedHashSet<>();

        for (Role role : roles) {
            roleEntities.add(
                    RoleEntity.builder()
                            .roleName(role)
                            .build()
            );
        }

        return UserEntity.builder()
                .id(UUID.randomUUID())
                .fullName("Jane Agent")
                .email("jane.agent@example.com")
                .passwordHash("hashed-password")
                .roles(roleEntities)
                .build();
    }

    @Test
    void toResponse_returnsNull_whenMemberIsNull() {

        AgencyMemberResponse response =
                mapper.toResponse(null);

        assertThat(response).isNull();
    }

    @Test
    void toResponse_mapsAllFieldsCorrectly_forActiveMember() {

        AgencyEntity agency = buildAgency();

        UserEntity user = buildUser(Role.AGENT);

        UserEntity addedBy = UserEntity.builder()
                .id(UUID.randomUUID())
                .fullName("Agency Owner")
                .email("owner@primerealty.com")
                .build();

        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(true)
                .role(Role.AGENT)
                .addedBy(addedBy)
                .build();

        AgencyMemberResponse response =
                mapper.toResponse(member);

        assertThat(response).isNotNull();

        assertThat(response.getId())
                .isEqualTo(member.getId());

        assertThat(response.getAgencyId())
                .isEqualTo(agency.getId());

        assertThat(response.getAgencyName())
                .isEqualTo("Prime Realty");

        assertThat(response.getUserId())
                .isEqualTo(user.getId());

        assertThat(response.getUserFullName())
                .isEqualTo("Jane Agent");

        assertThat(response.getUserEmail())
                .isEqualTo("jane.agent@example.com");

        assertThat(response.getPosition())
                .isEqualTo("AGENT");

        assertThat(response.getRole())
                .isEqualTo(Role.AGENT);

        assertThat(response.isActive())
                .isTrue();

        assertThat(response.getAddedBy())
                .isNotNull();

        assertThat(response.getAddedBy().id())
                .isEqualTo(addedBy.getId());

        assertThat(response.getAddedBy().fullName())
                .isEqualTo("Agency Owner");

        assertThat(response.getAddedBy().email())
                .isEqualTo("owner@primerealty.com");

        assertThat(response.getRemovedBy())
                .isNull();
    }

    @Test
    void toResponse_mapsInactiveMember_withActiveFalse() {

        AgencyEntity agency = buildAgency();

        UserEntity user =
                buildUser(Role.AGENCY_OWNER);

        UserEntity removedBy = UserEntity.builder()
                .id(UUID.randomUUID())
                .fullName("Super Admin")
                .email("admin@example.com")
                .build();

        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(false)
                .role(Role.AGENCY_OWNER)
                .removedBy(removedBy)
                .build();

        AgencyMemberResponse response =
                mapper.toResponse(member);

        assertThat(response)
                .isNotNull();

        assertThat(response.isActive())
                .isFalse();

        assertThat(response.getRole())
                .isEqualTo(Role.AGENCY_OWNER);

        assertThat(response.getPosition())
                .isEqualTo("AGENCY_OWNER");

        assertThat(response.getAddedBy())
                .isNull();

        assertThat(response.getRemovedBy())
                .isNotNull();

        assertThat(response.getRemovedBy().id())
                .isEqualTo(removedBy.getId());

        assertThat(response.getRemovedBy().fullName())
                .isEqualTo("Super Admin");

        assertThat(response.getRemovedBy().email())
                .isEqualTo("admin@example.com");
    }

    @Test
    void toResponse_mapsAddedByAndRemovedBy_whenBothArePresent() {

        AgencyEntity agency = buildAgency();

        UserEntity user =
                buildUser(Role.AGENT);

        UserEntity addedBy = UserEntity.builder()
                .id(UUID.randomUUID())
                .fullName("Agency Owner")
                .email("owner@example.com")
                .build();

        UserEntity removedBy = UserEntity.builder()
                .id(UUID.randomUUID())
                .fullName("Super Admin")
                .email("admin@example.com")
                .build();

        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(false)
                .role(Role.AGENT)
                .addedBy(addedBy)
                .removedBy(removedBy)
                .build();

        AgencyMemberResponse response =
                mapper.toResponse(member);

        assertThat(response.getAddedBy())
                .isNotNull();

        assertThat(response.getAddedBy().id())
                .isEqualTo(addedBy.getId());

        assertThat(response.getAddedBy().fullName())
                .isEqualTo(addedBy.getFullName());

        assertThat(response.getAddedBy().email())
                .isEqualTo(addedBy.getEmail());

        assertThat(response.getRemovedBy())
                .isNotNull();

        assertThat(response.getRemovedBy().id())
                .isEqualTo(removedBy.getId());

        assertThat(response.getRemovedBy().fullName())
                .isEqualTo(removedBy.getFullName());

        assertThat(response.getRemovedBy().email())
                .isEqualTo(removedBy.getEmail());
    }

    @Test
    void toResponse_returnsNullAuditUsers_whenAddedByAndRemovedByAreNull() {

        AgencyEntity agency = buildAgency();

        UserEntity user =
                buildUser(Role.AGENT);

        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(true)
                .role(Role.AGENT)
                .addedBy(null)
                .removedBy(null)
                .build();

        AgencyMemberResponse response =
                mapper.toResponse(member);

        assertThat(response)
                .isNotNull();

        assertThat(response.getAddedBy())
                .isNull();

        assertThat(response.getRemovedBy())
                .isNull();
    }

    @Test
    void toResponse_joinsMultipleRoles_intoCommaSeparatedPosition() {

        AgencyEntity agency = buildAgency();

        UserEntity user =
                buildUser(
                        Role.AGENCY_OWNER,
                        Role.AGENT
                );

        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(true)
                .role(Role.AGENT)
                .build();

        AgencyMemberResponse response =
                mapper.toResponse(member);

        assertThat(response.getPosition())
                .isEqualTo("AGENCY_OWNER, AGENT");
    }

    @Test
    void toResponse_doesNotFilterSuperAdminRole() {

        AgencyEntity agency = buildAgency();

        UserEntity user =
                buildUser(Role.SUPER_ADMIN);

        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(true)
                .role(Role.SUPER_ADMIN)
                .build();

        AgencyMemberResponse response =
                mapper.toResponse(member);

        assertThat(response.getPosition())
                .isEqualTo("SUPER_ADMIN");

        assertThat(response.getRole())
                .isEqualTo(Role.SUPER_ADMIN);
    }

    @Test
    void toResponse_setsPositionNull_whenUserHasNoRoles() {

        AgencyEntity agency = buildAgency();

        UserEntity user =
                buildUser();

        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(true)
                .role(Role.CLIENT)
                .build();

        AgencyMemberResponse response =
                mapper.toResponse(member);

        assertThat(response.getPosition())
                .isNull();

        assertThat(response.getRole())
                .isEqualTo(Role.CLIENT);
    }

    @Test
    void mapRolesToPosition_returnsNull_whenRolesSetIsNull() {

        assertThat(
                mapper.mapRolesToPosition(null)
        ).isNull();
    }

    @Test
    void mapRolesToPosition_returnsNull_whenRolesSetIsEmpty() {

        assertThat(
                mapper.mapRolesToPosition(Set.of())
        ).isNull();
    }

    @Test
    void mapRolesToPosition_returnsSingleRoleName_forSingleRole() {

        Set<RoleEntity> roles = Set.of(
                RoleEntity.builder()
                        .roleName(Role.CLIENT)
                        .build()
        );

        assertThat(
                mapper.mapRolesToPosition(roles)
        ).isEqualTo("CLIENT");
    }

    @Test
    void mapRolesToPosition_joinsMultipleRoleNames_withCommaAndSpace() {

        Set<RoleEntity> roles = new LinkedHashSet<>();

        roles.add(
                RoleEntity.builder()
                        .roleName(Role.AGENT)
                        .build()
        );

        roles.add(
                RoleEntity.builder()
                        .roleName(Role.LANDLORD)
                        .build()
        );

        assertThat(
                mapper.mapRolesToPosition(roles)
        ).isEqualTo("AGENT, LANDLORD");
    }

    @Test
    void toUserSummary_mapsUserFieldsCorrectly() {

        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .fullName("John Admin")
                .email("john.admin@example.com")
                .passwordHash("hashed-password")
                .build();

        UserSummaryResponse response =
                mapper.toUserSummary(user);

        assertThat(response)
                .isNotNull();

        assertThat(response.id())
                .isEqualTo(userId);

        assertThat(response.fullName())
                .isEqualTo("John Admin");

        assertThat(response.email())
                .isEqualTo("john.admin@example.com");
    }

    @Test
    void toUserSummary_returnsNull_whenUserIsNull() {

        UserSummaryResponse response =
                mapper.toUserSummary(null);

        assertThat(response)
                .isNull();
    }
}