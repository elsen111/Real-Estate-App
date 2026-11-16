package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.FavoriteResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@Tag(
        name = "Favorites",
        description = """
                APIs for managing the authenticated user's favorite properties,
                including retrieving saved properties, adding properties to favorites,
                and removing properties from the favorites list.
                """
)
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    @Operation(
            summary = "Get the authenticated user's favorite properties",
            description = """
                    Retrieves a paginated list of properties saved to the currently authenticated
                    user's favorites list.
                    Supports pagination and sorting.
                    Requires authentication.
                    """
    )
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getMyFavorites(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<PropertyResponse> response = favoriteService.getMyFavorites(currentUser, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Favorites fetched successfully.", response)
        );

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{propertyId}")
    @Operation(
            summary = "Add a property to favorites",
            description = """
                    Adds the specified property to the currently authenticated user's favorites list.
                    Returns the created favorite entry.
                    Requires authentication.
                    """
    )
    public ResponseEntity<ApiResponse<FavoriteResponse>> addToFavorites(
            @PathVariable UUID propertyId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        FavoriteResponse response = favoriteService.addFavorite(propertyId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property added to favorites", response)
        );

    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{propertyId}")
    @Operation(
            summary = "Remove a property from favorites",
            description = """
                    Removes the specified property from the currently authenticated user's
                    favorites list.
                    Requires authentication.
                    """
    )
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
