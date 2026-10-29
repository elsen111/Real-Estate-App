package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencyMediaEntity;
import com.realestate.backend.entity.CategoryMediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryMediaRepository extends JpaRepository<CategoryMediaEntity, UUID> {

    Optional<CategoryMediaEntity> findByCategoryId(
            UUID categoryId
    );

}
