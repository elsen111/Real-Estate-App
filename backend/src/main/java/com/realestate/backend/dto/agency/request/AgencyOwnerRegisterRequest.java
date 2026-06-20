package com.realestate.backend.dto.agency.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgencyOwnerRegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be at most 150 characters")
    private String ownerFullName;

    @NotBlank(message = "Agency owner email is required")
    @Email(message = "Agency owner email format is invalid")
    @Size(max = 150, message = "Agency owner email must be at most 150 characters")
    private String ownerEmail;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password name must be between 8 and 72 characters")
    private String password;

    @Size(max = 30, message = "Phone number must be at most 30 characters")
    private String ownerPhoneNumber;

    @NotBlank(message = "Agency email is required")
    @Email(message = "Agency email  format is invalid")
    @Size(max = 150, message = "Agency email  must be at most 150 characters")
    private String agencyEmail;

    @NotBlank(message = "Agency name is required")
    @Size(max = 150, message = "Agency name must be at most 150 characters")
    private String agencyName;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String agencyDescription;

    @NotBlank(message = "Phone number is required")
    @Size(max = 30, message = "Phone number must be at most 30 characters")
    private String agencyPhoneNumber;

    @Size(max = 255, message = "Website must be at most 255 characters")
    private String agencyWebsite;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address be at most 255 characters")
    private String address;

}
