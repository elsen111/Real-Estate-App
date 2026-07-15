package com.realestate.backend.dto.appointment.request;

import com.realestate.backend.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAppointmentStatusRequest {

    @NotNull(message = "Status is required.")
    private AppointmentStatus status;

    @Size(max = 2000, message = "Message length should be between 10 and 2000 characters")
    private String responseNote;

}
