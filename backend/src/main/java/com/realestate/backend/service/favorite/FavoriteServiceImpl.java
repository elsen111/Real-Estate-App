package com.realestate.backend.service.favorite;

import com.realestate.backend.dto.favorite.response.FavoriteResponse;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.entity.FavoriteEntity;
import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.favorite.FavoriteMapper;
import com.realestate.backend.mapper.property.PropertyMapper;
import com.realestate.backend.repository.FavoriteRepository;
import com.realestate.backend.repository.MediaFileRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final FavoriteMapper favoriteMapper;
    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final MediaFileRepository mediaFileRepository;

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

        Map<UUID, String> mainImageByPropertyId = mediaFileRepository
                .findMainImagesByPropertyIds(propertyIds).stream()
                .collect(Collectors.toMap(
                        m -> m.getProperty().getId(),
                        MediaFileEntity::getFileUrl,
                        (first, second) -> first
                ));
        return favorites.map(f ->
                propertyMapper.toPublicClientResponseWithImage(f.getProperty(), mainImageByPropertyId.get(f.getProperty().getId()))
        );
    }
}
