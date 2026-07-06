package com.realestate.backend.repository;

import com.realestate.backend.entity.SubscriptionPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlanEntity, UUID>, JpaSpecificationExecutor<SubscriptionPlanEntity> {

    Optional<SubscriptionPlanEntity> findByNameIgnoreCase(String name);

    List<SubscriptionPlanEntity> findAllByOrderByPriceAsc();

    boolean existsByNameIgnoreCase(String name);

    List<SubscriptionPlanEntity> findByActiveTrueOrderByPriceAsc();

    List<SubscriptionPlanEntity> findByActiveTrueOrderByPriceDesc();

    boolean existsByIdAndActiveTrue(UUID id);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

}
