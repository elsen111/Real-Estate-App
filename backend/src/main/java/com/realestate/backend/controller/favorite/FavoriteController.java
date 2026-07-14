package com.realestate.backend.controller.favorite;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.favorite.response.FavoriteResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.favorite.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{propertyId}")
    @Operation(summary = "Add to favorites")
    public ResponseEntity<ApiResponse<FavoriteResponse>> addToFavorites(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        FavoriteResponse response = favoriteService.addFavorite(propertyId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property added to favorites", response)
        );

    }

    @DeleteMapping("/{propertyId}")
    @Operation(summary = "Delete favorite from the list.")
    public ResponseEntity<ApiResponse<Void>> deleteFromFavorites(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        favoriteService.deleteFavorite(propertyId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property was successfully deleted from favorites list.", null)
        );

    }

}
