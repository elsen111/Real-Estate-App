package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.CreateCategoryRequest;
import com.realestate.backend.dto.request.UpdateCategoryRequest;
import com.realestate.backend.dto.response.CategoryLogoUploadResponse;
import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Tag(
        name = "Admin - Category Management",
        description = """
                Administrative endpoints for managing property categories, including category creation,
                retrieval, updates, activation, deactivation, deletion, and image uploads.
                """
)
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(
            summary = "Get all categories",
            description = "Retrieves a list of all categories, including their details and current status."
    )
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategories() {

        List<CategoryResponse> response = categoryService.getAllCategories();

        return ResponseEntity.ok(
                ApiResponse.success("Category list fetched successfully", response)
        );

    }

    @GetMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(
            summary = "Get category by ID",
            description = "Retrieves detailed information about a specific category using its unique identifier."
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable UUID categoryId
    ) {

        CategoryResponse response = categoryService.getCategoryById(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Category fetched successfully", response)
        );

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(
            summary = "Create a category",
            description = """
                    Creates a new property category using the provided category information.
                    The request must satisfy the defined validation constraints.
                    """
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {

        CategoryResponse response = categoryService.createCategory(request);

        return ResponseEntity.ok(
                ApiResponse.success("Category created successfully", response)
        );

    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(
            summary = "Update category",
            description = "Updates the information of an existing category using the provided request data."
    )
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(
            summary = "Toggle category status",
            description = """
                    Toggles the active status of an existing category.
                    Changes an active category to inactive or an inactive category to active.
                    """
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> toggleCategoryStatus(
            @PathVariable UUID categoryId
    ) {

        String response = categoryService.toggleStatus(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success(response, null)
        );

    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(
            summary = "Delete category",
            description = """
                    Soft-deletes an existing category using its unique identifier.
                    The category is marked as deleted without permanently removing its database record.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> softDeleteCategory(
            @PathVariable UUID categoryId
    ) {

        categoryService.softDeleteCategory(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Category successfully deleted", null)
        );

    }

    @PostMapping(
            value = "/{categoryId}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Operation(
            summary = "Upload category image",
            description = """
                    Uploads an image for a specific category using multipart/form-data.
                    The uploaded image is associated with the specified category and its storage information is returned.
                    """
    )
    public ResponseEntity<ApiResponse<CategoryLogoUploadResponse>> uploadCategoryImage(
            @PathVariable UUID categoryId,
            @RequestPart("file") MultipartFile file
    ) {

        CategoryLogoUploadResponse response = categoryService.uploadImage(categoryId, file);

        return ResponseEntity.ok(
                ApiResponse.success("Category image uploaded successfully.", response)
        );

    }

}
