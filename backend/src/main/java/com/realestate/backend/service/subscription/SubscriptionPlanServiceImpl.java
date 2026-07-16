package com.realestate.backend.service.subscription;

import com.realestate.backend.dto.subscription.response.SubscriptionPlanResponse;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.mapper.subscription.SubscriptionPlanMapper;
import com.realestate.backend.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public List<SubscriptionPlanResponse> getSubscriptionPlans() {

        List<SubscriptionPlanEntity> subscriptionPlans = subscriptionPlanRepository.findByActiveTrueAndDeletedFalseOrderByPriceAsc();

        return subscriptionPlans.stream().map(subscriptionPlanMapper::toPublicSubscriptionPlanResponse).toList();

    }
}
