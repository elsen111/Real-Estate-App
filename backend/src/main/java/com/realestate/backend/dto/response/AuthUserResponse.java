package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@JsonPropertyOrder({
        "id",
        "fullName",
        "email",
        "phoneNumber",
        "avatarUrl",
        "roles",
        "position",
        "enabled",
        "emailVerified",
        "createdAt",
        "updatedAt"
})
public class AuthUserResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private Set<String> roles;
    private String position;
    private boolean enabled;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
