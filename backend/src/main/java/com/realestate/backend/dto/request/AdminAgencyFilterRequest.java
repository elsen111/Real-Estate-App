package com.realestate.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminAgencyFilterRequest extends AgencyFilterRequest {

    private String name;
    private String city;
    private String email;
    private Boolean status;
    private Boolean isDeleted;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;

}
