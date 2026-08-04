package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@JsonPropertyOrder({
        "id",
        "fullName",
        "avatarUrl",
        "email",
        "phoneNumber",
        "enabled",
        "emailVerified"
})
public class AgencyOwnerResponse {

    private UUID id;
    private String fullName;
    private String avatarUrl;
    private String email;
    private String phoneNumber;
    private boolean enabled;
    private boolean emailVerified;

}
