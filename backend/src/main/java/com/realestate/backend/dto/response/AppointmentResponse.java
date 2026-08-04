package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.AppointmentType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({
        "id",
        "status",
        "appointmentType",
        "preferredDateTime",
        "confirmedDateTime",
        "note",
        "responseNote",
        "propertyId",
        "propertyTitle",
        "clientId",
        "clientFullName",
        "clientPhone",
        "clientEmail",
        "agencyId",
        "agentId",
        "createdAt",
        "updatedAt"
})
public class AppointmentResponse {

    private UUID id;
    private AppointmentStatus status;
    private AppointmentType appointmentType;
    private LocalDateTime preferredDateTime;
    private LocalDateTime confirmedDateTime;
    private String note;
    private String responseNote;
    private UUID propertyId;
    private String propertyTitle;
    private UUID clientId;
    private String clientFullName;
    private String clientPhone;
    private String clientEmail;
    private UUID agencyId;
    private UUID agentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
