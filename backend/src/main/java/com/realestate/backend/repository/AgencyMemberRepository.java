package com.realestate.backend.repository;

import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencyMemberRepository extends JpaRepository<AgencyMemberEntity, UUID>,
        JpaSpecificationExecutor<AgencyMemberEntity> {

    boolean existsByAgency_IdAndUser_Id(
            UUID agencyId,
            UUID userId
    );

    boolean existsByUser_IdAndActiveTrue(
            UUID userId
    );

    Optional<AgencyMemberEntity> findByAgency_IdAndUser_IdAndActiveTrue(
            UUID agencyId,
            UUID userId
    );

    Optional<AgencyMemberEntity> findByUserAndActiveTrue(
            UserEntity user
    );

    @Query(
            """
                    select am
                    from AgencyMemberEntity am
                    join am.user u
                    join u.roles r
                    where am.agency.id = :agencyId
                    and r.roleName = 'AGENCY_OWNER'
                    """
    )
    Optional<AgencyMemberEntity> findOwner(
            UUID agencyId
    );

    long countByAgencyIdAndActiveTrue(
            UUID agencyId
    );

    Optional<AgencyMemberEntity> findByUser_IdAndActiveTrue(
            UUID userId
    );

    boolean existsByAgency_IdAndUser_IdAndActiveTrue(
            UUID agencyId,
            UUID userId
    );

    boolean existsByAgencyIdAndUserIdAndActiveTrue(
            UUID agencyId,
            UUID userId
    );

}
