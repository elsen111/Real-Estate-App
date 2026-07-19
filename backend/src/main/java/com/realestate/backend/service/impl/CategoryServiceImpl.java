package com.realestate.backend.service.impl;

import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.entity.CategoryEntity;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.CategoryMapper;
import com.realestate.backend.repository.CategoryRepository;
import com.realestate.backend.service.CategoryService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    public List<CategoryResponse> getActiveCategories() {

        List<CategoryEntity> activeCategories = categoryRepository.findAllByActiveTrue();

        return activeCategories.stream().map(
                categoryMapper::toResponse
        ).toList();

    }

    @Override
    public CategoryResponse getActiveCategoryById(UUID categoryId) {

        CategoryEntity category = categoryRepository.findByIdAndActiveTrue(categoryId);

        if(category == null){
            throw new ResourceNotFoundException("Category not found with id " + categoryId);
        }

        return categoryMapper.toResponse(category);

    }
}
