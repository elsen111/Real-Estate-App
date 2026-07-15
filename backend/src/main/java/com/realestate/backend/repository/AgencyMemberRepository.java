package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencyMemberRepository extends JpaRepository<AgencyMemberEntity, UUID> {

    boolean existsByAgency_IdAndUser_Id(UUID agencyId, UUID userId);

    boolean existsByUser_IdAndActiveTrue(UUID userId);

    Optional<AgencyMemberEntity> findByAgency_IdAndUser_Id(UUID agencyId, UUID userId);

    Optional<AgencyMemberEntity> findByAgency_IdAndUser_IdAndActiveTrue(UUID agencyId, UUID userId);


    Page<AgencyMemberEntity> findByAgency_Id(UUID agencyId, Pageable pageable);

    Page<AgencyMemberEntity> findByUser_Id(UUID userId, Pageable pageable);

    Optional<AgencyMemberEntity> findByUserAndActiveTrue(UserEntity user);

    boolean existsByUser_Id(UUID id);

    @Query("""
select am
from AgencyMemberEntity am
join am.user u
join u.roles r
where am.agency.id = :agencyId
and r.roleName = 'AGENCY_OWNER'
""")
    Optional<AgencyMemberEntity> findOwner(UUID agencyId);

    long countByAgencyIdAndActiveTrue(UUID agencyId);

    Optional<AgencyMemberEntity> findByUser_IdAndActiveTrue(UUID userId);

    boolean existsByAgency_IdAndUser_IdAndActiveTrue(UUID id, UUID id1);

    boolean existsByAgencyIdAndUserIdAndActiveTrue(UUID agencyId, UUID iuserId);

    Optional<AgencyMemberEntity> findByUserId(UUID userId);
}
