package com.realestate.backend.service.impl;

import com.realestate.backend.dto.response.FavoriteResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.entity.FavoriteEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.*;
import com.realestate.backend.mapper.FavoriteMapper;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.repository.FavoriteRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper favoriteMapper;

    private final UserRepository userRepository;

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    @Override
    @Transactional
    public FavoriteResponse addFavorite(UUID propertyId, CustomUserDetails currentUser) {

        if (currentUser == null) {
            throw new UnauthorizedException("You should log in to add favorites.");
        }

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " + currentUser.getId())
                );

        boolean IsAlreadyFavorite = favoriteRepository.existsByUser_IdAndProperty_Id(user.getId(), propertyId);

        if (IsAlreadyFavorite) {
            throw new ConflictException("This property is already added to your favorites.");
        }

        PropertyEntity property = propertyRepository.getReferenceById(propertyId);

        if (property.getStatus() != PropertyStatus.ACTIVE) {
            throw new BusinessException("Property is not active. Property ID: " + propertyId);
        }

        FavoriteEntity addedFavorite = FavoriteEntity.builder()
                .user(user)
                .property(property)
                .build();

        FavoriteEntity savedFavorite = favoriteRepository.saveAndFlush(addedFavorite);

        log.atInfo()
                .setMessage("Property added to favorites")
                .addKeyValue("propertyId", propertyId)
                .addKeyValue("propertyTitle", property.getTitle())
                .addKeyValue("authenticatedUserUUID", user.getId())
                .log();

        return favoriteMapper.toCreateFavoriteResponse(savedFavorite);

    }

    @Override
    @Transactional
    public void deleteFavorite(UUID propertyId, CustomUserDetails currentUser) {

        boolean isFavorite = favoriteRepository.existsByUser_IdAndProperty_Id(currentUser.getId(), propertyId);

        if (!isFavorite) {
            throw new ResourceNotFoundException("Property not found in your list. Property ID: " + propertyId);
        }

        favoriteRepository.deleteByUser_IdAndProperty_Id(currentUser.getId(), propertyId);

        log.atInfo()
                .setMessage("Property removed from favorites")
                .addKeyValue("propertyId", propertyId)
                .addKeyValue("authenticatedUserUUID", currentUser.getId())
                .log();

    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponse> getMyFavorites(CustomUserDetails currentUser, Pageable pageable) {
        Page<FavoriteEntity> favorites = favoriteRepository.findByUserIdWithProperty(currentUser.getId(), pageable);

        if (favorites.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> propertyIds = favorites.getContent().stream()
                .map(f -> f.getProperty().getId())
                .toList();

        return favorites.map(f ->
                propertyMapper.toPublicClientResponse(f.getProperty())
        );
    }

}
