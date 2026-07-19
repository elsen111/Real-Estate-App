package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.realestate.backend.enums.AgencyStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminAgencyResponse {

    private UUID id;
    private String name;
    private String description;
    private String phoneNumber;
    private String email;
    private String website;
    private String logoUrl;
    private String city;
    private String address;
    private AgencyStatus status;
    private boolean isDeleted;
    private AgencyOwnerResponse owner;
    private AgencySubscriptionResponse subscription;
    private List<AdminAgencyPropertyResponse> properties;
    private AgencyStatisticsResponse statistics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
