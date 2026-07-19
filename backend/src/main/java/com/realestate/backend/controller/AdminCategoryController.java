package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}
