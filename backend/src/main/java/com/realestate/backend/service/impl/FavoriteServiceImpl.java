package com.realestate.backend.service.impl;

import com.realestate.backend.dto.response.FavoriteResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.entity.FavoriteEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
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

        if(currentUser == null) {
            throw new BusinessException("You need to be logged in");
        }

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " +  currentUser.getId())
                );

        boolean IsAlreadyFavorite = favoriteRepository.existsByUser_IdAndProperty_Id(user.getId(), propertyId);

        if(IsAlreadyFavorite) {
            throw new ConflictException("Property already exists in favorites list.");
        }

        PropertyEntity property = propertyRepository.getReferenceById(propertyId);

        if(property.getStatus() != PropertyStatus.ACTIVE) {
            throw  new BadRequestException("Property is not active.");
        }

        FavoriteEntity addedFavorite = FavoriteEntity.builder()
                .user(user)
                .property(property)
                .build();

        FavoriteEntity savedFavorite = favoriteRepository.saveAndFlush(addedFavorite);

        log.info(
                "User {} added property {} to favorites",
                user.getId(),
                propertyId
        );

        return favoriteMapper.toCreateFavoriteResponse(savedFavorite);

    }

    @Override
    @Transactional
    public void deleteFavorite(UUID propertyId, CustomUserDetails currentUser) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " +  currentUser.getId())
                );

        boolean isFavorite = favoriteRepository.existsByUser_IdAndProperty_Id(user.getId(), propertyId);

        if(!isFavorite) {
            throw new BadRequestException("Favorite does not exist in your list.");
        }

        favoriteRepository.deleteByUser_IdAndProperty_Id(user.getId(), propertyId);

        log.info(
                "User {} removed property {} from favorites",
                user.getId(),
                propertyId
        );

    }

    @Override
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
