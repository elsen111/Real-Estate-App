package com.realestate.backend.service;

import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.entity.CategoryEntity;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getActiveCategories();

}
