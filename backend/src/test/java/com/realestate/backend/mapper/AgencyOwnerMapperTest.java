package com.realestate.backend.mapper;

import com.realestate.backend.dto.response.AgencyOwnerResponse;
import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.entity.UserMediaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AgencyOwnerMapperTest {

    private AgencyOwnerMapper agencyOwnerMapper;

    @BeforeEach
    void setup() {
        agencyOwnerMapper = Mappers.getMapper(AgencyOwnerMapper.class);
    }

    @Test
    void shouldMapResponse() {

        UserEntity agencyOwner = createAgencyOwner();

        AgencyOwnerResponse response = agencyOwnerMapper.toResponse(agencyOwner);

        assertNotNull(response);
        assertEquals(agencyOwner.getId(), response.getId());
        assertEquals(agencyOwner.getFullName(), response.getFullName());
        assertEquals(agencyOwner.getEmail(), response.getEmail());

        assertEquals(
                agencyOwner.getProfilePhotoUrl().getMedia().getFileUrl(),
                response.getAvatarUrl()
        );

    }

//    HELPER
    private UserEntity createAgencyOwner() {

        MediaFileEntity mediaFile = new MediaFileEntity();
        mediaFile.setFileUrl("https://cdn.test/avatar.png");

        UserMediaEntity userMedia = new UserMediaEntity();
        userMedia.setMedia(mediaFile);

        UserEntity agencyOwner = new UserEntity();
        agencyOwner.setId(UUID.randomUUID());
        agencyOwner.setFullName("Ted Foxes");
        agencyOwner.setEmail("ted@gmail.com");
        agencyOwner.setProfilePhotoUrl(userMedia);

        return agencyOwner;

    }

}
