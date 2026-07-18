package com.realestate.backend.dto.property.response;

import java.util.UUID;

public record SetPropertyMediaResponse(
        UUID id,
        boolean isPrimary,
        Integer sortOrder
) {
}
