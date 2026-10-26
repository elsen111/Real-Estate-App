package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.CreateCategoryRequest;
import com.realestate.backend.dto.request.UpdateCategoryRequest;
import com.realestate.backend.dto.response.AgencyLogoUploadResponse;
import com.realestate.backend.dto.response.CategoryLogoUploadResponse;
import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.entity.AgencyMediaEntity;
import com.realestate.backend.entity.CategoryEntity;
import com.realestate.backend.entity.CategoryMediaEntity;
import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.CategoryMapper;
import com.realestate.backend.repository.CategoryMediaRepository;
import com.realestate.backend.repository.CategoryRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.CategoryService;
import com.realestate.backend.service.MediaService;
import com.realestate.backend.storage.MediaUploadPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    private final PropertyRepository propertyRepository;

    private final CategoryMediaRepository categoryMediaRepository;

    private final MediaService mediaService;

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern WHITESPACE_OR_HYPHEN = Pattern.compile("[\\s-]+");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");


    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {

        List<CategoryEntity> activeCategories = categoryRepository.findAllByActiveTrue();

        return activeCategories.stream().map(
                categoryMapper::toPublicResponse
        ).toList();

    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getActiveCategoryById(UUID categoryId) {

        CategoryEntity category = categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Active category not found with id: " + categoryId)
                );

        return categoryMapper.toPublicResponse(category);

    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {

        List<CategoryEntity> activeCategories = categoryRepository.findAll();

        return activeCategories.stream().map(
                categoryMapper::toAdminResponse
        ).toList();

    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID categoryId) {

        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + categoryId)
                );

        return categoryMapper.toAdminResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        if (categoryRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {
            throw new ConflictException("Category already exists with name: " + request.getName());
        }

        CategoryEntity newCategory = categoryMapper.toCreatedEntity(request);

        String slug = generateSlug(request.getName().trim());
        newCategory.setSlug(slug);

        CategoryEntity savedCategory = categoryRepository.saveAndFlush(newCategory);

        log.atInfo()
                .setMessage("Category created")
                .addKeyValue("categoryId", savedCategory.getId())
                .addKeyValue("categoryName", savedCategory.getName())
                .log();

        return categoryMapper.toAdminResponse(savedCategory);

    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UpdateCategoryRequest request, UUID categoryId) {

        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + categoryId)
                );

        if (categoryRepository.existsByNameIgnoreCaseAndIdNotAndDeletedFalse(request.getName(), categoryId)) {
            throw new ConflictException("Another category already exists with name: " + request.getName());
        }

        String oldName = category.getName();

        categoryMapper.toUpdatedEntity(request, category);

        CategoryEntity updatedCategory = categoryRepository.save(category);

        updatedCategory.setSlug(generateSlug(request.getName().trim()));

        log.atInfo()
                .setMessage("Category updated")
                .addKeyValue("categoryId", categoryId)
                .addKeyValue("oldName", oldName)
                .addKeyValue("newName", updatedCategory.getName())
                .log();

        return categoryMapper.toAdminResponse(updatedCategory);

    }

    @Override
    public String toggleStatus(UUID categoryId) {

        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + categoryId)
                );

        boolean isCategoryAssignedToProperty = propertyRepository.existsByCategoryIdAndStatus(
                categoryId, PropertyStatus.ACTIVE);

        if (isCategoryAssignedToProperty) {
            throw new BusinessException(
                    "Cannot deactivate a category that is already assigned to an existing property. Category ID: "
                            + category.getId());
        }

        boolean newStatus = !category.getActive();

        category.setActive(newStatus);
        categoryRepository.save(category);

        log.atInfo()
                .setMessage("Category status changed")
                .addKeyValue("categoryId", categoryId)
                .addKeyValue("categoryName", category.getName())
                .addKeyValue("oldStatus", newStatus ? "deactivated" : "activated")
                .addKeyValue("newStatus", newStatus ? "activated" : "deactivated")
                .log();

        return newStatus ? "Category is activated" : "Category is deactivated";

    }

    @Override
    @Transactional
    public void softDeleteCategory(UUID categoryId) {

        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + categoryId)
                );

        boolean isCategoryAssignedToProperty = propertyRepository.existsByCategoryIdAndStatus(
                categoryId, PropertyStatus.ACTIVE);

        if (isCategoryAssignedToProperty) {
            throw new BusinessException(
                    "Cannot delete a category that is already assigned to an existing property. Category ID: "
                            + categoryId);
        }

        String categoryName = category.getName();

        category.setActive(false);
        category.setDeleted(true);

        log.atInfo()
                .setMessage("Category deleted")
                .addKeyValue("categoryId", category.getId())
                .addKeyValue("categoryName", categoryName)
                .log();

    }

    @Override
    @Transactional
    public CategoryLogoUploadResponse uploadImage(UUID categoryId, MultipartFile file) {

        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + categoryId)
                );

        Optional<CategoryMediaEntity> existingImage =
                categoryMediaRepository.findByCategoryId(categoryId);

        log.atInfo()
                .setMessage("Uploading image for category.")
                .addKeyValue("categoryId", categoryId)
                .addKeyValue("categoryName", category.getName())
                .log();

        MediaFileEntity uploadedMedia =
                mediaService.upload(file, MediaUploadPolicy.CATEGORY_IMAGE);

        if (existingImage.isPresent()) {

            CategoryMediaEntity categoryMedia = existingImage.get();

            categoryMediaRepository.delete(categoryMedia);

            mediaService.delete(categoryMedia.getMedia());

            log.atInfo()
                    .setMessage("Replacing existing image for category.")
                    .addKeyValue("categoryId", categoryId)
                    .addKeyValue("categoryName", category.getName())
                    .log();

        }

        CategoryMediaEntity categoryMedia = CategoryMediaEntity.builder()
                .category(category)
                .media(uploadedMedia)
                .build();

        categoryMediaRepository.save(categoryMedia);

        log.atInfo()
                .setMessage("Category image uploaded.")
                .addKeyValue("agencyId", categoryId)
                .addKeyValue("categoryName", category.getName())
                .log();

        return CategoryLogoUploadResponse.builder()
                .image(uploadedMedia.getFileUrl())
                .build();

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
