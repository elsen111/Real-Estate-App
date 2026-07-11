package com.realestate.backend.service.property;

import com.realestate.backend.dto.property.request.CreatePropertyRequest;
import com.realestate.backend.dto.property.request.PropertyPublicFilterRequest;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.Role;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.property.PropertyMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.repository.specification.PropertySpecification;
import com.realestate.backend.security.CustomUserDetails;
import liquibase.license.User;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AgencyMemberRepository agencyMemberRepository;
    private final AgencySubscriptionRepository agencySubscriptionRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    @Override
    @Transactional
    public PropertyResponse createProperty(CreatePropertyRequest request, CustomUserDetails currentUser) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id" + currentUser.getId())
                );

        AgencyEntity agency = user.getAgency();
        if (agency == null) {
            throw new BadRequestException("You must belong to an agency to create a new property.");
        }

        AgencySubscriptionEntity subscription = agencySubscriptionRepository
                .findFirstByAgency_IdAndStatusOrderByEndDateDesc(agency.getId(), SubscriptionStatus.ACTIVE)
                .orElseThrow(
                        () -> new ConflictException(
                                "Your agency doesn't have an active subscription."
                        )
                );

        if(subscription.getEndDate().isBefore(LocalDate.now())) {
            throw new ConflictException("Your agency's subscription has expired.");
        }

        long currentListings = propertyRepository.countByAgencyId(agency.getId());
        int maxListings = subscription.getPlan().getMaxListings();

        if(currentListings >= maxListings) {
            throw new ConflictException(
                    "Listing limit reached (" + maxListings + "). Upgrade your subscription plan to add more properties."
            );
        }

        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id" + request.getCategoryId())
                );

        UserEntity assignedAgent = null;

        if(request.getAssignedAgentId() != null) {
            assignedAgent = userRepository.findById(request.getAssignedAgentId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("User not found with id" + request.getAssignedAgentId())
                    );

            boolean isActiveMemberOfThisAgency = agencyMemberRepository
                    .existsByAgency_IdAndUser_IdAndActiveTrue(agency.getId(), assignedAgent.getId());

            boolean hasAgentRole = assignedAgent.getRoles().stream()
                    .anyMatch(r -> r.getRoleName() == Role.AGENT);

            if (!isActiveMemberOfThisAgency || !hasAgentRole) {
                throw new BadRequestException(
                        "The specified agent does not belong to your agency");
            }
        }

        PropertyEntity newProperty = propertyMapper.toEntity(request);

        newProperty.setAgency(agency);
        newProperty.setCategory(category);
        newProperty.setAssignedAgent(assignedAgent);

        propertyRepository.saveAndFlush(newProperty);

        return propertyMapper.toDetailResponse(newProperty);
    }

    @Override
    public Page<PropertyResponse> getAllPublicProperties(PropertyPublicFilterRequest filter, Pageable pageable) {
        Specification<PropertyEntity> specification = PropertySpecification
                .withDetailedPublicFilter(filter);

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toPublicClientResponse);
    }
}
