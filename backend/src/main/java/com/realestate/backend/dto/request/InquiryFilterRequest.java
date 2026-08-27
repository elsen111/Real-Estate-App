package com.realestate.backend.dto.request;

import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.enums.InquiryType;

import java.time.LocalDateTime;
import java.util.UUID;

public record InquiryFilterRequest(
        InquiryStatus status,
        UUID agentId,
        String agentName,
        UUID clientId,
        String clientEmail,
        String clientName,
        UUID propertyId,
        String propertyTitle,
        InquiryType contact,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
