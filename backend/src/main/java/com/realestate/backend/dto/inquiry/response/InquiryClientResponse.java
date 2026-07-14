package com.realestate.backend.dto.inquiry.response;

import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.enums.InquiryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class InquiryClientResponse {

    private UUID id;
    private UUID propertyId;
    private UUID agencyId;
    private UUID assignedAgentId;
    private UUID clientId;
    private String clientPhone;
    private String clientEmail;
    private String propertyTitle;
    private String clientFullName;
    private String message;
    private InquiryType preferredContactMethod;
    private InquiryStatus status;
    private LocalDateTime createdAt;

}
