package com.realestate.backend.service;

import com.realestate.backend.dto.response.FavoriteResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FavoriteService {

    FavoriteResponse addFavorite(UUID propertyId, CustomUserDetails currentUser);

    void deleteFavorite(UUID propertyId, CustomUserDetails currentUser);

    Page<PropertyResponse> getMyFavorites(CustomUserDetails currentUser, Pageable pageable);

}
