package com.realestate.backend.service;

import com.realestate.backend.dto.request.CreateCategoryRequest;
import com.realestate.backend.entity.CategoryEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.CategoryMapper;
import com.realestate.backend.repository.CategoryRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;
    @Mock private PropertyRepository propertyRepository;

    @InjectMocks private CategoryServiceImpl service;

    @Test
    void createCategory_throws_whenNameAlreadyExists() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Villas");
        when(categoryRepository.existsByNameIgnoreCaseAndDeletedFalse("Villas")).thenReturn(true);

        assertThatThrownBy(() -> service.createCategory(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createCategory_generatesSlug_fromName() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Luxury Villas & Estates");
        CategoryEntity newCategory = CategoryEntity.builder().name("Luxury Villas & Estates").build();

        when(categoryRepository.existsByNameIgnoreCaseAndDeletedFalse("Luxury Villas & Estates")).thenReturn(false);
        when(categoryMapper.toCreatedEntity(request)).thenReturn(newCategory);
        when(categoryRepository.saveAndFlush(newCategory)).thenReturn(newCategory);

        service.createCategory(request);

        assertThat(newCategory.getSlug()).isEqualTo("luxury-villas-estates");
    }

    @Test
    void toggleStatus_throws_whenCategoryAssignedToActiveProperty() {
        UUID categoryId = UUID.randomUUID();
        CategoryEntity category = CategoryEntity.builder().id(categoryId).active(true).build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(propertyRepository.existsByCategoryIdAndStatus(categoryId, PropertyStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> service.toggleStatus(categoryId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void softDeleteCategory_throws_whenCategoryNotFound() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.softDeleteCategory(categoryId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generateSlug_normalizesSpacesAndCase() {
        assertThat(CategoryServiceImpl.generateSlug("  Cozy   Apartments  ")).isEqualTo("cozy-apartments");
        assertThat(CategoryServiceImpl.generateSlug("")).isEqualTo("");
    }
}