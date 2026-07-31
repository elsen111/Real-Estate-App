package com.realestate.backend.service;

import com.realestate.backend.dto.response.SubscriptionPlanResponse;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.mapper.SubscriptionPlanMapper;
import com.realestate.backend.repository.SubscriptionPlanRepository;
import com.realestate.backend.service.impl.SubscriptionPlanServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceImplTest {

    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock private SubscriptionPlanMapper subscriptionPlanMapper;

    @InjectMocks private SubscriptionPlanServiceImpl service;

    @Test
    void getSubscriptionPlans_returnsMappedActivePlans() {
        SubscriptionPlanEntity plan = SubscriptionPlanEntity.builder().name("Gold").active(true).build();
        SubscriptionPlanResponse response = SubscriptionPlanResponse.builder().build();

        when(subscriptionPlanRepository.findByActiveTrueAndDeletedFalseOrderByPriceAsc())
                .thenReturn(List.of(plan));
        when(subscriptionPlanMapper.toPublicSubscriptionPlanResponse(plan)).thenReturn(response);

        List<SubscriptionPlanResponse> result = service.getSubscriptionPlans();

        assertThat(result).containsExactly(response);
    }

    @Test
    void getSubscriptionPlans_returnsEmptyList_whenNoActivePlans() {
        when(subscriptionPlanRepository.findByActiveTrueAndDeletedFalseOrderByPriceAsc())
                .thenReturn(List.of());

        List<SubscriptionPlanResponse> result = service.getSubscriptionPlans();

        assertThat(result).isEmpty();
    }
}