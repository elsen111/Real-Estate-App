package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.enums.InquiryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({
        "id",
        "status",
        "message",
        "preferredContactMethod",
        "propertyId",
        "propertyTitle",
        "clientId",
        "clientPhone",
        "clientEmail",
        "clientFullName",
        "agencyId",
        "assignedAgentId",
        "createdAt"
})
public class InquiryResponse {

    private UUID id;
    private InquiryStatus status;
    private String message;
    private InquiryType preferredContactMethod;
    private UUID propertyId;
    private String propertyTitle;
    private UUID clientId;
    private String clientPhone;
    private String clientEmail;
    private String clientFullName;
    private UUID agencyId;
    private UUID assignedAgentId;
    private LocalDateTime createdAt;

}
