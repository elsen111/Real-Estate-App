package com.realestate.backend.mapper.favorite;

import com.realestate.backend.dto.favorite.response.FavoriteResponse;
import com.realestate.backend.entity.FavoriteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FavoriteMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "propertyId", source = "property.id")
    FavoriteResponse toCreateFavoriteResponse(FavoriteEntity favorite);

}
