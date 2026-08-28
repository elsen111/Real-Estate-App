package com.realestate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realestate.backend.dto.request.CreateCategoryRequest;
import com.realestate.backend.dto.request.UpdateCategoryRequest;
import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.security.CustomUserDetailsService;
import com.realestate.backend.security.JwtService;
import com.realestate.backend.security.ratelimit.RateLimitFilter;
import com.realestate.backend.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = {"ADMIN"})
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    private UUID categoryId;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        categoryResponse = CategoryResponse.builder()
                .id(categoryId)
                .name("Apartments")
                .slug("apartments")
                .description("Residential apartment listings")
                .active(true)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getActiveCategories_returnsCategoryList() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(categoryResponse));

        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category list fetched successfully"))
                .andExpect(jsonPath("$.data[0].id").value(categoryId.toString()))
                .andExpect(jsonPath("$.data[0].name").value("Apartments"));
    }

    @Test
    void getCategoryById_returnsCategory_whenCategoryExists() throws Exception {
        when(categoryService.getCategoryById(categoryId)).thenReturn(categoryResponse);

        mockMvc.perform(get("/admin/categories/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category fetched successfully"))
                .andExpect(jsonPath("$.data.slug").value("apartments"));
    }

    @Test
    void getCategoryById_returnsNotFound_whenCategoryDoesNotExist() throws Exception {
        when(categoryService.getCategoryById(categoryId))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(get("/admin/categories/{categoryId}", categoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategory_returnsCreatedCategory_whenValidRequest() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Apartments");
        request.setDescription("Residential apartment listings");

        when(categoryService.createCategory(any(CreateCategoryRequest.class)))
                .thenReturn(categoryResponse);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category created successfully"))
                .andExpect(jsonPath("$.data.name").value("Apartments"));
    }

    @Test
    void createCategory_returnsBadRequest_whenNameIsBlank() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("");
        request.setDescription("Residential apartment listings");

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void updateCategory_returnsUpdatedCategory_whenValidRequest() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Apartments Updated");
        request.setDescription("Updated description");

        CategoryResponse updated = CategoryResponse.builder()
                .id(categoryId)
                .name("Apartments Updated")
                .slug("apartments-updated")
                .description("Updated description")
                .active(true)
                .deleted(false)
                .build();

        when(categoryService.updateCategory(any(UpdateCategoryRequest.class), eq(categoryId)))
                .thenReturn(updated);

        mockMvc.perform(put("/admin/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Apartments Updated"));
    }

    @Test
    void updateCategory_returnsBadRequest_whenNameIsBlank() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("");

        mockMvc.perform(put("/admin/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleCategoryStatus_returnsSuccessMessage() throws Exception {
        when(categoryService.toggleStatus(categoryId))
                .thenReturn("Category status toggled successfully");

        mockMvc.perform(patch("/admin/categories/{categoryId}/status", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category status toggled successfully"));
    }

    @Test
    void softDeleteCategory_returnsSuccessMessage_whenCategoryExists() throws Exception {
        mockMvc.perform(delete("/admin/categories/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category successfully deleted"));

        verify(categoryService).softDeleteCategory(categoryId);
    }

    @Test
    void softDeleteCategory_returnsNotFound_whenCategoryDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Category not found"))
                .when(categoryService).softDeleteCategory(categoryId);

        mockMvc.perform(delete("/admin/categories/{categoryId}", categoryId))
                .andExpect(status().isNotFound());
    }
}