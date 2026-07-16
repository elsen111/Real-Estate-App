package com.realestate.backend.service.admin.agency;

import com.realestate.backend.dto.admin.agency.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.dto.admin.property.response.AdminAgencyPropertyResponse;
import com.realestate.backend.dto.agency.response.AgencyOwnerResponse;
import com.realestate.backend.dto.agency.response.AgencyStatisticsResponse;
import com.realestate.backend.dto.agency.response.AgencySubscriptionResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.AgencyStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.agency.AgencyMapper;
import com.realestate.backend.mapper.agency.AgencyOwnerMapper;
import com.realestate.backend.mapper.property.PropertyMapper;
import com.realestate.backend.mapper.subscription.SubscriptionPlanMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.repository.specification.AgencySpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAgencyServiceImpl implements AdminAgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;

    private final AgencyMemberRepository agencyMemberRepository;
    private final AgencyOwnerMapper agencyOwnerMapper;

    private final AgencySubscriptionRepository agencySubscriptionRepository;
    private final SubscriptionPlanMapper subscriptionMapper;

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public Page<AdminAgencyResponse> getAllAgencies(AdminAgencyFilterRequest filter, Pageable pageable) {
        Specification<AgencyEntity> specification = AgencySpecification
                .withFilter(filter);

        return agencyRepository.findAll(specification, pageable)
                .map(agencyMapper::toAdminResponse);
    }

    @Override
    public AdminAgencyResponse getAgencyById(UUID id) {
        AgencyEntity  agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency not found with id " + id));

        AgencyMemberEntity agencyOwner = agencyMemberRepository.findOwner(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency member not found with id " + id));

        AgencySubscriptionEntity subscription = agencySubscriptionRepository
                .findFirstByAgencyIdOrderByEndDateDesc(id, SubscriptionStatus.ACTIVE)
                .orElse(null);

        UserEntity owner = agencyOwner.getUser();
        AgencyOwnerResponse ownerResponse = agencyOwnerMapper.toResponse(owner);

        AgencySubscriptionResponse subscriptionResponse =
                subscriptionMapper.toAdminResponse(subscription);

        long totalAgents =
                agencyMemberRepository.countByAgencyIdAndActiveTrue(id);

        long totalProperties =
                propertyRepository.countByAgencyId(id);

        long activeListings =
                propertyRepository.countByAgencyIdAndStatus(
                        id,
                        PropertyStatus.ACTIVE
                );

        AgencyStatisticsResponse statistics =
                AgencyStatisticsResponse.builder()
                        .totalAgents(totalAgents)
                        .totalProperties(totalProperties)
                        .activeListings(activeListings)
                        .build();

        List<AdminAgencyPropertyResponse> properties =
                propertyRepository.findByAgencyId(id)
                        .stream()
                        .map(propertyMapper::toAdminResponse)
                        .toList();

        AdminAgencyResponse response = agencyMapper.toAdminResponse(agency);

        response.setOwner(ownerResponse);
        response.setSubscription(subscriptionResponse);
        response.setProperties(properties);
        response.setStatistics(statistics);

        return response;

    }

    @Transactional
    @Override
    public String changeAgencyStatus(UUID id, AgencyStatus status) {

        AgencyEntity agency = agencyRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency not found with id " + id)
                );

        agency.setStatus(status);

        agencyRepository.save(agency);

        return agency.getName() + "'s status changed to " + status.toString();

    }

    @Override
    public String softDeleteAgency(UUID id) {

        AgencyEntity agency = agencyRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency not found with id " + id)
                );

        agency.setIsDeleted(true);

        agencyRepository.save(agency);

        return agency.getName() + "has been deleted successfully";

    }

    @Override
    public AgencySubscriptionResponse createAgencySubscription(UUID agencyId, UUID subscriptionId) {

        AgencyEntity agency = agencyRepository.findById(agencyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency not found with id " + agencyId)
                );

        SubscriptionPlanEntity subscriptionPlan = subscriptionPlanRepository.findById(subscriptionId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Subscription plan not found with id " + subscriptionId)
                );

        boolean isPlanActive = subscriptionPlanRepository.existsByIdAndActiveTrue(subscriptionPlan.getId());
        boolean isAgencyDeleted = agency.getIsDeleted();
        boolean isAgencyApproved = agency.getStatus().equals(AgencyStatus.APPROVED);
        boolean hasActiveSubscription = agencySubscriptionRepository.existsByAgencyIdAndStatus(agencyId, SubscriptionStatus.ACTIVE);

        if(!isPlanActive) {
            throw new BadRequestException("Subscription plan is not active.");
        } else if (isAgencyDeleted) {
            throw new  BadRequestException("Agency has been deleted.");
        } else if(!isAgencyApproved)  {
            throw new  BadRequestException("Agency has not been approved.Only approved agencies are allowed to get subscriptions.");
        } else  if(hasActiveSubscription) {
            throw new  BadRequestException("Agency has already an active subscription.");
        }

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(subscriptionPlan.getDurationDays());

        AgencySubscriptionEntity agencySubscription =
                AgencySubscriptionEntity.builder()
                        .agency(agency)
                        .plan(subscriptionPlan)
                        .startDate(LocalDate.from(startDate))
                        .endDate(LocalDate.from(endDate))
                        .status(SubscriptionStatus.ACTIVE)
                        .build();

        AgencySubscriptionEntity createdAgencySubscription = agencySubscriptionRepository.saveAndFlush(agencySubscription);

        return subscriptionMapper.toAdminResponse(createdAgencySubscription);

    }

    @Override
    public AgencySubscriptionResponse getAgencySubscription(UUID agencyId) {

        AgencyEntity agency = agencyRepository.findById(agencyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency not found with id " + agencyId)
                );



        AgencySubscriptionEntity agencySubscription = agencySubscriptionRepository
                .findFirstByAgencyIdAndStatusOrderByEndDateDesc(
                    agencyId,
                    SubscriptionStatus.ACTIVE
        ).orElseThrow(
                () -> new ResourceNotFoundException("This agency does not have any active subscription currently")
        );

        return subscriptionMapper.toAdminResponse(agencySubscription);

    }
}
