package com.realestate.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateAppointmentRequest {

    @NotNull(message = "Message is required.")
    @Size(min = 10, max = 2000, message = "Message length should be between 10 and 2000 characters")
    private String note;

    @NotNull(message = "Preferred date time is required.")
    private LocalDateTime preferredDateTime;

}
