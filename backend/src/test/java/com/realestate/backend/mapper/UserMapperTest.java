package com.realestate.backend.mapper;

import com.realestate.backend.dto.response.AgentResponse;
import com.realestate.backend.dto.response.AuthUserResponse;
import com.realestate.backend.dto.response.UserResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setup() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    void shouldMapSummary_withRolesPositionAndAvatar() {

        UserEntity user = createUserEntity();

        AuthUserResponse response = userMapper.toSummary(user);

        assertNotNull(response);

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getFullName(), response.getFullName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(user.getEnabled(), response.isEnabled());
        assertEquals(user.isEmailVerified(), response.isEmailVerified());
        assertEquals(Set.of("AGENT", "CLIENT"), response.getRoles());

        assertEquals("Agent", response.getPosition());
        assertEquals("https://cdn/avatar.jpg", response.getAvatarUrl());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
        assertEquals(user.getUpdatedAt(), response.getUpdatedAt());
    }

    @Test
    void shouldMapSummary_whenRolesAndProfilePhotoAreNull() {

        UserEntity user = createUserEntity();
        user.setRoles(null);
        user.setProfilePhotoUrl(null);

        AuthUserResponse response = userMapper.toSummary(user);

        assertTrue(response.getRoles().isEmpty());
        assertNull(response.getPosition());
        assertNull(response.getAvatarUrl());
    }

    @Test
    void shouldMapSummary_whenProfilePhotoMediaIsNull() {

        UserEntity user = createUserEntity();
        UserMediaEntity photoWithoutMedia = new UserMediaEntity();
        photoWithoutMedia.setId(UUID.randomUUID());
        photoWithoutMedia.setMedia(null);
        user.setProfilePhotoUrl(photoWithoutMedia);

        AuthUserResponse response = userMapper.toSummary(user);

        assertNull(response.getAvatarUrl());
    }

    @Test
    void shouldMapAdminResponse_withAgencyNameAndPosition() {

        UserEntity user = createUserEntity();

        UserResponse response = userMapper.toAdminResponse(user);

        assertNotNull(response);

        assertEquals("Baku Realty", response.getAgency());
        assertEquals(Set.of("AGENT", "CLIENT"), response.getRoles());
        assertEquals("Agent", response.getPosition());
        assertEquals("https://cdn/avatar.jpg", response.getAvatarUrl());
        assertEquals(user.getEnabled(), response.getEnabled());
        assertEquals(user.isEmailVerified(), response.getEmailVerified());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void shouldMapAdminResponse_whenAgencyIsNull() {

        UserEntity user = createUserEntity();
        user.setAgency(null);

        UserResponse response = userMapper.toAdminResponse(user);

        assertNull(response.getAgency());
    }

    @Test
    void shouldMapAgentResponse_sameAsAdminResponse() {

        UserEntity user = createUserEntity();

        UserResponse response = userMapper.toAgentResponse(user);

        assertNotNull(response);
        assertEquals(user.getFullName(), response.getFullName());
        assertEquals("Baku Realty", response.getAgency());
        assertEquals(Set.of("AGENT", "CLIENT"), response.getRoles());
        assertEquals("Agent", response.getPosition());
        assertEquals("https://cdn/avatar.jpg", response.getAvatarUrl());
    }

    @Test
    void shouldMapAgentWithUserIdResponse_fromAgencyMember() {

        UserEntity user = createUserEntity();
        AgencyEntity memberAgency = createAgencyEntity("Sumgait Realty");
        AgencyMemberEntity member = new AgencyMemberEntity();
        member.setId(UUID.randomUUID());
        member.setUser(user);
        member.setAgency(memberAgency);
        member.setCreatedAt(LocalDateTime.now());

        AgentResponse response = userMapper.toAgentWithUserIdResponse(member);

        assertNotNull(response);

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getPhoneNumber(), response.getPhoneNumber());
        assertEquals("Sumgait Realty", response.getAgency());
        assertEquals(memberAgency.getId(), response.getAgencyId());
        assertEquals(member.getId(), response.getMemberId());
        assertEquals(Set.of("AGENT", "CLIENT"), response.getRoles());
        assertEquals("Agent", response.getPosition());
        assertEquals("https://cdn/avatar.jpg", response.getAvatarUrl());
        assertEquals(member.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void shouldMapAgentWithUserIdResponse_whenMemberAgencyIsNull() {

        UserEntity user = createUserEntity();
        AgencyMemberEntity member = new AgencyMemberEntity();
        member.setId(UUID.randomUUID());
        member.setUser(user);
        member.setAgency(null);

        AgentResponse response = userMapper.toAgentWithUserIdResponse(member);

        assertNull(response.getAgency());
        assertNull(response.getAgencyId());
    }

    @Test
    void shouldMapAgentWithUserIdResponse_whenUserIsNull() {

        AgencyMemberEntity member = new AgencyMemberEntity();
        member.setId(UUID.randomUUID());
        member.setUser(null);
        member.setAgency(createAgencyEntity("Sumgait Realty"));

        AgentResponse response = userMapper.toAgentWithUserIdResponse(member);

        assertNull(response.getId());
        assertNull(response.getPhoneNumber());
        assertTrue(response.getRoles().isEmpty());
        assertNull(response.getPosition());
        assertNull(response.getAvatarUrl());
    }

    @Test
    void shouldResolvePosition_pickingHighestPriorityRole() {

        UserEntity user = new UserEntity();
        user.setRoles(Set.of(
                createRoleEntity(Role.CLIENT),
                createRoleEntity(Role.AGENCY_OWNER),
                createRoleEntity(Role.AGENT)));

        assertEquals("Agency Owner", userMapper.resolvePosition(user));
    }

    @Test
    void shouldResolvePosition_returnNull_whenRolesAreNullOrEmpty() {

        UserEntity userWithNullRoles = new UserEntity();
        userWithNullRoles.setRoles(null);
        assertNull(userMapper.resolvePosition(userWithNullRoles));

        UserEntity userWithEmptyRoles = new UserEntity();
        userWithEmptyRoles.setRoles(Set.of());
        assertNull(userMapper.resolvePosition(userWithEmptyRoles));
    }

    @Test
    void shouldMapRoleNames_returnEmptySet_whenRolesAreNull() {

        UserEntity user = new UserEntity();
        user.setRoles(null);

        assertTrue(userMapper.toRoleNames(user).isEmpty());
    }

    @Test
    void shouldMapRoleNames_toEnumNameStrings() {

        UserEntity user = new UserEntity();
        user.setRoles(Set.of(createRoleEntity(Role.ADMIN)));

        assertEquals(Set.of("ADMIN"), userMapper.toRoleNames(user));
    }

    @Test
    void shouldReturnNull_whenSourceArgumentsAreNull() {

        assertNull(userMapper.toSummary(null));
        assertNull(userMapper.toAdminResponse(null));
        assertNull(userMapper.toAgentResponse(null));
        assertNull(userMapper.toAgentWithUserIdResponse(null));
    }

    // HELPERS
    private RoleEntity createRoleEntity(Role name) {

        RoleEntity role = new RoleEntity();
        role.setId(UUID.randomUUID());
        role.setRoleName(name);

        return role;
    }

    private AgencyEntity createAgencyEntity(String name) {

        AgencyEntity agency = new AgencyEntity();
        agency.setId(UUID.randomUUID());
        agency.setName(name);

        return agency;
    }

    private UserEntity createUserEntity() {

        UserEntity user = new UserEntity();

        user.setId(UUID.randomUUID());
        user.setFullName("Elshan Hasanov");
        user.setEmail("elshan@example.com");
        user.setPhoneNumber("+994501112233");
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRoles(Set.of(createRoleEntity(Role.AGENT), createRoleEntity(Role.CLIENT)));
        user.setAgency(createAgencyEntity("Baku Realty"));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setProfilePhotoUrl(createProfilePhoto("https://cdn/avatar.jpg"));

        return user;
    }

    private UserMediaEntity createProfilePhoto(String url) {

        MediaFileEntity media = new MediaFileEntity();
        media.setId(UUID.randomUUID());
        media.setFileUrl(url);

        UserMediaEntity profilePhoto = new UserMediaEntity();
        profilePhoto.setId(UUID.randomUUID());
        profilePhoto.setMedia(media);

        return profilePhoto;
    }
}