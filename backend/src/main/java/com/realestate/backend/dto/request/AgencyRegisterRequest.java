package com.realestate.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgencyRegisterRequest {

    @NotBlank(message = "Agency name is required")
    @Size(max = 150, message = "Agency name must be at most 150 characters")
    private String agencyName;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String agencyDescription;

    @NotBlank(message = "Agency email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String agencyBusinessEmail;

    @NotBlank(message = "Agency phone number is required")
    @Size(max = 30, message = "Phone number must be at most 30 characters")
    private String agencyBusinessPhone;

    @Size(max = 255, message = "Website must be at most 255 characters")
    private String agencyWebsiteUrl;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be at most 100 characters")
    private String agencyCity;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must be at most 255 characters")
    private String agencyAddress;

}
