package com.realestate.backend.service.favorite;

import com.realestate.backend.dto.favorite.response.FavoriteResponse;
import com.realestate.backend.security.CustomUserDetails;

import java.util.UUID;

public interface FavoriteService {

    FavoriteResponse addFavorite(UUID propertyId, CustomUserDetails currentUser);

    void deleteFavorite(UUID propertyId, CustomUserDetails currentUser);

}
