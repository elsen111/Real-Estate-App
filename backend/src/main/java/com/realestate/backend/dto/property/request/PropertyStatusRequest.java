package com.realestate.backend.dto.property.request;

import com.realestate.backend.enums.PropertyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PropertyStatusRequest {

    @NotNull(message = "Status is required")
    PropertyStatus status;

}
