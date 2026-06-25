package com.realestate.backend.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 100, message = "Full name should be at most 100 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email address should be at most 150 characters")
    private String email;

    @Pattern(
            regexp = "^\\+?[0-9]{7,15}$",
            message = "Invalid phone number"
    )
    private String phoneNumber;

}
