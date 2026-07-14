package com.realestate.backend.dto.favorite.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class FavoriteResponse {

    private UUID id;
    private UUID userId;
    private UUID propertyId;

}
