package com.realestate.backend.config.properties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record BootstrapAdminProperties(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "Password should be at least 8 characters long") String password
) {
}
