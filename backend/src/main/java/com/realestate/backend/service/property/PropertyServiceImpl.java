package com.realestate.backend.service.property;

import com.realestate.backend.dto.media.response.PropertyImageResponse;
import com.realestate.backend.dto.property.request.PropertyRequest;
import com.realestate.backend.dto.property.request.PropertyPublicFilterRequest;
import com.realestate.backend.dto.property.request.PropertyStatusRequest;
import com.realestate.backend.dto.property.response.PropertyDetailResponse;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.exception.UnauthorizedException;
import com.realestate.backend.mapper.property.PropertyMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.repository.specification.PropertySpecification;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AgencyMemberRepository agencyMemberRepository;
    private final AgencySubscriptionRepository agencySubscriptionRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final MediaFileRepository mediaFileRepository;

    @Override
    @Transactional
    public PropertyResponse createProperty(PropertyRequest request, CustomUserDetails currentUser) {

        UserEntity user = getCurrentUser(currentUser.getId());

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

        CategoryEntity category = getCategory(request.getCategoryId());

        UserEntity assignedAgent = getAssignedAgent(request, agency);

        PropertyEntity newProperty = propertyMapper.toEntity(request);

        newProperty.setAgency(agency);
        newProperty.setCategory(category);
        newProperty.setAssignedAgent(assignedAgent);

        propertyRepository.saveAndFlush(newProperty);

        return propertyMapper.toCreateResponse(newProperty);
    }

    @Override
    public Page<PropertyResponse> getAllPublicProperties(PropertyPublicFilterRequest filter, Pageable pageable) {
        Specification<PropertyEntity> specification = PropertySpecification
                .withDetailedPublicFilter(filter);

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toPublicClientResponse);
    }

    @Override
    public PropertyDetailResponse getPropertyDetailsById(UUID propertyId, CustomUserDetails currentUser) {

        PropertyEntity property = getPropertyEntity(propertyId);

        List<PropertyImageResponse> images = mediaFileRepository.findByPropertyIdOrderBySortOrderAsc(propertyId);

        if(!canView(property, currentUser)) {
            throw new ResourceNotFoundException("Active property not found with id: " + propertyId);
        }

        return propertyMapper.toDetailResponse(property).toBuilder().images(propertyMapper.toImageResponseList(images)).build();

    }

    @Override
    @Transactional
    public PropertyResponse updateProperty(UUID propertyId, PropertyRequest request, CustomUserDetails currentUser) {

        UserEntity user = getCurrentUser(currentUser.getId());

        AgencyEntity agency = user.getAgency();

        if (agency == null) {
            throw new BadRequestException("You must belong to an agency to update a property.");
        }

        PropertyEntity property = getPropertyEntity(propertyId);

        havePermissionOverProperty(property, agency, currentUser);

        CategoryEntity category = getCategory(request.getCategoryId());

        UserEntity assignedAgent = getAssignedAgent(request, agency);

        propertyMapper.updateEntityFromDto(request, property);

        property.setCategory(category);
        property.setAssignedAgent(assignedAgent);

        PropertyEntity updatedProperty = propertyRepository.saveAndFlush(property);

        return propertyMapper.toCreateResponse(updatedProperty);
    }

    @Override
    public void updateStatus(UUID propertyId, PropertyStatusRequest request, CustomUserDetails currentUser) {

        UserEntity user = getCurrentUser(currentUser.getId());

        AgencyEntity agency = user.getAgency();

        PropertyEntity property = getPropertyEntity(propertyId);

        if(!isSuperAdmin(currentUser)) {
            if (agency == null) {
                throw new BadRequestException("You must belong to an agency to update a property.");
            }
        }

        havePermissionOverProperty(property, agency, currentUser);

        if(!ALLOWED_STATUSES_FOR_AGENCIES.contains(request.getStatus())) {
            throw new BadRequestException("New status should be one of these: SOLD, RENTED.");
        }

        property.setStatus(request.getStatus());
        propertyRepository.saveAndFlush(property);

    }

    @Override
    public PropertyResponse toggleFeaturedProperty(UUID propertyId, CustomUserDetails currentUser) {

        UserEntity user = getCurrentUser(currentUser.getId());

        PropertyEntity property = getPropertyEntity(propertyId);

        AgencyEntity agency = user.getAgency();
        if(!isSuperAdmin(currentUser)) {
            if (agency == null) {
                throw new BadRequestException("You must belong to an agency to update a property.");
            }

        }

        havePermissionOverProperty(property, agency, currentUser);

        if(property.getAssignedAgent() != null && property.getAssignedAgent().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException(
                    "Only agency owners are allowed to change the property's featured characteristics."
            );
        }

        property.setFeatured(!property.getFeatured());
        propertyRepository.saveAndFlush(property);

        return propertyMapper.toCreateResponse(property);

    }

    private UserEntity getCurrentUser(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id" + userId)
                );

    }

    private CategoryEntity getCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: " + categoryId
                        ));
    }

    private UserEntity getAssignedAgent(PropertyRequest request, AgencyEntity agency) {

        if (request.getAssignedAgentId() == null) {
            return null;
        }

        UserEntity assignedAgent = userRepository.findById(request.getAssignedAgentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + request.getAssignedAgentId()
                        ));

        boolean isActiveMember = agencyMemberRepository
                .existsByAgency_IdAndUser_IdAndActiveTrue(
                        agency.getId(),
                        assignedAgent.getId());

        boolean hasAgentRole = assignedAgent.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == Role.AGENT);

        if (!isActiveMember || !hasAgentRole) {
            throw new BadRequestException(
                    "The specified agent does not belong to your agency.");
        }

        return assignedAgent;
    }

    void havePermissionOverProperty(PropertyEntity property, AgencyEntity agency, CustomUserDetails currentUser) {
        if (isSuperAdmin(currentUser)) {
            return;
        }

        if (!property.getAgency().getId().equals(agency.getId())) {
            throw new BadRequestException("You do not have permission to modify a property belonging to another agency.");
        }
    }

    PropertyEntity getPropertyEntity(UUID propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Property not found with id: " + propertyId)
                );
    }

    private boolean canView(PropertyEntity property, CustomUserDetails currentUser) {
        boolean isPrivileged = currentUser != null && isOwnerAgentOrAdmin(property, currentUser);

        if (isPrivileged) {
            return true;
        }

        return !PUBLICLY_HIDDEN_STATUSES.contains(property.getStatus());
    }

    private static final Set<PropertyStatus> PUBLICLY_HIDDEN_STATUSES = EnumSet.of(
            PropertyStatus.PENDING,
            PropertyStatus.REJECTED,
            PropertyStatus.DELETED,
            PropertyStatus.CANCELED
    );

    private static final Set<PropertyStatus> ALLOWED_STATUSES_FOR_AGENCIES = EnumSet.of(
            PropertyStatus.SOLD,
            PropertyStatus.RENTED
    );

    private boolean isOwnerAgentOrAdmin(PropertyEntity property, CustomUserDetails currentUser) {
        if (isSuperAdmin(currentUser)) {
            return true;
        }

        boolean isOwner = property.getAgency().getEmail() != null
                && property.getAgency().getEmail().equals(currentUser.getEmail());

        boolean isAssignedAgent = property.getAssignedAgent() != null
                && property.getAssignedAgent().getId().equals(currentUser.getId());

        return isOwner || isAssignedAgent;
    }

    private boolean isSuperAdmin(CustomUserDetails user) {
        String target = SecurityConstants.ROLE_PREFIX + "SUPER_ADMIN";
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(target::equals);
    }
}
