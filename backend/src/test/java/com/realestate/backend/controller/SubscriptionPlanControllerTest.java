package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.SubscriptionPlanResponse;
import com.realestate.backend.security.ratelimit.RateLimitFilter;
import com.realestate.backend.service.SubscriptionPlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanControllerTest {

    @Mock
    private SubscriptionPlanService subscriptionPlanService;

    @Mock
    private RateLimitFilter rateLimitFilter;

    @InjectMocks
    private SubscriptionPlanController controller;

    private SubscriptionPlanResponse buildPlan(UUID id) {
        return SubscriptionPlanResponse.builder()
                .id(id)
                .name("Gold Plan")
                .description("Premium plan for agencies")
                .price(BigDecimal.valueOf(99.99))
                .durationDays(30)
                .maxListings(50)
                .maxAgents(10)
                .featuredListingsAllowed(true)
                .build();
    }

    @Test
    void getSubscriptionPlans_returnsOk_withActivePlans() {
        List<SubscriptionPlanResponse> plans = List.of(
                buildPlan(UUID.randomUUID()),
                buildPlan(UUID.randomUUID())
        );

        when(subscriptionPlanService.getSubscriptionPlans()).thenReturn(plans);

        ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> response =
                controller.getSubscriptionPlans();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Subscription plans fetched successfully.");
        assertThat(response.getBody().getData()).isEqualTo(plans);
        assertThat(response.getBody().getData()).hasSize(2);

        verify(subscriptionPlanService).getSubscriptionPlans();
        verifyNoMoreInteractions(subscriptionPlanService);
    }

    @Test
    void getSubscriptionPlans_returnsOk_withEmptyList_whenNoActivePlans() {
        when(subscriptionPlanService.getSubscriptionPlans()).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> response =
                controller.getSubscriptionPlans();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEmpty();

        verify(subscriptionPlanService).getSubscriptionPlans();
    }
}