package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({
        "id",
        "name",
        "description",
        "status",
        "phoneNumber",
        "email",
        "website",
        "logoUrl",
        "city",
        "address",
        "isDeleted",
        "owner",
        "subscription",
        "properties",
        "statistics",
        "createdAt",
        "updatedAt"
})
public class AdminAgencyResponse {

    private UUID id;
    private String name;
    private String description;
    private AgencyStatus status;
    private String phoneNumber;
    private String email;
    private String website;
    private String logoUrl;
    private String city;
    private String address;
    private Boolean isDeleted;
    private AgencyOwnerResponse owner;
    private AgencySubscriptionResponse subscription;
    private List<AdminAgencyPropertyResponse> properties;
    private AgencyStatisticsResponse statistics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
