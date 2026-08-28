package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AdminSubscriptionPlanFilterRequest;
import com.realestate.backend.dto.request.SubscriptionPlanRequest;
import com.realestate.backend.dto.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.security.ratelimit.RateLimitFilter;
import com.realestate.backend.service.AdminSubscriptionPlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSubscriptionControllerTest {

    @Mock
    private AdminSubscriptionPlanService adminSubscriptionPlanService;

    @InjectMocks
    private AdminSubscriptionController controller;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    private SubscriptionPlanRequest buildValidRequest() {
        return SubscriptionPlanRequest.builder()
                .name("Gold Plan")
                .description("Premium plan for agencies")
                .price(BigDecimal.valueOf(99.99))
                .durationDays(30)
                .maxListings(50)
                .maxAgents(10)
                .featuredListingsAllowed(true)
                .build();
    }

    private AdminSubscriptionPlanResponse buildResponse(UUID id) {
        return AdminSubscriptionPlanResponse.builder()
                .id(id)
                .name("Gold Plan")
                .description("Premium plan for agencies")
                .price(BigDecimal.valueOf(99.99))
                .durationDays(30)
                .maxListings(50)
                .maximumAgents(10)
                .featuredListingsAllowed(true)
                .active(true)
                .build();
    }

    @Test
    void createSubscriptionPlan_returnsCreated_withCreatedPlan() {
        SubscriptionPlanRequest request = buildValidRequest();
        UUID planId = UUID.randomUUID();
        AdminSubscriptionPlanResponse expected = buildResponse(planId);

        when(adminSubscriptionPlanService.createSubscriptionPlan(request)).thenReturn(expected);

        ResponseEntity<ApiResponse<AdminSubscriptionPlanResponse>> response =
                controller.createSubscriptionPlan(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Subscription plan created successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(adminSubscriptionPlanService).createSubscriptionPlan(request);
        verifyNoMoreInteractions(adminSubscriptionPlanService);
    }

    @Test
    void getAllSubscriptionPlans_returnsOk_withListOfPlans() {
        AdminSubscriptionPlanFilterRequest filter = new AdminSubscriptionPlanFilterRequest();
        filter.setActive(true);

        List<AdminSubscriptionPlanResponse> plans = List.of(buildResponse(UUID.randomUUID()));

        when(adminSubscriptionPlanService.getAllSubscriptionPlans(filter)).thenReturn(plans);

        ResponseEntity<ApiResponse<List<AdminSubscriptionPlanResponse>>> response =
                controller.getAllSubscriptionPlans(filter);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Subscription plans fetched successfully");
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData()).isEqualTo(plans);

        verify(adminSubscriptionPlanService).getAllSubscriptionPlans(filter);
    }

    @Test
    void getAllSubscriptionPlans_returnsOk_withEmptyList_whenNoneFound() {
        AdminSubscriptionPlanFilterRequest filter = new AdminSubscriptionPlanFilterRequest();

        when(adminSubscriptionPlanService.getAllSubscriptionPlans(any())).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<AdminSubscriptionPlanResponse>>> response =
                controller.getAllSubscriptionPlans(filter);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).isEmpty();
    }

    @Test
    void getSubscriptionPlanById_returnsOk_withPlan() {
        UUID planId = UUID.randomUUID();
        AdminSubscriptionPlanResponse expected = buildResponse(planId);

        when(adminSubscriptionPlanService.getSubscriptionPlanById(planId)).thenReturn(expected);

        ResponseEntity<ApiResponse<AdminSubscriptionPlanResponse>> response =
                controller.getSubscriptionPlanById(planId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Subscription plan fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(adminSubscriptionPlanService).getSubscriptionPlanById(planId);
    }

    @Test
    void updateSubscriptionPlan_returnsOk_withUpdatedPlan() {
        UUID planId = UUID.randomUUID();
        SubscriptionPlanRequest request = buildValidRequest();
        AdminSubscriptionPlanResponse expected = buildResponse(planId);

        when(adminSubscriptionPlanService.updateSubscriptionPlan(planId, request)).thenReturn(expected);

        ResponseEntity<ApiResponse<AdminSubscriptionPlanResponse>> response =
                controller.updateSubscriptionPlan(planId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Subscription plan updated successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(adminSubscriptionPlanService).updateSubscriptionPlan(planId, request);
    }

    @Test
    void toggleSubscriptionPlanStatus_returnsOk_withNoData() {
        UUID planId = UUID.randomUUID();

        ResponseEntity<ApiResponse<Void>> response =
                controller.toggleSubscriptionPlanStatus(planId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Subscription plan updated successfully");
        assertThat(response.getBody().getData()).isNull();

        verify(adminSubscriptionPlanService).toggleSubscriptionPlanStatus(planId);
        verifyNoMoreInteractions(adminSubscriptionPlanService);
    }

    @Test
    void deleteSubscriptionPlan_returnsOk_withServiceMessage() {
        UUID planId = UUID.randomUUID();
        String message = "Subscription plan deleted successfully";

        when(adminSubscriptionPlanService.softDeleteSubscriptionPlan(planId)).thenReturn(message);

        ResponseEntity<ApiResponse<Void>> response =
                controller.deleteSubscriptionPlan(planId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo(message);
        assertThat(response.getBody().getData()).isNull();

        verify(adminSubscriptionPlanService).softDeleteSubscriptionPlan(planId);
        verifyNoMoreInteractions(adminSubscriptionPlanService);
    }
}