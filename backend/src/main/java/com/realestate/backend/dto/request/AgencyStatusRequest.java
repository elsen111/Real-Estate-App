package com.realestate.backend.dto.request;

import com.realestate.backend.enums.AgencyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyStatusRequest {

    @NotNull(message = "Status is required")
    AgencyStatus status;

}
