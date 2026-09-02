package com.realestate.backend.service.impl;

import com.realestate.backend.entity.AppointmentEntity;
import com.realestate.backend.entity.InquiryEntity;
import com.realestate.backend.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void notifyAgencyOfNewInquiry(InquiryEntity inquiry) {

        log.atInfo()
                .setMessage("Notifying agency for the new inquiry")
                .addKeyValue("inquiryId", inquiry.getId())
                .addKeyValue("propertyId", inquiry.getProperty().getId())
                .addKeyValue("agencyId",  inquiry.getAgency().getId())
                .addKeyValue("agencyName", inquiry.getAgency().getName())
                .addKeyValue("clientId", inquiry.getClient().getId())
                .log();

    }

    @Override
    public void notifyAgencyOfNewAppointment(AppointmentEntity appointment) {

        log.atInfo()
                .setMessage("Notifying agency for the new appointment")
                .addKeyValue("appointmentId", appointment.getId())
                .addKeyValue("propertyId", appointment.getProperty().getId())
                .addKeyValue("agencyId",  appointment.getAgency().getId())
                .addKeyValue("agencyName", appointment.getAgency().getName())
                .addKeyValue("clientId", appointment.getClient().getId())
                .log();
    }
}
