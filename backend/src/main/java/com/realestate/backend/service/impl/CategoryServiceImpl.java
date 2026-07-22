package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.CreateCategoryRequest;
import com.realestate.backend.dto.request.UpdateCategoryRequest;
import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.entity.CategoryEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.CategoryMapper;
import com.realestate.backend.repository.CategoryRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    private final PropertyRepository propertyRepository;

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern WHITESPACE_OR_HYPHEN = Pattern.compile("[\\s-]+");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");


    @Override
    public List<CategoryResponse> getActiveCategories() {

        List<CategoryEntity> activeCategories = categoryRepository.findAllByActiveTrue();

        return activeCategories.stream().map(
                categoryMapper::toPublicResponse
        ).toList();

    }

    @Override
    public CategoryResponse getActiveCategoryById(UUID categoryId) {

        CategoryEntity category = categoryRepository.findByIdAndActiveTrue(categoryId);

        if(category == null){
            throw new ResourceNotFoundException("Category not found with id " + categoryId);
        }

        return categoryMapper.toPublicResponse(category);

    }

    @Override
    public List<CategoryResponse> getAllCategories() {

        List<CategoryEntity> activeCategories = categoryRepository.findAll();

        return activeCategories.stream().map(
                categoryMapper::toAdminResponse
        ).toList();

    }

    @Override
    public CategoryResponse getCategoryById(UUID categoryId) {

        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id " + categoryId)
                );

        return categoryMapper.toAdminResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        if(categoryRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {
            throw new ConflictException("Category with name " + request.getName() + " already exists");
        }

        CategoryEntity newCategory = categoryMapper.toCreatedEntity(request);

        String slug = generateSlug(request.getName().trim());
        newCategory.setSlug(slug);

        CategoryEntity savedCategory = categoryRepository.saveAndFlush(newCategory);

        log.info(
                "Category '{}' ({}) created",
                savedCategory.getName(),
                savedCategory.getId()
        );

        return categoryMapper.toAdminResponse(savedCategory);

    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UpdateCategoryRequest request, UUID categoryId) {

        CategoryEntity oldCategory = categoryRepository.findById(categoryId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id " + categoryId)
                );

        if(categoryRepository.existsByNameIgnoreCaseAndIdNotAndDeletedFalse(request.getName(), categoryId)) {
            throw new ConflictException("Category with name " + request.getName() + " already exists");
        }

        String oldName = oldCategory.getName();

        categoryMapper.toUpdatedEntity(request, oldCategory);

        CategoryEntity updatedCategory = categoryRepository.save(oldCategory);

        updatedCategory.setSlug(generateSlug(request.getName().trim()));

        log.info(
                "Category '{}' ({}) updated to '{}'",
                oldName,
                categoryId,
                updatedCategory.getName()
        );

        return categoryMapper.toAdminResponse(updatedCategory);

    }

    @Override
    public String toggleStatus(UUID categoryId) {

        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id " + categoryId)
                );

        boolean isCategoryAssignedToProperty = propertyRepository.existsByCategoryIdAndStatus(categoryId, PropertyStatus.ACTIVE);

        if(isCategoryAssignedToProperty) {
            throw new BusinessException("Cannot deactivate a category that is already assigned to an existing property");
        }

        boolean newStatus = !category.isActive();

        category.setActive(newStatus);
        categoryRepository.save(category);

        log.info(
                "Category '{}' ({}) {}",
                category.getName(),
                categoryId,
                newStatus ? "activated" : "deactivated"
        );

        return newStatus ? "Category is activated" : "Category is deactivated";

    }

    @Override
    @Transactional
    public void softDeleteCategory(UUID categoryId) {

        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id " + categoryId)
                );

        boolean isCategoryAssignedToProperty = propertyRepository.existsByCategoryIdAndStatus(categoryId, PropertyStatus.ACTIVE);

        if(isCategoryAssignedToProperty) {
            throw new BusinessException("Cannot delete a category as it is already assigned to an existing property");
        }

        category.setActive(false);
        category.setDeleted(true);

        categoryRepository.save(category);

        log.info(
                "Category '{}' ({}) soft deleted",
                category.getName(),
                categoryId
        );

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
