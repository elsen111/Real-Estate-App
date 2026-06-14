package com.realestate.backend.repository;

import com.realestate.backend.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    boolean existsByReviewer_IdAndAgency_Id(UUID reviewerId, UUID agencyId);

    Page<ReviewEntity> findByAgency_Id(UUID agencyId, Pageable pageable);

    Page<ReviewEntity> findByReviewer_Id(UUID reviewerId, Pageable pageable);

}
