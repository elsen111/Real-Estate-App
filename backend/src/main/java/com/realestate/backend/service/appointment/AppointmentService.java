package com.realestate.backend.service.appointment;

import com.realestate.backend.dto.appointment.request.CreateAppointmentRequest;
import com.realestate.backend.dto.appointment.response.AppointmentResponse;
import com.realestate.backend.security.CustomUserDetails;

import java.util.UUID;

public interface AppointmentService {

    AppointmentResponse createAppointment(UUID propertyId, CreateAppointmentRequest request, CustomUserDetails currentUser);

}
