package com.realestate.backend.service.notification;

import com.realestate.backend.entity.AppointmentEntity;
import com.realestate.backend.entity.InquiryEntity;

public interface NotificationService {

    void notifyAgencyOfNewInquiry(InquiryEntity inquiry);

    void notifyAgencyOfNewAppointment(AppointmentEntity appointment);

}
