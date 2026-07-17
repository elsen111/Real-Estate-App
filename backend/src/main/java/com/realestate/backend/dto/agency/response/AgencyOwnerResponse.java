package com.realestate.backend.dto.agency.response;

import com.realestate.backend.entity.MediaFileEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class AgencyOwnerResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private boolean enabled;
    private boolean emailVerified;

}
