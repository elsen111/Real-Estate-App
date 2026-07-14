package com.realestate.backend.controller.favorite;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.favorite.response.FavoriteResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.favorite.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
