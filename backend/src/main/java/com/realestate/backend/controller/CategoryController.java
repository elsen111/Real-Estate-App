package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.CategoryLogoUploadResponse;
import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(
        name = "Categories",
        description = """
                Public APIs for retrieving active property categories and their associated
                information, including category details and display metadata.
                """
)
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(
            summary = "Get all active categories",
            description = """
                    Retrieves a list of all currently active property categories.
                    Inactive categories are excluded from the response.
                    """
    )
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategories() {

        List<CategoryResponse> response = categoryService.getActiveCategories();

        return ResponseEntity.ok(
                ApiResponse.success("Category list fetched successfully", response)
        );

    }

    @GetMapping("/{categoryId}")
    @Operation(
            summary = "Get an active category by ID",
            description = """
                    Retrieves the details of a specific active property category using its unique identifier.
                    Returns category information only if the category is active.
                    """
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> getActiveCategoryById(
            @PathVariable UUID categoryId
    ) {

        CategoryResponse response = categoryService.getActiveCategoryById(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Category fetched successfully", response)
        );

    }

}
