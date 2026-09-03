package com.realestate.backend.dto.request;

import jakarta.validation.Valid;

public record AgencyOwnerRegisterRequest(
        @Valid UserRegisterRequest owner,
        @Valid AgencyRegisterRequest agency
) {
}
