package com.realestate.backend.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "fullName",
        "email",
        "phoneNumber",
        "avatarUrl",
        "agency",
        "position",
        "roles",
        "enabled",
        "emailVerified",
        "createdAt"
})
public class UserResponse {

    UUID id;
    String fullName;
    String email;
    String phoneNumber;
    String avatarUrl;
    String agency;
    String position;
    Set<String> roles;
    Boolean enabled;
    Boolean emailVerified;
    LocalDateTime createdAt;

}
