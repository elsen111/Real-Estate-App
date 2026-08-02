package com.realestate.backend.mapper;

import com.realestate.backend.dto.response.InquiryResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.InquiryEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InquiryMapperTest {

    private InquiryMapper inquiryMapper;

    @BeforeEach
    void setup() {
        inquiryMapper = Mappers.getMapper(InquiryMapper.class);
    }

    @Test
    void shouldMapResponse() {

        InquiryEntity inquiry = createInquiryEntity();

        InquiryResponse response = inquiryMapper.toResponse(inquiry);

        assertNotNull(response);

        assertEquals(inquiry.getId(), response.getId());

        assertEquals(
                inquiry.getProperty().getId(),
                response.getPropertyId()
        );

        assertEquals(
                inquiry.getProperty().getTitle(),
                response.getPropertyTitle()
        );

        assertEquals(
                inquiry.getClient().getId(),
                response.getClientId()
        );

        assertEquals(
                inquiry.getClient().getFullName(),
                response.getClientFullName()
        );

        assertEquals(
                inquiry.getClient().getPhoneNumber(),
                response.getClientPhone()
        );

        assertEquals(
                inquiry.getClient().getEmail(),
                response.getClientEmail()
        );

        assertEquals(
                inquiry.getAssignedAgent().getId(),
                response.getAssignedAgentId()
        );

        assertEquals(
                inquiry.getAgency().getId(),
                response.getAgencyId()
        );
    }

    // HELPER
    private InquiryEntity createInquiryEntity() {

        PropertyEntity property = new PropertyEntity();
        property.setId(UUID.randomUUID());
        property.setTitle("Luxury Villa");

        UserEntity client = new UserEntity();
        client.setId(UUID.randomUUID());
        client.setFullName("John Smith");
        client.setPhoneNumber("+994501112233");
        client.setEmail("john.smith@gmail.com");

        UserEntity assignedAgent = new UserEntity();
        assignedAgent.setId(UUID.randomUUID());
        assignedAgent.setFullName("Ted Foxes");

        AgencyEntity agency = new AgencyEntity();
        agency.setId(UUID.randomUUID());
        agency.setName("Dream Estate");

        InquiryEntity inquiry = new InquiryEntity();
        inquiry.setId(UUID.randomUUID());
        inquiry.setProperty(property);
        inquiry.setClient(client);
        inquiry.setAssignedAgent(assignedAgent);
        inquiry.setAgency(agency);

        return inquiry;
    }

}