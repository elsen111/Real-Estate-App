package com.realestate.backend.repository;

import com.realestate.backend.entity.FavoriteEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, UUID> {

    boolean existsByUser_IdAndProperty_Id(UUID user, UUID propertyId);

    Optional<FavoriteEntity> findByUser_IdAndProperty_Id(UUID userId, UUID propertyId);

    Page<FavoriteEntity> findByUser_Id(UUID userId, Pageable pageable);

    void deleteByUser_IdAndProperty_Id(UUID userId, UUID propertyId);

    @Query(value = """
    SELECT f FROM FavoriteEntity f
    JOIN FETCH f.property p
    JOIN FETCH p.category
    JOIN FETCH p.agency
    LEFT JOIN FETCH p.assignedAgent
    WHERE f.user.id = :userId
    ORDER BY f.createdAt DESC
    """,
            countQuery = """
    SELECT COUNT(f) FROM FavoriteEntity f
    WHERE f.user.id = :userId
    """)
    Page<FavoriteEntity> findByUserIdWithProperty(@Param("userId") UUID userId, Pageable pageable);

}
