package com.realestate.backend.mapper;

import com.realestate.backend.dto.response.FavoriteResponse;
import com.realestate.backend.entity.FavoriteEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FavoriteMapperTest {

    private FavoriteMapper favoriteMapper;

    @BeforeEach
    void setup() {
        favoriteMapper = Mappers.getMapper(FavoriteMapper.class);
    }

    @Test
    void shouldMapCreateFavoriteResponse() {

        FavoriteEntity favorite = createFavoriteEntity();

        FavoriteResponse response = favoriteMapper.toCreateFavoriteResponse(favorite);

        assertNotNull(response);

        assertEquals(favorite.getId(), response.getId());

        assertEquals(
                favorite.getUser().getId(),
                response.getUserId()
        );

        assertEquals(
                favorite.getProperty().getId(),
                response.getPropertyId()
        );
    }

    // HELPER
    private FavoriteEntity createFavoriteEntity() {

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());

        PropertyEntity property = new PropertyEntity();
        property.setId(UUID.randomUUID());

        FavoriteEntity favorite = new FavoriteEntity();
        favorite.setId(UUID.randomUUID());
        favorite.setUser(user);
        favorite.setProperty(property);

        return favorite;
    }

}