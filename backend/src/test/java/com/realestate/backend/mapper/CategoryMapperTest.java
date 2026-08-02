package com.realestate.backend.mapper;

import com.realestate.backend.dto.request.CreateCategoryRequest;
import com.realestate.backend.dto.request.UpdateCategoryRequest;
import com.realestate.backend.dto.response.CategoryResponse;
import com.realestate.backend.entity.CategoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryMapperTest {

    private CategoryMapper categoryMapper;

    @BeforeEach
    void setup() {
        categoryMapper = Mappers.getMapper(CategoryMapper.class);
    }

    @Test
    void shouldMapPublicResponse() {

        CategoryEntity category = createCategoryEntity();

        CategoryResponse response = categoryMapper.toPublicResponse(category);

        assertNotNull(response);

        assertEquals(category.getId(), response.getId());
        assertEquals(category.getName(), response.getName());
        assertEquals(category.getSlug(), response.getSlug());
        assertEquals(category.getActive(), response.isActive());

        assertNull(response.getDeleted());
    }

    @Test
    void shouldMapCreateRequestToEntity() {

        CreateCategoryRequest request = createCategoryRequest();

        CategoryEntity entity = categoryMapper.toCreatedEntity(request);

        assertNotNull(entity);

        assertEquals(request.getName(), entity.getName());

        assertNull(entity.getId());
        assertNull(entity.getSlug());

        assertTrue(entity.getActive());
        assertFalse(entity.getDeleted());

        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void shouldUpdateEntity() {

        UpdateCategoryRequest request = createUpdateCategoryRequest();

        CategoryEntity entity = createCategoryEntity();

        UUID id = entity.getId();
        String slug = entity.getSlug();
        Boolean active = entity.getActive();
        Boolean deleted = entity.getDeleted();
        LocalDateTime createdAt = entity.getCreatedAt();
        LocalDateTime updatedAt = entity.getUpdatedAt();

        categoryMapper.toUpdatedEntity(request, entity);

        assertEquals(request.getName(), entity.getName());

        assertEquals(id, entity.getId());
        assertEquals(slug, entity.getSlug());
        assertEquals(active, entity.getActive());
        assertEquals(deleted, entity.getDeleted());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }

    // HELPER
    private CategoryEntity createCategoryEntity() {

        CategoryEntity category = new CategoryEntity();
        category.setId(UUID.randomUUID());
        category.setName("Apartment");
        category.setSlug("apartment");
        category.setActive(true);
        category.setDeleted(false);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return category;
    }

    private CreateCategoryRequest createCategoryRequest() {

        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("House");

        return request;
    }

    private UpdateCategoryRequest createUpdateCategoryRequest() {

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Updated House");

        return request;
    }

}