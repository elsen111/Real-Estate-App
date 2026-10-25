package com.realestate.backend.dto.response;
import java.util.UUID;

public record TopAgencyResponse(
        UUID id,
        String name,
        String logoUrl,
        Long listingCount,
        Long aggregateViews
) {
}
