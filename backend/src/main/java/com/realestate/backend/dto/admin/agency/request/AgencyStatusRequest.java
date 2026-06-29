package com.realestate.backend.dto.admin.agency.request;

import com.realestate.backend.enums.AgencyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AgencyStatusRequest {

    @NotNull(message = "Status is required")
    AgencyStatus status;

}
