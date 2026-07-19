package com.realestate.backend.dto.response;

import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.AppointmentType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AppointmentResponse {

    private UUID id;
    private UUID propertyId;
    private UUID clientId;
    private UUID agencyId;
    private UUID agentId;
    private String clientPhone;
    private String clientEmail;
    private String propertyTitle;
    private String clientFullName;
    private String note;
    private String responseNote;
    private AppointmentType appointmentType;
    private LocalDateTime preferredDateTime;
    private LocalDateTime confirmedDateTime;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
