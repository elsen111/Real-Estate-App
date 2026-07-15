package com.realestate.backend.service.appointment;

import com.realestate.backend.dto.appointment.request.CreateAppointmentRequest;
import com.realestate.backend.dto.appointment.response.AppointmentResponse;
import com.realestate.backend.dto.inquiry.response.InquiryResponse;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AppointmentService {

    AppointmentResponse createAppointment(UUID propertyId, CreateAppointmentRequest request, CustomUserDetails currentUser);

    Page<AppointmentResponse> getClientAppointments(CustomUserDetails currentUser, AppointmentStatus status, Pageable pageable);

}
