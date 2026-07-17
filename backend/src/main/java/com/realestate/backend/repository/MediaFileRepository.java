package com.realestate.backend.repository;

import com.realestate.backend.dto.media.response.PropertyImageResponse;
import com.realestate.backend.entity.MediaFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFileEntity, UUID> {

//    List<MediaFileEntity> findByProperty_IdOrderBySortOrderAsc(UUID propertyId);
//
//    List<MediaFileEntity> findByAgency_IdOrderBySortOrderAsc(UUID agencyId);
//
//    List<MediaFileEntity> findByProperty_IdAndMediaPurposeOrderBySortOrderAsc(
//            UUID propertyId,
//            String mediaPurpose
//    );
//
//    List<MediaFileEntity> findByAgency_IdAndMediaPurposeOrderBySortOrderAsc(
//            UUID agencyId,
//            String mediaPurpose
//    );
//
//    Optional<MediaFileEntity> findFirstByProperty_IdAndMediaPurposeAndIsMainTrue(
//            UUID propertyId,
//            String mediaPurpose
//    );
//
//    Optional<MediaFileEntity> findFirstByAgency_IdAndMediaPurposeAndIsMainTrue(
//            UUID agencyId,
//            String mediaPurpose
//    );
//
//    boolean existsByProperty_IdAndId(UUID propertyId, UUID mediaFileId);
//
//    boolean existsByAgency_IdAndId(UUID agencyId, UUID mediaFileId);
//
//    void deleteByProperty_Id(UUID propertyId);
//
//    void deleteByAgency_Id(UUID agencyId);
//
//    void deleteByProperty_IdAndId(UUID propertyId, UUID mediaFileId);
//
//    void deleteByAgency_IdAndId(UUID agencyId, UUID mediaFileId);
//
//    List<PropertyImageResponse> findByPropertyIdOrderBySortOrderAsc(UUID propertyId);
//
//    @Query("""
//    SELECT m FROM MediaFileEntity m
//    WHERE m.property.id IN :propertyIds
//    AND m.isMain = true
//    """)
//    List<MediaFileEntity> findMainImagesByPropertyIds(@Param("propertyIds") List<UUID> propertyIds);

}