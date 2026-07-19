package com.realestate.backend.mapper;

import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.entity.CategoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(CategoryEntity category);

}
