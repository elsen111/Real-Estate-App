package com.realestate.backend.repository;

import com.realestate.backend.entity.FavoriteEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, UUID> {

    boolean existsByUser_IdAndProperty_Id(UUID user, UUID propertyId);

    Optional<FavoriteEntity> findByUser_IdAndProperty_Id(UUID userId, UUID propertyId);

    Page<FavoriteEntity> findByUser_Id(UUID userId, Pageable pageable);

    @Transactional
    void deleteByUser_IdAndProperty_Id(UUID userId, UUID propertyId);

}
