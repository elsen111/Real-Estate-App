package com.realestate.backend.service;

import com.realestate.backend.dto.request.CreateAppointmentRequest;
import com.realestate.backend.dto.request.UpdateAppointmentStatusRequest;
import com.realestate.backend.dto.response.AppointmentResponse;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AppointmentService {

    AppointmentResponse createAppointment(UUID propertyId, CreateAppointmentRequest request, CustomUserDetails currentUser);

    Page<AppointmentResponse> getClientAppointments(CustomUserDetails currentUser, AppointmentStatus status, Pageable pageable);

    void cancelAppointment(UUID appointmentId, CustomUserDetails currentUser);

    Page<AppointmentResponse> getMyAgencyAppointments(CustomUserDetails currentUser, AppointmentStatus status, UUID propertyId, Pageable pageable);

    AppointmentResponse updateStatus(CustomUserDetails currentUser, UUID inquiryId, UpdateAppointmentStatusRequest request);

}
