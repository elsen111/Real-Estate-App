package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.CreateCategoryRequest;
import com.realestate.backend.dto.request.UpdateCategoryRequest;
import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategories() {

        List<CategoryResponse> response = categoryService.getAllCategories();

        return ResponseEntity.ok(
                ApiResponse.success("Category list fetched successfully", response)
        );

    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by id")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable UUID categoryId
    ) {

        CategoryResponse response = categoryService.getCategoryById(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Category fetched successfully", response)
        );

    }

    @PostMapping
    @Operation(summary = "Create a new category")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {

        CategoryResponse response = categoryService.createCategory(request);

        return ResponseEntity.ok(
                ApiResponse.success("Category created successfully", response)
        );

    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update an existing category")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {

        CategoryResponse response = categoryService.updateCategory(request, categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Category updated successfully", response)
        );

    }

    @PatchMapping("/{categoryId}/status")
    @Operation(summary = "Change the status (activity) of an existing category")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> toggleCategoryStatus(
            @PathVariable UUID categoryId
    ) {

        String response = categoryService.toggleStatus(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success(response, null)
        );

    }

}
