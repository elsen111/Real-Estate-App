package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.request.UpdateAgencyRequest;
import com.realestate.backend.dto.response.*;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.AgencyStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.AgencyMapper;
import com.realestate.backend.mapper.AgencyOwnerMapper;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.mapper.SubscriptionPlanMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.repository.specification.AgencySpecification;
import com.realestate.backend.service.AdminAgencyService;
import com.realestate.backend.service.AgencyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
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

    private final AgencyService agencyService;

    @Override
    public Page<AdminAgencyResponse> getAllAgencies(
            AdminAgencyFilterRequest filter,
            Pageable pageable
    ) {
        Specification<AgencyEntity> specification = AgencySpecification
                .withFilter(filter);

        return agencyRepository.findAll(specification, pageable)
                .map(agencyMapper::toAdminResponse);
    }

    @Override
    public AdminAgencyResponse getAgencyById(UUID id) {
        AgencyEntity agency = agencyRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Agency not found with id " + id
                        )
                );

        AgencyMemberEntity agencyOwner = agencyMemberRepository.findOwner(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Agency member not found with id " + id
                        )
                );

        AgencySubscriptionEntity subscription = agencySubscriptionRepository
                .findFirstByAgencyIdOrderByEndDateDesc(
                        id,
                        SubscriptionStatus.ACTIVE
                )
                .orElse(null);

        UserEntity owner = agencyOwner.getUser();
        AgencyOwnerResponse ownerResponse = agencyOwnerMapper.toResponse(owner);

        AgencySubscriptionResponse subscriptionResponse =
                subscriptionMapper.toAdminResponse(subscription);

        // '-1' is for Agency Owner
        long totalAgents =
                agencyMemberRepository.countByAgencyIdAndActiveTrue(id) - 1;

        long totalProperties =
                propertyRepository.countByAgencyId(id);

        long activeListings =
                propertyRepository.countByAgencyIdAndStatusIn(
                        id,
                        List.of(
                                PropertyStatus.ACTIVE
                        )
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
                        () -> new ResourceNotFoundException(
                                "Agency not found with id " + id
                        )
                );

        AgencyStatus previousStatus = agency.getStatus();

        agency.setStatus(status);

        agencyRepository.save(agency);

        log.atInfo()
                .setMessage("agency_status_changed")
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyName", agency.getName())
                .addKeyValue("previousStatus", previousStatus)
                .addKeyValue("newStatus", status)
                .log();

        return agency.getName() + "'s status changed to " + status.toString();
    }

    @Transactional
    @Override
    public AdminAgencyResponse updateAgency(
            UUID agencyId,
            UpdateAgencyRequest request
    ) {

        AgencyEntity agency = agencyService.updateAgency(agencyId, request);

        log.atInfo()
                .setMessage("Agency updated")
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyName", agency.getName())
                .log();

        return agencyMapper.toAdminResponse(agency);
    }

    @Override
    @Transactional
    public String softDeleteAgency(UUID id) {

        AgencyEntity agency = agencyRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Agency not found with id " + id
                        )
                );

        agency.setIsDeleted(true);

        agencyRepository.save(agency);

        log.atInfo()
                .setMessage("agency_soft_deleted")
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyName", agency.getName())
                .log();

        return agency.getName() + "has been deleted successfully";
    }

    @Override
    @Transactional
    public AgencySubscriptionResponse createAgencySubscription(
            UUID agencyId,
            UUID subscriptionId
    ) {

        AgencyEntity agency = agencyRepository.findById(agencyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Agency not found with id " + agencyId
                        )
                );

        SubscriptionPlanEntity subscriptionPlan =
                subscriptionPlanRepository.findById(subscriptionId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Subscription plan not found with id " + subscriptionId
                                )
                        );

        boolean isPlanActive =
                subscriptionPlanRepository.existsByIdAndActiveTrue(
                        subscriptionPlan.getId()
                );

        boolean isAgencyDeleted = agency.getIsDeleted();

        boolean isAgencyApproved =
                agency.getStatus().equals(AgencyStatus.APPROVED);

        boolean hasActiveSubscription =
                agencySubscriptionRepository.existsByAgencyIdAndStatus(
                        agencyId,
                        SubscriptionStatus.ACTIVE
                );

        if (!isPlanActive) {
            throw new BadRequestException(
                    "Subscription plan is not active."
            );

        } else if (isAgencyDeleted) {
            throw new BadRequestException(
                    "Agency has been deleted."
            );

        } else if (!isAgencyApproved) {
            log.atWarn()
                    .setMessage("subscription_creation_rejected")
                    .addKeyValue("agencyId", agencyId)
                    .addKeyValue("agencyName", agency.getName())
                    .addKeyValue("reason", "agency_not_approved")
                    .log();

            throw new BadRequestException(
                    "Agency has not been approved.Only approved agencies are allowed to get subscriptions."
            );

        } else if (hasActiveSubscription) {
            throw new BadRequestException(
                    "Agency has already an active subscription."
            );
        }

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate =
                startDate.plusDays(subscriptionPlan.getDurationDays());

        AgencySubscriptionEntity agencySubscription =
                AgencySubscriptionEntity.builder()
                        .agency(agency)
                        .plan(subscriptionPlan)
                        .startDate(LocalDate.from(startDate))
                        .endDate(LocalDate.from(endDate))
                        .status(SubscriptionStatus.ACTIVE)
                        .build();

        AgencySubscriptionEntity createdAgencySubscription =
                agencySubscriptionRepository.saveAndFlush(
                        agencySubscription
                );

        log.atInfo()
                .setMessage("agency_subscription_created")
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyName", agency.getName())
                .addKeyValue("subscriptionPlanId", subscriptionPlan.getId())
                .addKeyValue("subscriptionPlanName", subscriptionPlan.getName())
                .addKeyValue("subscriptionEndDate", createdAgencySubscription.getEndDate())
                .log();

        return subscriptionMapper.toAdminResponse(
                createdAgencySubscription
        );
    }

    @Override
    public AgencySubscriptionResponse getAgencySubscription(UUID agencyId) {

        AgencyEntity agency = agencyRepository.findById(agencyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Agency not found with id " + agencyId
                        )
                );

        AgencySubscriptionEntity agencySubscription =
                agencySubscriptionRepository
                        .findFirstByAgencyIdAndStatusOrderByEndDateDesc(
                                agencyId,
                                SubscriptionStatus.ACTIVE
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "This agency does not have any active subscription currently"
                                )
                        );

        // -1 is for Agency Owner
        long usedAgents =
                agencyMemberRepository.countByAgencyIdAndActiveTrue(agencyId) - 1;

        long usedListings =
                propertyRepository.countByAgencyIdAndStatusIn(
                        agencyId,
                        List.of(
                                PropertyStatus.PENDING,
                                PropertyStatus.ACTIVE
                        )
                );

        AgencySubscriptionResponse response =
                subscriptionMapper.toAdminResponse(agencySubscription);

        int maxAgents = agencySubscription.getPlan().getMaxAgents();
        int maxListings = agencySubscription.getPlan().getMaxListings();

        response.setUsedAgents((int) usedAgents);
        response.setRemainingAgents(
                Math.max(0, maxAgents - (int) usedAgents)
        );

        response.setUsedListings((int) usedListings);
        response.setRemainingListings(
                Math.max(0, maxListings - (int) usedListings)
        );

        return response;
    }

    @Transactional
    @Override
    public String approveAgency(UUID agencyId) {

        AgencyEntity agency = agencyRepository
                .findById(agencyId)
                .filter(a -> !a.getIsDeleted())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Agency not found with id " + agencyId
                        )
                );

        if (!AgencyStatus.PENDING.equals(agency.getStatus())) {

            log.atWarn()
                    .setMessage("agency_approval_rejected")
                    .addKeyValue("agencyId", agencyId)
                    .addKeyValue("agencyName", agency.getName())
                    .addKeyValue("currentStatus", agency.getStatus())
                    .log();

            throw new BusinessException(
                    "Only pending agencies can be approved."
            );
        }

        agency.setStatus(AgencyStatus.APPROVED);
        agencyRepository.save(agency);

        log.atInfo()
                .setMessage("agency_approved")
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyName", agency.getName())
                .addKeyValue("previousStatus", AgencyStatus.PENDING)
                .addKeyValue("newStatus", AgencyStatus.APPROVED)
                .log();

        return "Agency " + agency.getName() + " is approved successfully.";
    }

    @Transactional
    @Override
    public String rejectAgency(UUID agencyId) {

        AgencyEntity agency = agencyRepository
                .findById(agencyId)
                .filter(a -> !a.getIsDeleted())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Agency not found with id " + agencyId
                        )
                );

        if (!AgencyStatus.PENDING.equals(agency.getStatus())) {
            throw new BusinessException(
                    "Only pending agencies can be rejected."
            );
        }

        agency.setStatus(AgencyStatus.REJECTED);
        agencyRepository.save(agency);

        log.atInfo()
                .setMessage("agency_rejected")
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyName", agency.getName())
                .addKeyValue("previousStatus", AgencyStatus.PENDING)
                .addKeyValue("newStatus", AgencyStatus.REJECTED)
                .log();

        return "Agency " + agency.getName() + " is rejected successfully.";
    }
}
