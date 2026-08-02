package com.realestate.backend.mapper;

import com.realestate.backend.dto.request.SubscriptionPlanRequest;
import com.realestate.backend.dto.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.dto.response.AgencySubscriptionResponse;
import com.realestate.backend.dto.response.SubscriptionPlanResponse;
import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SubscriptionPlanMapperTest {

    private SubscriptionPlanMapper subscriptionPlanMapper;

    @BeforeEach
    void setup() {
        subscriptionPlanMapper = Mappers.getMapper(SubscriptionPlanMapper.class);
    }

    @Test
    void shouldMapAgencySubscriptionResponse() {

        AgencySubscriptionEntity subscription = createAgencySubscriptionEntity();

        AgencySubscriptionResponse response =
                subscriptionPlanMapper.toAdminResponse(subscription);

        assertNotNull(response);

        assertEquals(subscription.getPlan().getId(), response.getPlanId());
        assertEquals(subscription.getPlan().getName(), response.getPlanName());
        assertEquals(subscription.getPlan().getPrice(), response.getPrice());
        assertEquals(subscription.getPlan().getDurationDays(), response.getDurationDays());
        assertEquals(subscription.getPlan().getMaxAgents(), response.getMaxAgents());
        assertEquals(subscription.getPlan().getMaxListings(), response.getMaxListings());
        assertEquals(subscription.getStatus(), response.getSubscriptionStatus());
    }

    @Test
    void shouldMapSubscriptionPlanEntity() {

        SubscriptionPlanRequest request = createSubscriptionPlanRequest();

        SubscriptionPlanEntity entity =
                subscriptionPlanMapper.toSubscriptionPlanEntity(request);

        assertNotNull(entity);

        assertEquals(request.getName(), entity.getName());
        assertEquals(request.getPrice(), entity.getPrice());
        assertEquals(request.getDurationDays(), entity.getDurationDays());
        assertEquals(request.getMaxAgents(), entity.getMaxAgents());
        assertEquals(request.getMaxListings(), entity.getMaxListings());

        assertNull(entity.getId());
        assertTrue(entity.isActive());
        assertFalse(entity.isDeleted());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void shouldMapAdminSubscriptionPlanResponse() {

        SubscriptionPlanEntity entity = createSubscriptionPlanEntity();

        AdminSubscriptionPlanResponse response =
                subscriptionPlanMapper.toAdminSubscriptionPlanResponse(entity);

        assertNotNull(response);

        assertEquals(entity.getId(), response.getId());
        assertEquals(entity.getName(), response.getName());
        assertEquals(entity.getPrice(), response.getPrice());
        assertEquals(entity.getDurationDays(), response.getDurationDays());

        assertEquals(entity.getMaxAgents(), response.getMaximumAgents());

        assertEquals(entity.getMaxListings(), response.getMaxListings());
        assertEquals(entity.isActive(), response.getActive());
    }

    // HELPERS
    private AgencySubscriptionEntity createAgencySubscriptionEntity() {

        AgencySubscriptionEntity subscription = new AgencySubscriptionEntity();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPlan(createSubscriptionPlanEntity());

        return subscription;
    }

    private SubscriptionPlanEntity createSubscriptionPlanEntity() {

        SubscriptionPlanEntity entity = new SubscriptionPlanEntity();

        entity.setId(UUID.randomUUID());
        entity.setName("Premium");
        entity.setPrice(BigDecimal.valueOf(99.99));
        entity.setDurationDays(30);
        entity.setMaxAgents(20);
        entity.setMaxListings(500);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        return entity;
    }

    private SubscriptionPlanRequest createSubscriptionPlanRequest() {

        SubscriptionPlanRequest request = new SubscriptionPlanRequest();

        request.setName("Premium");
        request.setPrice(BigDecimal.valueOf(99.99));
        request.setDurationDays(30);
        request.setMaxAgents(20);
        request.setMaxListings(500);

        return request;
    }
}