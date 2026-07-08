package com.realestate.backend.service.agency;

import com.realestate.backend.dto.agency.request.UpdateAgencyRequest;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.dto.agency.response.AgencySubscriptionResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.AgencySubscriptionEntity;
import com.realestate.backend.entity.SubscriptionPlanEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.agency.AgencyMapper;
import com.realestate.backend.repository.AgencyRepository;
import com.realestate.backend.repository.AgencySubscriptionRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgencyServiceImpl implements AgencyService {

    private final UserRepository userRepository;

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;

    private final AgencySubscriptionRepository agencySubscriptionRepository;

   private final PropertyRepository propertyRepository;

    @Override
    public AgencyResponse getCurrentAgency(CustomUserDetails currentUser) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " + currentUser.getId())
                );

        AgencyEntity currentAgency = user.getAgency();

        if(currentAgency == null) {
            throw new ResourceNotFoundException("No agency associated with this user id: " + currentUser.getId());
        }

        return agencyMapper.toSummary(currentAgency);

    }

    @Transactional
    @Override
    public AgencyResponse updateOwnAgency(
            CustomUserDetails currentUser,
            UpdateAgencyRequest request
    ) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " + currentUser.getId())
                );

        AgencyEntity agency = agencyRepository.findById(user.getAgency().getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency not found with id " + user.getAgency().getId())
                );

        agency.setName(request.getName());
        agency.setDescription(request.getDescription());
        agency.setPhoneNumber(request.getPhoneNumber());
        agency.setEmail(request.getEmail());
        agency.setWebsite(request.getWebsite());
        agency.setCity(request.getCity());
        agency.setAddress(request.getAddress());

        agencyRepository.save(agency);
        return agencyMapper.toSummary(agency);

    }

    @Override
    public AgencySubscriptionResponse getMySubscription(
            CustomUserDetails currentUser
    ) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " + currentUser.getId())
                );

        AgencyEntity agency = user.getAgency();

        if(agency == null) {
            throw new ResourceNotFoundException("The user doesn't belong to any agency: " + currentUser.getId());
        }

        AgencySubscriptionEntity agencySubscription = agencySubscriptionRepository
                .findByAgencyAndStatus(
                        agency,
                        SubscriptionStatus.ACTIVE
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency currently doesn't have active subscription. Id: " + agency.getId())
                );

        long usedListings = propertyRepository.countByAgencyId(agency.getId());
        long usedAgents = userRepository.countByAgency(agency);
        SubscriptionPlanEntity subscriptionPlan = agencySubscription.getPlan();

        return AgencySubscriptionResponse.builder()
                .id(agencySubscription.getId())
                .planId(subscriptionPlan.getId())
                .planName(subscriptionPlan.getName())
                .subscriptionStatus(agencySubscription.getStatus())
                .price(subscriptionPlan.getPrice())
                .durationDays(subscriptionPlan.getDurationDays())
                .startDate(agencySubscription.getStartDate())
                .endDate(agencySubscription.getEndDate())
                .maxListings(subscriptionPlan.getMaxListings())
                .usedListings((int) usedListings)
                .remainingListings(
                        subscriptionPlan.getMaxListings() - (int) usedListings
                )
                .maxAgents(subscriptionPlan.getMaxAgents())
                .usedAgents((int) usedAgents)
                .remainingAgents(
                        subscriptionPlan.getMaxAgents() - (int) usedAgents
                )
                .build();

    }
}
