package com.realestate.backend.dto.response;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String fullName,
        String email
) {
}
