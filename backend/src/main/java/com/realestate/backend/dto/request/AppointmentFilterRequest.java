package com.realestate.backend.dto.request;

import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.AppointmentType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentFilterRequest(
        UUID propertyId,
        String propertyTitle,
        UUID clientId,
        String clientName,
        String clientEmail,
        UUID agentId,
        String agentName,
        UUID agencyId,
        String agencyName,
        AppointmentType type,
        AppointmentStatus status,
        LocalDateTime createdAfter,
        LocalDateTime createdBefore
) {
}
