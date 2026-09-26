package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.AgencyAgentFilterRequest;
import com.realestate.backend.dto.response.*;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.request.AgencyFilterRequest;
import com.realestate.backend.dto.request.AgencyPropertyFilterRequest;
import com.realestate.backend.dto.request.UpdateAgencyRequest;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.AgencyMapper;
import com.realestate.backend.mapper.AgencyMemberMapper;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.mapper.UserMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.repository.specification.AgencyAgentSpecification;
import com.realestate.backend.repository.specification.AgencySpecification;
import com.realestate.backend.repository.specification.PropertySpecification;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AgencyService;
import com.realestate.backend.service.MediaService;
import com.realestate.backend.storage.MediaUploadPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgencyServiceImpl implements AgencyService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;

    private final AgencySubscriptionRepository agencySubscriptionRepository;

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    private final AgencyMediaRepository agencyMediaRepository;

    private final MediaService mediaService;
    private final AgencyMemberMapper agencyMemberMapper;
    private final AgencyMemberRepository agencyMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public AgencyResponse getCurrentAgency(CustomUserDetails currentUser) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        AgencyEntity currentAgency = user.getAgency();

        if (currentAgency == null) {
            throw new ResourceNotFoundException("No agency associated with this user id: " + currentUser.getId());
        }

        return agencyMapper.toAgencyOwnerResponse(currentAgency);

    }

    @Transactional
    @Override
    public AgencyResponse updateOwnAgency(
            CustomUserDetails currentUser,
            UpdateAgencyRequest request
    ) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        AgencyEntity currentAgency = user.getAgency();

        if (currentAgency == null) {
            throw new ResourceNotFoundException("No agency associated with this user id: " + currentUser.getId());
        }

        AgencyEntity agency = updateAgency(currentAgency.getId(), request);

        log.atInfo()
                .setMessage("Agency updated.")
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyName", agency.getName())
                .log();

        return agencyMapper.toAgencyOwnerResponse(agency);

    }

    @Override
    @Transactional(readOnly = true)
    public AgencySubscriptionResponse getMySubscription(
            CustomUserDetails currentUser
    ) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        AgencyEntity agency = user.getAgency();

        if (agency == null) {
            throw new ResourceNotFoundException("Agency not found associated with the user: " + currentUser.getId());
        }

        AgencySubscriptionEntity agencySubscription = agencySubscriptionRepository
                .findByAgencyAndStatus(
                        agency,
                        SubscriptionStatus.ACTIVE
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Active subscription not found for agency: " + agency.getId())
                );

        long usedListings = propertyRepository.countByAgencyIdAndStatusIn(
                agency.getId(),
                List.of(
                        PropertyStatus.PENDING,
                        PropertyStatus.ACTIVE
                )
        );
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

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponse> getMyAgencyProperties(
            CustomUserDetails currentUser, AgencyPropertyFilterRequest filter, Pageable pageable) {


        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        AgencyEntity currentAgency = user.getAgency();

        if (currentAgency == null) {
            throw new ResourceNotFoundException("No agency associated with this user id: " + currentUser.getId());
        }

        Specification<PropertyEntity> specification = PropertySpecification
                .withAgencyFilter(filter);

        specification = specification.and(PropertySpecification.hasAgencyId(currentAgency.getId()));

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toAdminPropertyResponse);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgencyResponse> getAllPublicAgencies(AgencyFilterRequest filter, Pageable pageable) {
        Specification<AgencyEntity> specification = AgencySpecification
                .withPublicFilter(filter);

        return agencyRepository.findAll(specification, pageable)
                .map(agencyMapper::toPublicAgencyListItem);
    }

    @Override
    @Transactional(readOnly = true)
    public AgencyResponse getPublicAgencyInfo(UUID agencyId) {

        AgencyEntity agency = agencyRepository.findById(agencyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency not found with id: " + agencyId)
                );

        long totalAgents = userRepository.countByAgency(agency) - 1;

        return agencyMapper.toPublicAgencyResponse(agency, totalAgents);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponse> getAgencyProperties(
            UUID agencyId,
            PropertyFilterRequest filter,
            Pageable pageable
    ) {
        Specification<PropertyEntity> specification = PropertySpecification
                .withPublicFilter(filter)
                .and(PropertySpecification.hasAgencyId(agencyId));

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toPublicAgencyPropertyResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AgencyMemberResponse> getAgencyAgents(
            UUID agencyId, AgencyAgentFilterRequest filterRequest, Pageable pageable) {
        Specification<AgencyMemberEntity> specification = AgencyAgentSpecification
                .withAgencyAgentFilter(agencyId, filterRequest);

        return agencyMemberRepository.findAll(specification, pageable)
                .map(agencyMemberMapper::toAgentResponse);

    }

    @Override
    @Transactional
    public AgencyLogoUploadResponse uploadLogo(
            MultipartFile file,
            CustomUserDetails currentUser
    ) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        AgencyEntity agency = user.getAgency();

        if (agency == null) {
            throw new ResourceNotFoundException("Agency not found associated with the user: " + user.getId());
        }

        Optional<AgencyMediaEntity> existingLogo =
                agencyMediaRepository.findByAgencyId(agency.getId());

        log.atInfo()
                .setMessage("Uploading logo for agency.")
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyName", agency.getName())
                .log();

        MediaFileEntity uploadedMedia =
                mediaService.upload(file, MediaUploadPolicy.AGENCY_LOGO);

        if (existingLogo.isPresent()) {

            AgencyMediaEntity agencyMedia = existingLogo.get();

            agencyMediaRepository.delete(agencyMedia);

            mediaService.delete(agencyMedia.getMedia());

            log.atInfo()
                    .setMessage("Replacing existing logo for agency.")
                    .addKeyValue("agencyId", agency.getId())
                    .addKeyValue("agencyName", agency.getName())
                    .log();

        }

        AgencyMediaEntity agencyMedia = AgencyMediaEntity.builder()
                .agency(agency)
                .media(uploadedMedia)
                .build();

        agencyMediaRepository.save(agencyMedia);

        log.atInfo()
                .setMessage("Agency logo uploaded.")
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyName", agency.getName())
                .log();

        return AgencyLogoUploadResponse.builder()
                .logoUrl(uploadedMedia.getFileUrl())
                .build();

    }

    @Override
    @Transactional
    public void removeAgencyLogo(CustomUserDetails currentUser) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        AgencyEntity agency = user.getAgency();

        if (agency == null) {
            throw new ResourceNotFoundException("No agency associated with this user id: " + currentUser.getId());
        }

        AgencyMediaEntity agencyMedia = agencyMediaRepository
                .findByAgencyId(agency.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Logo not found for agency: " + agency.getId()
                        ));

        agencyMediaRepository.delete(agencyMedia);

        mediaService.delete(agencyMedia.getMedia());

    }

    //      SHARED METHOD
    @Transactional
    @Override
    public AgencyEntity updateAgency(UUID agencyId, UpdateAgencyRequest request) {

        AgencyEntity agency = agencyRepository.findById(agencyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency not found with id: " + agencyId)
                );

        if (agencyRepository.existsByEmail(request.getEmail()) && !agency.getEmail().equals(request.getEmail())) {
            throw new ConflictException("Email already exists for another agency.");
        }

        agency.setName(request.getName());
        agency.setDescription(request.getDescription());
        agency.setPhoneNumber(request.getPhoneNumber());
        agency.setEmail(request.getEmail());
        agency.setWebsite(request.getWebsite());
        agency.setCity(request.getCity());
        agency.setAddress(request.getAddress());

        return agency;

    }

}
