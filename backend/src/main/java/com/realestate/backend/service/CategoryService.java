package com.realestate.backend.service;

import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.entity.CategoryEntity;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryResponse> getActiveCategories();

    CategoryResponse getActiveCategoryById(UUID categoryId);

    List<CategoryResponse> getAllCategories();

}
