package com.realestate.backend.service.favorite;

import com.realestate.backend.dto.favorite.response.FavoriteResponse;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FavoriteService {

    FavoriteResponse addFavorite(UUID propertyId, CustomUserDetails currentUser);

    void deleteFavorite(UUID propertyId, CustomUserDetails currentUser);

    Page<PropertyResponse> getMyFavorites(CustomUserDetails currentUser, Pageable pageable);

}
