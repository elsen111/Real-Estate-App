package com.realestate.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAgencyRequest {

    @NotBlank(message = "Agency name is required.")
    @Size(max = 150, message = "Agency name must not exceed 150 characters.")
    private String name;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotBlank(message = "Phone number is required.")
    @Size(max = 30, message = "Phone number must not exceed 30 characters.")
    @Pattern(
            regexp = "^\\+?[0-9]{7,15}$",
            message = "Invalid phone number."
    )
    private String phoneNumber;

    @NotBlank(message = "Email is required.")
    @Size(max = 150, message = "Email must not exceed 150 characters.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Website url is required.")
    private String website;

    @NotBlank(message = "City is required.")
    @Size(max = 100, message = "City must not exceed 100 characters.")
    private String city;

    @NotBlank(message = "Address is required.")
    private String address;

}
