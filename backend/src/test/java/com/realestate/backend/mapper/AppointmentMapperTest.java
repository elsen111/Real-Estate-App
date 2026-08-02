package com.realestate.backend.mapper;

import com.realestate.backend.dto.response.AppointmentResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AppointmentEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AppointmentMapperTest {

    private AppointmentMapper appointmentMapper;

    @BeforeEach
    void setup() {
        appointmentMapper = Mappers.getMapper(AppointmentMapper.class);
    }

    @Test
    void shouldMapResponse() {

        AppointmentEntity appointmentEntity = createAppointmentEntity();

        AppointmentResponse response = appointmentMapper.toResponse(appointmentEntity);

        assertNotNull(response);

        assertEquals(appointmentEntity.getId(), response.getId());

        assertEquals(
                appointmentEntity.getProperty().getId(),
                response.getPropertyId()
        );

        assertEquals(
                appointmentEntity.getProperty().getTitle(),
                response.getPropertyTitle()
        );

        assertEquals(
                appointmentEntity.getClient().getId(),
                response.getClientId()
        );

        assertEquals(
                appointmentEntity.getClient().getFullName(),
                response.getClientFullName()
        );

        assertEquals(
                appointmentEntity.getClient().getPhoneNumber(),
                response.getClientPhone()
        );

        assertEquals(
                appointmentEntity.getClient().getEmail(),
                response.getClientEmail()
        );

        assertEquals(
                appointmentEntity.getAgent().getId(),
                response.getAgentId()
        );

        assertEquals(
                appointmentEntity.getAgency().getId(),
                response.getAgencyId()
        );

    }

    //    HELPER
    private AppointmentEntity createAppointmentEntity() {

        PropertyEntity property = new PropertyEntity();
        property.setId(UUID.randomUUID());
        property.setTitle("Luxury Villa");

        UserEntity client = new UserEntity();
        client.setId(UUID.randomUUID());
        client.setFullName("John Smith");
        client.setPhoneNumber("+994501112233");
        client.setEmail("john.smith@gmail.com");

        UserEntity agent = new UserEntity();
        agent.setId(UUID.randomUUID());
        agent.setFullName("Ted Foxes");

        AgencyEntity agency = new AgencyEntity();
        agency.setId(UUID.randomUUID());
        agency.setName("Dream Estate");

        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setId(UUID.randomUUID());
        appointment.setProperty(property);
        appointment.setClient(client);
        appointment.setAgent(agent);
        appointment.setAgency(agency);

        return appointment;
    }

}
