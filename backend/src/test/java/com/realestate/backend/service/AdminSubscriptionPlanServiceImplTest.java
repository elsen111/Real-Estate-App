package com.realestate.backend.service;

import com.realestate.backend.dto.request.SubscriptionPlanRequest;
import com.realestate.backend.dto.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.SubscriptionPlanMapper;
import com.realestate.backend.repository.AgencySubscriptionRepository;
import com.realestate.backend.repository.SubscriptionPlanRepository;
import com.realestate.backend.service.impl.AdminSubscriptionPlanServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSubscriptionPlanServiceImplTest {

    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock private SubscriptionPlanMapper subscriptionPlanMapper;
    @Mock private AgencySubscriptionRepository agencySubscriptionRepository;

    @InjectMocks private AdminSubscriptionPlanServiceImpl service;

    @Test
    void createSubscriptionPlan_throws_whenNameAlreadyExists() {
        SubscriptionPlanRequest request = new SubscriptionPlanRequest();
        request.setName("Gold Plan");
        when(subscriptionPlanRepository.existsByNameIgnoreCase("Gold Plan")).thenReturn(true);

        assertThatThrownBy(() -> service.createSubscriptionPlan(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createSubscriptionPlan_succeeds_whenNameIsUnique() {
        SubscriptionPlanRequest request = new SubscriptionPlanRequest();
        request.setName("Gold Plan");
        SubscriptionPlanEntity entity = SubscriptionPlanEntity.builder().name("Gold Plan").build();

        when(subscriptionPlanRepository.existsByNameIgnoreCase("Gold Plan")).thenReturn(false);
        when(subscriptionPlanMapper.toSubscriptionPlanEntity(request)).thenReturn(entity);
        when(subscriptionPlanMapper.toAdminSubscriptionPlanResponse(entity))
                .thenReturn(AdminSubscriptionPlanResponse.builder().build());

        AdminSubscriptionPlanResponse response = service.createSubscriptionPlan(request);

        assertThat(response).isNotNull();
    }

    @Test
    void softDeleteSubscriptionPlan_throws_whenPlanInUse() {
        UUID id = UUID.randomUUID();
        SubscriptionPlanEntity entity = SubscriptionPlanEntity.builder().id(id).name("Gold Plan").build();

        when(subscriptionPlanRepository.findById(id)).thenReturn(Optional.of(entity));
        when(agencySubscriptionRepository.existsByPlanIdAndStatus(id, SubscriptionStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> service.softDeleteSubscriptionPlan(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void softDeleteSubscriptionPlan_succeeds_whenNotInUse() {
        UUID id = UUID.randomUUID();
        SubscriptionPlanEntity entity = SubscriptionPlanEntity.builder().id(id).name("Gold Plan").build();

        when(subscriptionPlanRepository.findById(id)).thenReturn(Optional.of(entity));
        when(agencySubscriptionRepository.existsByPlanIdAndStatus(id, SubscriptionStatus.ACTIVE)).thenReturn(false);

        String result = service.softDeleteSubscriptionPlan(id);

        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.isActive()).isFalse();
        assertThat(result).contains("Gold Plan");
    }

    @Test
    void toggleSubscriptionPlanStatus_throws_whenPlanNotFound() {
        UUID id = UUID.randomUUID();
        when(subscriptionPlanRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.toggleSubscriptionPlanStatus(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}