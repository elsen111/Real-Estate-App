package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.enums.AgencyMemberType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencyMemberRepository extends JpaRepository<AgencyMemberEntity, UUID> {

    boolean existsByAgency_IdAndUser_Id(UUID agencyId, UUID userId);

    boolean existsByAgency_IdAndUser_IdAndActiveTrue(UUID agencyId, UUID userId);

    Optional<AgencyMemberEntity> findByAgency_IdAndUser_Id(UUID agencyId, UUID userId);

    Optional<AgencyMemberEntity> findByAgency_IdAndUser_IdAndActiveTrue(UUID agencyId, UUID userId);

    boolean existsByAgency_IdAndUser_IdAndMemberTypeAndActiveTrue(
            UUID agencyId,
            UUID userId,
            AgencyMemberType memberType
    );

    Page<AgencyMemberEntity> findByAgency_Id(UUID agencyId, Pageable pageable);

    Page<AgencyMemberEntity> findByUser_Id(UUID userId, Pageable pageable);

}
