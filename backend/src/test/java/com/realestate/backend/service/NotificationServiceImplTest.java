package com.realestate.backend.service;

import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AppointmentEntity;
import com.realestate.backend.entity.InquiryEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

class NotificationServiceImplTest {

    private final NotificationServiceImpl service = new NotificationServiceImpl();

    @Test
    void notifyAgencyOfNewInquiry_doesNotThrow_forValidInquiry() {
        PropertyEntity property = PropertyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .name("Acme Realty")
                .build();

        UserEntity client = UserEntity.builder()
                .id(UUID.randomUUID())
                .build();

        InquiryEntity inquiry = InquiryEntity.builder()
                .id(UUID.randomUUID())
                .property(property)
                .agency(agency)
                .client(client)
                .build();

        assertThatCode(() -> service.notifyAgencyOfNewInquiry(inquiry))
                .doesNotThrowAnyException();
    }

    @Test
    void notifyAgencyOfNewAppointment_doesNotThrow_forValidAppointment() {
        PropertyEntity property = PropertyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .name("Acme Realty")
                .build();

        UserEntity client = UserEntity.builder()
                .id(UUID.randomUUID())
                .build();

        AppointmentEntity appointment = AppointmentEntity.builder()
                .id(UUID.randomUUID())
                .property(property)
                .agency(agency)
                .client(client)
                .build();

        assertThatCode(() -> service.notifyAgencyOfNewAppointment(appointment))
                .doesNotThrowAnyException();
    }
}