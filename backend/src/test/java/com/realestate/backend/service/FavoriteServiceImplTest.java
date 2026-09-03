package com.realestate.backend.service;

import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.repository.FavoriteRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.FavoriteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private UserRepository userRepository;
    @Mock private PropertyRepository propertyRepository;

    @InjectMocks private FavoriteServiceImpl service;

    private CustomUserDetails user(UUID id) {
        return CustomUserDetails.from(UserEntity.builder().id(id).roles(new HashSet<>()).build());
    }

    @Test
    void addFavorite_throws_whenNotLoggedIn() {
        assertThatThrownBy(() -> service.addFavorite(UUID.randomUUID(), null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void addFavorite_throws_whenAlreadyFavorited() {
        UUID userId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserEntity.builder().id(userId).build()));
        when(favoriteRepository.existsByUser_IdAndProperty_Id(userId, propertyId)).thenReturn(true);

        assertThatThrownBy(() -> service.addFavorite(propertyId, user(userId)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void addFavorite_throws_whenPropertyNotActive() {
        UUID userId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        PropertyEntity property = PropertyEntity.builder().id(propertyId).status(PropertyStatus.PENDING).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(UserEntity.builder().id(userId).build()));
        when(favoriteRepository.existsByUser_IdAndProperty_Id(userId, propertyId)).thenReturn(false);
        when(propertyRepository.getReferenceById(propertyId)).thenReturn(property);

        assertThatThrownBy(() -> service.addFavorite(propertyId, user(userId)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteFavorite_throws_whenFavoriteDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(favoriteRepository.existsByUser_IdAndProperty_Id(userId, propertyId)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteFavorite(propertyId, user(userId)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteFavorite_succeeds_whenFavoriteExists() {
        UUID userId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        when(favoriteRepository.existsByUser_IdAndProperty_Id(userId, propertyId)).thenReturn(true);

        service.deleteFavorite(propertyId, user(userId));

        verify(favoriteRepository).deleteByUser_IdAndProperty_Id(userId, propertyId);
    }
}