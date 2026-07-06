package com.realestate.backend.mapper.subscription;

import com.realestate.backend.dto.admin.subscription.request.SubscriptionPlanRequest;
import com.realestate.backend.dto.admin.subscription.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.dto.agency.response.AgencySubscriptionResponse;
import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPlanMapper {

    public AgencySubscriptionResponse toAdminResponse(AgencySubscriptionEntity subscription){

        if(subscription == null) return null;

        return AgencySubscriptionResponse.builder()

                .id(subscription.getId())
                .planName(subscription.getPlan().getName())
                .price(subscription.getPlan().getPrice())
                .durationDays(subscription.getPlan().getDurationDays())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .subscriptionStatus(subscription.getStatus())
                .build();

    }

    public SubscriptionPlanEntity toSubscriptionPlanEntity(SubscriptionPlanRequest request){

        return SubscriptionPlanEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .maxListings(request.getMaxListings())
                .maxAgents(request.getMaxAgents())
                .featuredListingsAllowed(request.getFeaturedListingsAllowed())
                .active(true)
                .build();

    }

    public AdminSubscriptionPlanResponse toAdminSubscriptionPlanResponse(SubscriptionPlanEntity subscription){

        return AdminSubscriptionPlanResponse.builder()
                .id(subscription.getId())
                .name(subscription.getName())
                .description(subscription.getDescription())
                .price(subscription.getPrice())
                .durationDays(subscription.getDurationDays())
                .maxListings(subscription.getMaxListings())
                .maximumAgents(subscription.getMaxAgents())
                .active(subscription.isActive())
                .featuredListingsAllowed(subscription.isFeaturedListingsAllowed())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();

    }

}
