package com.realestate.backend.mapper;

import com.realestate.backend.dto.response.AdminAgencyResponse;
import com.realestate.backend.dto.response.AgencyResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencyMediaEntity;
import com.realestate.backend.entity.MediaFileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AgencyMapperTest {

    private AgencyMapper agencyMapper;

    @BeforeEach
    void setup() {
        agencyMapper = Mappers.getMapper(AgencyMapper.class);
    }

    @Test
    void shouldMapAgencyOwnerResponse() {
        AgencyEntity agency = createAgency();

        AgencyResponse response = agencyMapper.toAgencyOwnerResponse(agency);

        assertNotNull(response);
        assertEquals(agency.getId(), response.getId());
        assertEquals(agency.getName(), response.getName());

        assertEquals(
                agency.getLogoUrl().getMedia().getFileUrl(),
                response.getLogoUrl()
        );

    }

    @Test
    void shouldMapAdminResponse() {

        AgencyEntity agency = createAgency();

        AdminAgencyResponse response = agencyMapper.toAdminResponse(agency);

        assertNotNull(response);

        assertEquals(agency.getId(), response.getId());
        assertEquals(agency.getName(), response.getName());

        assertEquals(
                agency.getLogoUrl().getMedia().getFileUrl(),
                response.getLogoUrl()
        );

        assertNull(response.getProperties());

    }

    @Test
    void shouldMapPublicAgencyListItem() {

        AgencyEntity agency = createAgency();

        AgencyResponse response = agencyMapper.toPublicAgencyListItem(agency);
        assertNotNull(response);
        assertEquals(agency.getId(), response.getId());
        assertEquals(agency.getName(), response.getName());

        assertEquals(
                agency.getLogoUrl().getMedia().getFileUrl(),
                response.getLogoUrl()
        );

        assertNull(response.getStatus());

    }

    @Test
    void shouldMapPublicAgencyResponseWithTotalAgents() {

        AgencyEntity agency = createAgency();

        AgencyResponse response = agencyMapper.toPublicAgencyResponse(agency, 8);

        assertNotNull(response);

        assertEquals(agency.getId(), response.getId());
        assertEquals(agency.getName(), response.getName());

        assertEquals(8, response.getTotalAgents());

        assertEquals(
                agency.getLogoUrl().getMedia().getFileUrl(),
                response.getLogoUrl()
        );

        assertNull(response.getStatus());

    }

    @Test
    void shouldMapPropertyResponse() {

        AgencyEntity agency = createAgency();

        AgencyResponse response = agencyMapper.toPropertyResponse(agency);

        assertNotNull(response);
        assertEquals(agency.getId(), response.getId());
        assertEquals(agency.getName(), response.getName());

        assertEquals(
                agency.getLogoUrl().getMedia().getFileUrl(),
                response.getLogoUrl()
        );

    }

//    HELPER
    private AgencyEntity createAgency() {

        MediaFileEntity mediaFile = new MediaFileEntity();
        mediaFile.setFileUrl("\"https://cdn.test/logo.png\"");

        AgencyMediaEntity agencyMedia = new AgencyMediaEntity();
        agencyMedia.setMedia(mediaFile);

        AgencyEntity agency = new AgencyEntity();
        agency.setId(UUID.randomUUID());
        agency.setName("Dream Estate");
        agency.setLogoUrl(agencyMedia);

        return agency;

    }

}
