package com.realestate.backend.dto.user.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
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
    LocalDateTime createdAt;

}
