package com.realestate.backend.mapper;

import com.realestate.backend.dto.request.SubscriptionPlanRequest;
import com.realestate.backend.dto.response.AdminSubscriptionPlanResponse;
import com.realestate.backend.dto.response.AgencySubscriptionResponse;
import com.realestate.backend.dto.response.SubscriptionPlanResponse;
import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionPlanMapper {

    @Mapping(source = "plan.id", target = "planId")
    @Mapping(source = "plan.name", target = "planName")
    @Mapping(source = "plan.price", target = "price")
    @Mapping(source = "plan.durationDays", target = "durationDays")
    @Mapping(source = "plan.maxAgents", target = "maxAgents")
    @Mapping(source = "plan.maxListings", target = "maxListings")
    @Mapping(source = "status", target = "subscriptionStatus")
    AgencySubscriptionResponse toAdminResponse(AgencySubscriptionEntity subscription);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SubscriptionPlanEntity toSubscriptionPlanEntity(SubscriptionPlanRequest request);

    @Mapping(source = "maxAgents", target = "maximumAgents")
    AdminSubscriptionPlanResponse toAdminSubscriptionPlanResponse(SubscriptionPlanEntity subscription);

    SubscriptionPlanResponse toPublicSubscriptionPlanResponse(SubscriptionPlanEntity subscription);

}
