package com.realestate.backend.dto.request;

import com.realestate.backend.enums.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInquiryStatusRequest {

    @NotNull(message = "Status is required")
    private InquiryStatus status;

}
