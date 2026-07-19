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

        log.info("New inquiry {} received for property {} — notifying agency {}",
                inquiry.getId(), inquiry.getProperty().getId(), inquiry.getAgency().getId());

    }

    @Override
    public void notifyAgencyOfNewAppointment(AppointmentEntity appointment) {
        log.info("New appointment {} received for property {} — notifying agency {}",
                appointment.getId(), appointment.getProperty().getId(), appointment.getAgency().getId());
    }
}
