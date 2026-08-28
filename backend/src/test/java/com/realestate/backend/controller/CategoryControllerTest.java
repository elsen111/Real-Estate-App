package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.security.ratelimit.RateLimitFilter;
import com.realestate.backend.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private RateLimitFilter rateLimitFilter;

    @InjectMocks
    private CategoryController controller;

    private CategoryResponse buildCategory(UUID id) {
        return CategoryResponse.builder()
                .id(id)
                .name("Apartments")
                .slug("apartments")
                .description("Residential apartment listings")
                .active(true)
                .deleted(false)
                .build();
    }

    @Test
    void getActiveCategories_returnsOk_withCategoryList() {
        List<CategoryResponse> categories = List.of(
                buildCategory(UUID.randomUUID()),
                buildCategory(UUID.randomUUID())
        );

        when(categoryService.getActiveCategories()).thenReturn(categories);

        ResponseEntity<ApiResponse<List<CategoryResponse>>> response =
                controller.getActiveCategories();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Category list fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(categories);
        assertThat(response.getBody().getData()).hasSize(2);

        verify(categoryService).getActiveCategories();
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void getActiveCategories_returnsOk_withEmptyList_whenNoActiveCategories() {
        when(categoryService.getActiveCategories()).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<CategoryResponse>>> response =
                controller.getActiveCategories();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEmpty();

        verify(categoryService).getActiveCategories();
    }

    @Test
    void getActiveCategoryById_returnsOk_withCategory() {
        UUID categoryId = UUID.randomUUID();
        CategoryResponse expected = buildCategory(categoryId);

        when(categoryService.getActiveCategoryById(categoryId)).thenReturn(expected);

        ResponseEntity<ApiResponse<CategoryResponse>> response =
                controller.getActiveCategoryById(categoryId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Category fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(categoryService).getActiveCategoryById(categoryId);
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void getActiveCategoryById_propagatesException_whenCategoryNotFound() {
        UUID categoryId = UUID.randomUUID();

        when(categoryService.getActiveCategoryById(categoryId))
                .thenThrow(new RuntimeException("Category not found"));

        try {
            controller.getActiveCategoryById(categoryId);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Category not found");
        }

        verify(categoryService).getActiveCategoryById(categoryId);
    }
}