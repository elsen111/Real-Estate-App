package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.AdminSubscriptionPlanFilterRequest;
import com.realestate.backend.dto.request.SubscriptionPlanRequest;
import com.realestate.backend.dto.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.SubscriptionPlanMapper;
import com.realestate.backend.repository.AgencySubscriptionRepository;
import com.realestate.backend.repository.SubscriptionPlanRepository;
import com.realestate.backend.repository.specification.AdminSubscriptionPlanSpecification;
import com.realestate.backend.service.AdminSubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminSubscriptionPlanServiceImpl implements AdminSubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    private final AgencySubscriptionRepository  agencySubscriptionRepository;

    @Override
    @Transactional
    public AdminSubscriptionPlanResponse createSubscriptionPlan(SubscriptionPlanRequest request) {

        validatePlanName(request.getName(), null);

        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanMapper.toSubscriptionPlanEntity(request);

        subscriptionPlanRepository.saveAndFlush(subscriptionPlanEntity);

        log.atInfo()
                .setMessage("Subscription plan created")
                .addKeyValue("subscriptionId", subscriptionPlanEntity.getId())
                .addKeyValue("subscriptionTitle", subscriptionPlanEntity.getName())
                .log();

        return  subscriptionPlanMapper.toAdminSubscriptionPlanResponse(subscriptionPlanEntity);

    }

    @Override
    public List<AdminSubscriptionPlanResponse> getAllSubscriptionPlans(AdminSubscriptionPlanFilterRequest filter) {

        Specification<SubscriptionPlanEntity> specification = AdminSubscriptionPlanSpecification.withFilter(filter);

        return subscriptionPlanRepository.findAll(specification).stream().map(subscriptionPlanMapper::toAdminSubscriptionPlanResponse).toList();

    }

    @Override
    public AdminSubscriptionPlanResponse getSubscriptionPlanById(UUID id) {
        SubscriptionPlanEntity subscriptionPlan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Subscription plan not found with id " + id)
                );

        return subscriptionPlanMapper.toAdminSubscriptionPlanResponse(subscriptionPlan);
    }

    @Override
    @Transactional
    public AdminSubscriptionPlanResponse updateSubscriptionPlan(UUID id, SubscriptionPlanRequest request) {

        SubscriptionPlanEntity subscriptionPlan = subscriptionPlanRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Subscription plan not found with id " + id)
                );

        validatePlanName(request.getName(), id);
        validatePlanUsage(id);

        subscriptionPlan.setName(request.getName());
        subscriptionPlan.setDescription(request.getDescription());
        subscriptionPlan.setPrice(request.getPrice());
        subscriptionPlan.setPrice(request.getPrice());
        subscriptionPlan.setDurationDays(request.getDurationDays());
        subscriptionPlan.setMaxListings(request.getMaxListings());
        subscriptionPlan.setMaxAgents(request.getMaxAgents());
        subscriptionPlan.setFeaturedListingsAllowed(request.getFeaturedListingsAllowed());

        SubscriptionPlanEntity updatedSubscriptionPlan = subscriptionPlanRepository.save(subscriptionPlan);

        log.atInfo()
                .setMessage("Subscription plan updated")
                .addKeyValue("subscriptionId", subscriptionPlan.getId())
                .addKeyValue("subscriptionTitle", subscriptionPlan.getName())
                .log();

        return subscriptionPlanMapper.toAdminSubscriptionPlanResponse(updatedSubscriptionPlan);

    }

    @Override
    @Transactional
    public void toggleSubscriptionPlanStatus(UUID id) {
        SubscriptionPlanEntity subscriptionPlan = subscriptionPlanRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Subscription plan not found with id " + id)
                );

        boolean newStatus = !subscriptionPlan.isActive();

        subscriptionPlan.setActive(newStatus);
        subscriptionPlanRepository.save(subscriptionPlan);

        log.atInfo()
                .setMessage("Subscription plan status changed")
                .addKeyValue("subscriptionId", subscriptionPlan.getId())
                .addKeyValue("subscriptionTitle", subscriptionPlan.getName())
                .log();
    }

    @Override
    @Transactional
    public String softDeleteSubscriptionPlan(UUID id) {

        SubscriptionPlanEntity subscriptionPlan =  subscriptionPlanRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Subscription plan not found with id " + id)
                );

        validatePlanUsage(id);

        UUID subscriptionPlanId = subscriptionPlan.getId();
        String subscriptionPlanTitle = subscriptionPlan.getName();

        subscriptionPlan.setDeleted(true);
        subscriptionPlan.setActive(false);

        subscriptionPlanRepository.save(subscriptionPlan);


        log.atInfo()
                .setMessage("Subscription plan deleted.")
                .addKeyValue("subscriptionId", subscriptionPlanId)
                .addKeyValue("subscriptionTitle", subscriptionPlanTitle)
                .log();

        return subscriptionPlan.getName() + " plan deleted successfully";

    }



//    HELPER METHODS
    private void validatePlanName(String name, UUID planId) {

        String trimmedName = name.trim();
        boolean nameExists;

        if (Objects.nonNull(planId)) {
            nameExists = subscriptionPlanRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, planId);
        } else {
            nameExists = subscriptionPlanRepository.existsByNameIgnoreCase(trimmedName);
        }

        if (nameExists) {
            log.atWarn()
                    .setMessage("Duplicate subscription plan name.")
                    .addKeyValue("planId", planId)
                    .log();

            throw new BadRequestException(
                    "Subscription plan with this name already exists."
            );
        }
    }

    private void validatePlanUsage(UUID planId) {

        boolean usedByAnyAgency = agencySubscriptionRepository.existsByPlanIdAndStatus(planId, SubscriptionStatus.ACTIVE);

        if(usedByAnyAgency) {
            log.atWarn()
                    .setMessage("Plan already in use by agency(ies).")
                    .addKeyValue("planId", planId)
                    .log();

            throw new BadRequestException("Plan already in use by agency or agencies");
        }

    }

}
