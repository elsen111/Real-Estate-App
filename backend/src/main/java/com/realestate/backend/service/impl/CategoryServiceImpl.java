package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.CreateCategoryRequest;
import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.entity.CategoryEntity;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.CategoryMapper;
import com.realestate.backend.repository.CategoryRepository;
import com.realestate.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern WHITESPACE_OR_HYPHEN = Pattern.compile("[\\s-]+");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");


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

    @Override
    public List<CategoryResponse> getAllCategories() {

        List<CategoryEntity> activeCategories = categoryRepository.findAll();

        return activeCategories.stream().map(
                categoryMapper::toResponse
        ).toList();

    }

    @Override
    public CategoryResponse getCategoryById(UUID categoryId) {

        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id " + categoryId)
                );

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        CategoryEntity newCategory = categoryMapper.toCreatedEntity(request);

        String slug = generateSlug(request.getName().trim());
        newCategory.setSlug(slug);

        CategoryEntity savedCategory = categoryRepository.saveAndFlush(newCategory);

        return categoryMapper.toResponse(savedCategory);

    }



//    HELPER METHOD
public static String generateSlug(String str) {

    if (str == null || str.isBlank()) {
        return "";
    }

    String slug = str.toLowerCase(Locale.ENGLISH).trim();

    slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
    slug = DIACRITICS.matcher(slug).replaceAll("");
    slug = NON_ALPHANUMERIC.matcher(slug).replaceAll("");
    slug = WHITESPACE_OR_HYPHEN.matcher(slug).replaceAll("-");

    return slug;
}

}
