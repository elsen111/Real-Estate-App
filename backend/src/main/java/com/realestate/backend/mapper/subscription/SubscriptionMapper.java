package com.realestate.backend.mapper.subscription;

import com.realestate.backend.dto.agency.response.AgencySubscriptionResponse;
import com.realestate.backend.entity.AgencySubscriptionEntity;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

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

}
