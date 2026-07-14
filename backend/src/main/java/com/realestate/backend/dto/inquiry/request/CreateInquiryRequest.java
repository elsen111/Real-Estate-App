package com.realestate.backend.dto.inquiry.request;

import com.realestate.backend.enums.InquiryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInquiryRequest {

    @NotNull(message = "Message is required.")
    @Size(min = 10, max = 2000, message = "Message length should be between 10 and 2000 characters")
    private String message;

    @NotNull(message = "Preferred contact method is required.")
    private InquiryType preferredContactMethod;

}
