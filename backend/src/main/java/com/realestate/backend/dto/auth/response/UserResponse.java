package com.realestate.backend.dto.auth.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class UserResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private boolean enabled;
    private boolean emailVerified;
    private Set<String> roles;
    private String position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
