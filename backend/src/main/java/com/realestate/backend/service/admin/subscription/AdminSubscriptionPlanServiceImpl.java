package com.realestate.backend.service.admin.subscription;

import com.realestate.backend.dto.admin.subscription.request.AdminSubscriptionPlanFilterRequest;
import com.realestate.backend.dto.admin.subscription.request.CreateSubscriptionPlanRequest;
import com.realestate.backend.dto.admin.subscription.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.subscription.SubscriptionPlanMapper;
import com.realestate.backend.repository.SubscriptionPlanRepository;
import com.realestate.backend.repository.specification.AdminSubscriptionPlanSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class AdminSubscriptionPlanServiceImpl implements AdminSubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public AdminSubscriptionPlanResponse createSubscriptionPlan(CreateSubscriptionPlanRequest request) {

        validatePlanName(request.getName());

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

    private void validatePlanName(String name) {

        if (subscriptionPlanRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BadRequestException(
                    "Subscription plan with this name already exists."
            );
        }
    }

}
