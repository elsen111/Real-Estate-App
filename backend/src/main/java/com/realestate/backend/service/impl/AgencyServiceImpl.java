package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.AgencyAgentFilterRequest;
import com.realestate.backend.dto.response.AgencyLogoUploadResponse;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.dto.request.AgencyFilterRequest;
import com.realestate.backend.dto.request.AgencyPropertyFilterRequest;
import com.realestate.backend.dto.request.UpdateAgencyRequest;
import com.realestate.backend.dto.response.AgencyResponse;
import com.realestate.backend.dto.response.AgencySubscriptionResponse;
import com.realestate.backend.dto.response.UserResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.MediaFolder;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.AgencyMapper;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.mapper.UserMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.repository.specification.AgencyAgentSpecification;
import com.realestate.backend.repository.specification.AgencyPropertySpecification;
import com.realestate.backend.repository.specification.AgencySpecification;
import com.realestate.backend.repository.specification.PropertySpecification;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AgencyService;
import com.realestate.backend.service.MediaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

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
        return agencyMapper.toAgencyOwnerResponse(agency);

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

    @Override
    public Page<PropertyResponse> getMyAgencyProperties(CustomUserDetails currentUser, AgencyPropertyFilterRequest filter, Pageable pageable) {


        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " + currentUser.getId())
                );

        AgencyEntity agency = agencyRepository.findById(user.getAgency().getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency not found with id " + user.getAgency().getId())
                );



        Specification<PropertyEntity> specification = AgencyPropertySpecification
                .withFilter(filter);

        specification = specification.and(AgencyPropertySpecification.hasAgencyId(user.getAgency().getId()));

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toAdminPropertyResponse);

    }

    @Override
    public Page<AgencyResponse> getAllPublicAgencies(AgencyFilterRequest filter, Pageable pageable) {
        Specification<AgencyEntity> specification = AgencySpecification
                .withPublicFilter(filter);

        return agencyRepository.findAll(specification, pageable)
                .map(agencyMapper::toPublicAgencyListItem);
    }

    @Override
    public AgencyResponse getPublicAgencyInfo(UUID agencyId) {

        AgencyEntity agency = agencyRepository.findById(agencyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency not found with id " + agencyId)
                );

        long totalAgents = userRepository.countByAgency(agency);

        return agencyMapper.toPublicAgencyResponse(agency, totalAgents);

    }

    @Override
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
    public Page<UserResponse> getAgencyAgents(UUID agencyId, AgencyAgentFilterRequest filterRequest, Pageable pageable) {
        Specification<UserEntity> specification = AgencyAgentSpecification
                .withAgencyAgentFilter(agencyId, filterRequest);

        return userRepository.findAll(specification, pageable)
                .map(userMapper::toAgentResponse);

    }

    @Override
    @Transactional
    public AgencyLogoUploadResponse uploadLogo(
            MultipartFile file,
            CustomUserDetails currentUser
    ) {

         UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with this id")
                );

         AgencyEntity agency = user.getAgency();

         if(agency == null) {
             throw new ResourceNotFoundException("This user doesn't belong to any agency: " + currentUser.getId());
         }

        Optional<AgencyMediaEntity> existingLogo =
                agencyMediaRepository.findByAgencyId(agency.getId());

        MediaFileEntity uploadedMedia =
                mediaService.upload(file, MediaFolder.AGENCY_LOGO);

        if (existingLogo.isPresent()) {

            AgencyMediaEntity agencyMedia = existingLogo.get();

            agencyMediaRepository.delete(agencyMedia);

            mediaService.delete(agencyMedia.getMedia());

        }

        AgencyMediaEntity agencyMedia = AgencyMediaEntity.builder()
                .agency(agency)
                .media(uploadedMedia)
                .build();

        agencyMediaRepository.save(agencyMedia);

        return AgencyLogoUploadResponse.builder()
                .logoUrl(uploadedMedia.getFileUrl())
                .build();

    }

    @Override
    @Transactional
    public void removeAgencyLogo(CustomUserDetails currentUser) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " + currentUser.getId())
                );

        AgencyMediaEntity agencyMedia = agencyMediaRepository
                .findByAgencyId(user.getAgency().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Logo not found."
                        ));

        agencyMediaRepository.delete(agencyMedia);

        mediaService.delete(agencyMedia.getMedia());

    }

}
