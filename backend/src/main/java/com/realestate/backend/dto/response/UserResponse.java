package com.realestate.backend.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
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
public class UserResponse {

    UUID id;
    String fullName;
    String email;
    String phoneNumber;
    Set<String> roles;
    Boolean enabled;
    Boolean emailVerified;
    String position;
    String agency;
    String avatarUrl;
    LocalDateTime createdAt;

}
