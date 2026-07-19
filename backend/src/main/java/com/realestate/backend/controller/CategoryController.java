package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all active categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategories() {

        List<CategoryResponse> response = categoryService.getActiveCategories();

        return ResponseEntity.ok(
                ApiResponse.success("Category list fetched successfully", response)
        );

    }

}
