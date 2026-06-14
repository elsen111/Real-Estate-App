package com.realestate.backend.repository;

import com.realestate.backend.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    Optional<CategoryEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    Optional<CategoryEntity> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);
}
