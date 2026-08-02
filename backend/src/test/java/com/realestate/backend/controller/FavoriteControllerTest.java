package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.FavoriteResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    @InjectMocks
    private FavoriteController controller;

    private final CustomUserDetails currentUser = mock(CustomUserDetails.class);

    @Test
    void getMyFavorites_returnsOk_withFavoriteProperties() {
        Pageable pageable = Pageable.ofSize(10);
        Page<PropertyResponse> page = new PageImpl<>(List.of(
                PropertyResponse.builder().id(UUID.randomUUID()).title("Cozy studio").build()
        ));

        when(favoriteService.getMyFavorites(currentUser, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<PropertyResponse>>> response =
                controller.getMyFavorites(currentUser, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Favorites fetched successfully.");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(favoriteService).getMyFavorites(currentUser, pageable);
        verifyNoMoreInteractions(favoriteService);
    }

    @Test
    void getMyFavorites_returnsOk_withEmptyPage_whenNoFavorites() {
        Pageable pageable = Pageable.ofSize(10);

        when(favoriteService.getMyFavorites(currentUser, pageable)).thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<ApiResponse<Page<PropertyResponse>>> response =
                controller.getMyFavorites(currentUser, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).isEmpty();
    }

    @Test
    void addToFavorites_returnsOk_withCreatedFavorite() {
        UUID propertyId = UUID.randomUUID();
        FavoriteResponse expected = FavoriteResponse.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .propertyId(propertyId)
                .build();

        when(favoriteService.addFavorite(propertyId, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<FavoriteResponse>> response =
                controller.addToFavorites(propertyId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Property added to favorites");
        assertThat(response.getBody().getData()).isEqualTo(expected);
        assertThat(response.getBody().getData().getPropertyId()).isEqualTo(propertyId);

        verify(favoriteService).addFavorite(propertyId, currentUser);
        verifyNoMoreInteractions(favoriteService);
    }

    @Test
    void addToFavorites_propagatesException_whenAlreadyFavorited() {
        UUID propertyId = UUID.randomUUID();

        when(favoriteService.addFavorite(propertyId, currentUser))
                .thenThrow(new RuntimeException("Property is already in favorites"));

        try {
            controller.addToFavorites(propertyId, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Property is already in favorites");
        }

        verify(favoriteService).addFavorite(propertyId, currentUser);
    }

    @Test
    void deleteFromFavorites_returnsOk_withNoData() {
        UUID propertyId = UUID.randomUUID();

        ResponseEntity<ApiResponse<Void>> response =
                controller.deleteFromFavorites(propertyId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Property was successfully deleted from favorites list.");
        assertThat(response.getBody().getData()).isNull();

        verify(favoriteService).deleteFavorite(propertyId, currentUser);
        verifyNoMoreInteractions(favoriteService);
    }

    @Test
    void deleteFromFavorites_propagatesException_whenFavoriteNotFound() {
        UUID propertyId = UUID.randomUUID();

        org.mockito.Mockito.doThrow(new RuntimeException("Favorite not found"))
                .when(favoriteService).deleteFavorite(propertyId, currentUser);

        try {
            controller.deleteFromFavorites(propertyId, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Favorite not found");
        }

        verify(favoriteService).deleteFavorite(propertyId, currentUser);
    }
}