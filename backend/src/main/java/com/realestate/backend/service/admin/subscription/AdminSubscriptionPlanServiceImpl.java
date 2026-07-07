package com.realestate.backend.service.admin.subscription;

import com.realestate.backend.dto.admin.subscription.request.AdminSubscriptionPlanFilterRequest;
import com.realestate.backend.dto.admin.subscription.request.SubscriptionPlanRequest;
import com.realestate.backend.dto.admin.subscription.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.subscription.SubscriptionPlanMapper;
import com.realestate.backend.repository.AgencySubscriptionRepository;
import com.realestate.backend.repository.SubscriptionPlanRepository;
import com.realestate.backend.repository.specification.AdminSubscriptionPlanSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class AdminSubscriptionPlanServiceImpl implements AdminSubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    private final AgencySubscriptionRepository  agencySubscriptionRepository;

    @Override
    public AdminSubscriptionPlanResponse createSubscriptionPlan(SubscriptionPlanRequest request) {

        validatePlanName(request.getName(), null);

        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanMapper.toSubscriptionPlanEntity(request);

        subscriptionPlanRepository.saveAndFlush(subscriptionPlanEntity);

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

        return subscriptionPlanMapper.toAdminSubscriptionPlanResponse(updatedSubscriptionPlan);

    }

    @Override
    public void toggleSubscriptionPlanStatus(UUID id) {
        SubscriptionPlanEntity subscriptionPlan = subscriptionPlanRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Subscription plan not found with id " + id)
                );

        boolean newStatus = !subscriptionPlan.isActive();

        subscriptionPlan.setActive(newStatus);
        subscriptionPlanRepository.save(subscriptionPlan);

    }

    @Override
    public String softDeleteSubscriptionPlan(UUID id) {

        SubscriptionPlanEntity subscriptionPlan =  subscriptionPlanRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Subscription plan not found with id " + id)
                );

        validatePlanUsage(id);

        subscriptionPlan.setDeleted(true);
        subscriptionPlan.setActive(false);

        subscriptionPlanRepository.save(subscriptionPlan);

        return subscriptionPlan.getName() + " plan deleted successfully";

    }

    private void validatePlanName(String name, UUID planId) {

        String trimmedName = name.trim();
        boolean nameExists;

        if (Objects.nonNull(planId)) {
            nameExists = subscriptionPlanRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, planId);
        } else {
            nameExists = subscriptionPlanRepository.existsByNameIgnoreCase(trimmedName);
        }

        if (nameExists) {
            throw new BadRequestException(
                    "Subscription plan with this name already exists."
            );
        }
    }

    private void validatePlanUsage(UUID planId) {

        boolean usedByAnyAgency = agencySubscriptionRepository.existsByPlanIdAndStatus(planId, SubscriptionStatus.ACTIVE);

        if(usedByAnyAgency) {
            throw new BadRequestException("Plan already in use by agency or agencies");
        }

    }

}
