package com.realestate.backend.dto.request;

import com.realestate.backend.enums.PropertyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyStatusRequest {

    @NotNull(message = "Status is required")
    PropertyStatus status;

}
