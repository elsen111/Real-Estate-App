package com.realestate.backend.mapper;

import com.realestate.backend.dto.response.AgencyMemberResponse;
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

/**
 * Unit tests for {@link AgencyMemberMapper}.
 * <p>
 * {@code AgencyMemberMapper} is a MapStruct {@code @Mapper} interface: the actual
 * implementation ({@code AgencyMemberMapperImpl}) is generated at compile time by
 * the MapStruct annotation processor. It cannot be instantiated with {@code new}
 * (it - like any interface - is reported as "abstract"), so tests obtain an
 * instance via {@link Mappers#getMapper(Class)}, which is MapStruct's standard,
 * Spring-context-free way of resolving the generated implementation.
 */
class AgencyMemberMapperTest {

    private final AgencyMemberMapper mapper = Mappers.getMapper(AgencyMemberMapper.class);

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
            roleEntities.add(RoleEntity.builder().roleName(role).build());
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
        AgencyMemberResponse response = mapper.toResponse(null);

        assertThat(response).isNull();
    }

    @Test
    void toResponse_mapsAllFieldsCorrectly_forActiveMember() {
        AgencyEntity agency = buildAgency();
        UserEntity user = buildUser(Role.AGENT);
        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(true)
                .build();

        AgencyMemberResponse response = mapper.toResponse(member);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(member.getId());
        assertThat(response.getAgencyId()).isEqualTo(agency.getId());
        assertThat(response.getAgencyName()).isEqualTo("Prime Realty");
        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getUserFullName()).isEqualTo("Jane Agent");
        assertThat(response.getUserEmail()).isEqualTo("jane.agent@example.com");
        assertThat(response.getPosition()).isEqualTo("AGENT");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void toResponse_mapsInactiveMember_withActiveFalse() {
        AgencyEntity agency = buildAgency();
        UserEntity user = buildUser(Role.AGENCY_OWNER);
        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(false)
                .build();

        AgencyMemberResponse response = mapper.toResponse(member);

        assertThat(response.isActive()).isFalse();
        assertThat(response.getPosition()).isEqualTo("AGENCY_OWNER");
    }

    @Test
    void toResponse_joinsMultipleRoles_intoCommaSeparatedPosition() {
        AgencyEntity agency = buildAgency();

        UserEntity user = buildUser(Role.AGENCY_OWNER, Role.AGENT);
        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(true)
                .build();

        AgencyMemberResponse response = mapper.toResponse(member);

        assertThat(response.getPosition()).isEqualTo("AGENCY_OWNER, AGENT");
    }

    @Test
    void toResponse_doesNotFilterSuperAdminRole() {
        AgencyEntity agency = buildAgency();
        UserEntity user = buildUser(Role.SUPER_ADMIN);
        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(true)
                .build();

        AgencyMemberResponse response = mapper.toResponse(member);

        assertThat(response.getPosition()).isEqualTo("SUPER_ADMIN");
    }

    @Test
    void toResponse_setsPositionNull_whenUserHasNoRoles() {
        AgencyEntity agency = buildAgency();
        UserEntity user = buildUser();
        AgencyMemberEntity member = AgencyMemberEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .user(user)
                .active(true)
                .build();

        AgencyMemberResponse response = mapper.toResponse(member);

        assertThat(response.getPosition()).isNull();
    }

    @Test
    void mapRolesToPosition_returnsNull_whenRolesSetIsNull() {
        assertThat(mapper.mapRolesToPosition(null)).isNull();
    }

    @Test
    void mapRolesToPosition_returnsNull_whenRolesSetIsEmpty() {
        assertThat(mapper.mapRolesToPosition(Set.of())).isNull();
    }

    @Test
    void mapRolesToPosition_returnsSingleRoleName_forSingleRole() {
        Set<RoleEntity> roles = Set.of(RoleEntity.builder().roleName(Role.CLIENT).build());

        assertThat(mapper.mapRolesToPosition(roles)).isEqualTo("CLIENT");
    }

    @Test
    void mapRolesToPosition_joinsMultipleRoleNames_withCommaAndSpace() {
        Set<RoleEntity> roles = new LinkedHashSet<>();
        roles.add(RoleEntity.builder().roleName(Role.AGENT).build());
        roles.add(RoleEntity.builder().roleName(Role.LANDLORD).build());

        assertThat(mapper.mapRolesToPosition(roles)).isEqualTo("AGENT, LANDLORD");
    }

}