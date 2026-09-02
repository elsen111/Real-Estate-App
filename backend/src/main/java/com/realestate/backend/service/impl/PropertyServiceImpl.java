package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.*;
import com.realestate.backend.dto.response.*;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.MediaFolder;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.*;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.repository.specification.PropertySpecification;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.SecurityConstants;
import com.realestate.backend.service.MediaService;
import com.realestate.backend.service.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final AgencyMemberRepository agencyMemberRepository;

    private final AgencySubscriptionRepository agencySubscriptionRepository;

    private final MediaService mediaService;

    private final PropertyMediaRepository propertyMediaRepository;

    private static final int MAP_RESULTS_LIMIT = 500;

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

        log.atInfo()
                .setMessage("Property created")
                .addKeyValue("propertyId", newProperty.getId())
                .addKeyValue("propertyTitle", newProperty.getTitle())
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyEmail", agency.getEmail())
                .log();

        return propertyMapper.toCreateResponse(newProperty);
    }

    @Override
    public Page<PropertyResponse> getAllPublicProperties(PropertyPublicFilterRequest filter, Pageable pageable) {
        Specification<PropertyEntity> specification = PropertySpecification
                .withDetailedPublicFilter(filter);

        return getPropertyResponses(pageable, specification);
    }

    @Override
    public PropertyDetailResponse getPropertyDetailsById(UUID propertyId, CustomUserDetails currentUser) {

        PropertyEntity property = getPropertyEntity(propertyId);

        if(!canView(property, currentUser)) {
            throw new ResourceNotFoundException("Active property not found with id: " + propertyId);
        }

        PropertyDetailResponse propertyDetails =
                propertyMapper.toDetailResponse(property);

        List<PropertyMediaEntity> mediaFiles =
                propertyMediaRepository
                        .findByPropertyIdOrderBySortOrderAsc(propertyId);

        List<PropertyMediaResponse> images = mediaFiles.stream()
                .map(propertyMapper::toMediaResponse).toList();

        propertyDetails.setImages(images);

        return propertyDetails;

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

        log.atInfo()
                .setMessage("Property updated")
                .addKeyValue("propertyId", updatedProperty.getId())
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyEmail", agency.getEmail())
                .log();

        return propertyMapper.toCreateResponse(updatedProperty);
    }

    @Override
    @Transactional
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

        PropertyStatus previousStatus = property.getStatus();

        property.setStatus(request.getStatus());
        propertyRepository.saveAndFlush(property);

        log.atInfo()
                .setMessage("Property status changed")
                .addKeyValue("propertyId", property.getId())
                .addKeyValue("oldStatus", previousStatus)
                .addKeyValue("newStatus", property.getStatus())
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyEmail", agency.getEmail())
                .log();

    }

    @Override
    @Transactional
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

        log.atInfo()
                .setMessage("Property featured characteristic changed")
                .addKeyValue("propertyId", property.getId())
                .addKeyValue("isFeatured", property.getFeatured())
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyEmail", agency.getEmail())
                .log();

        return propertyMapper.toCreateResponse(property);

    }

    @Override
    @Transactional
    public void softDeleteProperty(UUID propertyId, CustomUserDetails currentUser) {

        PropertyEntity property = getPropertyEntity(propertyId);

        UserEntity user = getCurrentUser(currentUser.getId());

        AgencyEntity agency = user.getAgency();
        if(!isSuperAdmin(currentUser)) {
            if (agency == null) {
                throw new BadRequestException("You must belong to an agency to delete this property.");
            }

        }

        havePermissionOverProperty(property, agency, currentUser);

        if(property.getAssignedAgent() != null && property.getAssignedAgent().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException(
                    "Only agency owners are allowed to change the property's featured characteristics."
            );
        }

        property.setStatus(PropertyStatus.DELETED);
        propertyRepository.saveAndFlush(property);

        log.atInfo()
                .setMessage("Property deleted")
                .addKeyValue("propertyId", property.getId())
                .addKeyValue("agencyId", agency.getId())
                .addKeyValue("agencyEmail", agency.getEmail())
                .log();

    }

    @Override
    public Page<PropertyResponse> getFeaturedProperties(PropertyPublicFilterRequest filter, Pageable pageable) {
        Specification<PropertyEntity> specification = PropertySpecification
                .withFeaturedPublicFilter(filter)
                .and(PropertySpecification.isFeatured(true));;

        return getPropertyResponses(pageable, specification);

    }

    @Override
    public Page<PropertyResponse> getRecentProperties(PropertyPublicFilterRequest filter, int size) {
        Specification<PropertyEntity> specification = PropertySpecification
                .withRecentFilter(filter);

        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return getPropertyResponses(pageable, specification);

    }

    @Override
    public Page<PropertyResponse> getSimilarProperties(UUID propertyId, Pageable pageable) {

        PropertyEntity refProperty = getPropertyEntity(propertyId);

        Specification<PropertyEntity> strictSpec = PropertySpecification.withSimilarityFilter(refProperty);
        long strictCount = propertyRepository.count(strictSpec);

        Specification<PropertyEntity> effectiveSpec = strictCount >= pageable.getPageSize()
                ? strictSpec
                : PropertySpecification.withSimilarityFilterRelaxed(refProperty);

        return getPropertyResponses(pageable, effectiveSpec);

    }

    @Override
    public PropertySearchSuggestionResponse getSearchSuggestions(String keyword) {

        if(!StringUtils.hasText(keyword) || keyword.trim().length() < 2) {
            throw new BadRequestException("Search keyword must be at least 2 characters long.");
        }

        String trimmedKeyword = keyword.trim();
        Pageable limit =  PageRequest.of(0, 8);

        List<PropertySuggestionResponse> properties = propertyRepository.findMatchingTitles(trimmedKeyword, limit);
        List<String> cities = propertyRepository.findMatchingCities(trimmedKeyword, limit);
        List<String> districts = propertyRepository.findMatchingDistricts(trimmedKeyword, limit);

        return propertyMapper.toSuggestionsResponse(properties, cities, districts);

    }

    @Override
    public Page<PropertyMapResponse> getMapProperties(PropertyMapFilterRequest request, Pageable pageable) {
        Specification<PropertyEntity> spec = PropertySpecification.withMapFilter(request);

        int cappedSize = Math.min(pageable.getPageSize(), MAP_RESULTS_LIMIT);
        Pageable effectivePageable = PageRequest.of(pageable.getPageNumber(), cappedSize, pageable.getSort());

        Page<PropertyEntity> propertyPage = propertyRepository.findAll(spec, effectivePageable);

        if (propertyPage.isEmpty()) {
            return Page.empty(effectivePageable);
        }

        return propertyPage.map(propertyMapper::toPropertyMapResponse
        );
    }

    @Override
    public List<PropertyMediaResponse> getPropertyMedia(UUID propertyId) {

        if(!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property not found with id: " + propertyId);
        }

        List<PropertyMediaEntity> mediaFiles = propertyMediaRepository.findByPropertyIdOrderBySortOrderAsc(propertyId);

        if(mediaFiles.isEmpty()) {
            throw new ResourceNotFoundException("This property doesn't have uploaded images");
        }

        return mediaFiles.stream()
                .map(propertyMapper::toMediaResponse).toList();

    }

    @Override
    @Transactional
    public List<SetPropertyMediaResponse> setPrimaryImage(UUID propertyId, UUID propertyMediaId, CustomUserDetails currentUser) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " + currentUser.getId())
                );

        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Property not found with id " + propertyId)
                );

        if(user.getAgency() == null) {
            throw new ResourceNotFoundException("No agency exists for this user.");
        }

        if(!propertyMediaRepository.existsById(propertyMediaId)) {
            throw new ResourceNotFoundException("Property media not found with id " + propertyMediaId);
        }

        havePermissionOverProperty(property, user.getAgency(), currentUser);

        List<PropertyMediaEntity> mediaFiles = propertyMediaRepository.findByPropertyIdOrderBySortOrderAsc(propertyId);

        PropertyMediaEntity primaryMediaFile = propertyMediaRepository.findByPropertyIdAndIsPrimary(propertyId, true);

        primaryMediaFile.setIsPrimary(false);
        propertyMediaRepository.save(primaryMediaFile);

        if(mediaFiles.isEmpty()) {
            throw new ResourceNotFoundException("This property doesn't have uploaded images");
        }

        for (PropertyMediaEntity mediaFile : mediaFiles) {

            if(mediaFile.getId().equals(propertyMediaId)) {
                mediaFile.setIsPrimary(true);
                propertyMediaRepository.save(mediaFile);
                break;
            }

        }

        log.atInfo()
                .setMessage("Property's primary image updated")
                .addKeyValue("propertyMediaId", propertyMediaId)
                .addKeyValue("propertyId", propertyId)
                .log();

        return mediaFiles.stream()
                .map(propertyMapper::toMediaPriorityResponse).toList();
    }

    @Override
    @Transactional
    public void removePropertyMediaFile(
            CustomUserDetails currentUser,
            UUID propertyMediaId
    ) {

        UserEntity user = getCurrentUser(currentUser.getId());

        if (user.getAgency() == null) {
            throw new ResourceNotFoundException(
                    "No agency exists for this user."
            );
        }

        PropertyMediaEntity propertyMedia =
                propertyMediaRepository.findById(propertyMediaId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Property media not found."
                                ));

        PropertyEntity property = propertyMedia.getProperty();

        havePermissionOverProperty(
                property,
                user.getAgency(),
                currentUser
        );

        boolean wasPrimary = propertyMedia.getIsPrimary();

        propertyMediaRepository.delete(propertyMedia);

        mediaService.delete(propertyMedia.getMedia());

        log.atInfo()
                .setMessage("Property media removed.")
                .addKeyValue("propertyId", property.getId())
                .log();

        if (wasPrimary) {

            propertyMediaRepository
                    .findFirstByPropertyIdOrderBySortOrderAsc(property.getId())
                    .ifPresent(media -> {

                        media.setIsPrimary(true);

                        propertyMediaRepository.save(media);

                    });

        }

    }

    @Transactional
    @Override
    public void assignAgentToProperty(
            UUID propertyId,
            AssignAgentToPropertyRequest request,
            CustomUserDetails currentUser
    ) {

        UUID ownerId = currentUser.getId();
        UUID agentId = request.getAgentId();

        log.atInfo()
                .setMessage("Agent assigned to property")
                .addKeyValue("propertyId", propertyId)
                .addKeyValue("agentId", agentId)
                .addKeyValue("ownerId", ownerId)
                .log();

        UserEntity owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agency owner not found with id: " + ownerId
                ));

        if (owner.getAgency() == null) {

            log.atWarn()
                    .setMessage("Agency owner has no associated agency")
                    .addKeyValue("ownerId", ownerId)
                    .log();

            throw new BusinessException(
                    "Agency owner is not associated with an agency."
            );
        }

        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Property not found with id: " + propertyId
                ));

        UUID agencyId = owner.getAgency().getId();

        if (!agencyId.equals(property.getAgency().getId())) {
            log.atWarn()
                    .setMessage("Property assignment rejection due to agency ownership mismatch")
                    .addKeyValue("propertyId", propertyId)
                    .addKeyValue("propertyAgencyId", property.getAgency().getId())
                    .addKeyValue("ownerId", ownerId)
                    .addKeyValue("ownerAgencyId", agencyId)
                    .log();

            throw new ForbiddenException(
                    "You cannot assign a property from another agency."
            );
        }

        if (!PropertyStatus.ACTIVE.equals(property.getStatus())) {
            log.atWarn()
                    .setMessage("Property assignment rejected due to invalid property status")
                    .addKeyValue("propertyId", propertyId)
                    .addKeyValue("propertyStatus", property.getStatus())
                    .log();

            throw new BusinessException(
                    "Agents can only be assigned to active properties."
            );
        }

        if (property.getAssignedAgent() != null && property.getAssignedAgent().getId().equals(agentId)) {
            log.atWarn()
                    .setMessage("Property is already assigned to an agent")
                    .addKeyValue("propertyId", propertyId)
                    .addKeyValue("agentId", property.getAssignedAgent().getId())
                    .log();

            throw new ConflictException(
                    "This property is already assigned to this agent."
            );
        }

        UserEntity agent = userRepository.findById(agentId)
                .filter(user ->
                        Boolean.TRUE.equals(user.getEnabled()) &&
                                Boolean.FALSE.equals(user.getDeleted())
                )
                .orElseThrow(() -> {
                    log.atWarn()
                            .setMessage("Property assignment failed because active agent was not found")
                            .addKeyValue("agentId", agentId)
                            .addKeyValue("propertyId", propertyId)
                            .log();

                    return new ResourceNotFoundException(
                            "Agent not found with id: " + agentId
                    );
                });

        if (agent.getAgency() == null ||
                !agencyId.equals(agent.getAgency().getId())) {

            log.atWarn()
                    .setMessage("Agent assignment denied due to agency ownership mismatch")
                    .addKeyValue("agentId", agentId)
                    .addKeyValue("agencyId", agencyId)
                    .addKeyValue("propertyId", propertyId)
                    .addKeyValue("ownerId", ownerId)
                    .log();

            throw new BadRequestException(
                    "Agent does not belong to this agency."
            );
        }

        if (agent.getRoles().stream().noneMatch(
                role -> role.getRoleName() == Role.AGENT
        )) {

            log.atWarn()
                    .setMessage("Property assignment denied because user is not an agent")
                    .addKeyValue("userId", agentId)
                    .addKeyValue("propertyId", propertyId)
                    .log();

            throw new BadRequestException(
                    "Selected user is not an agent."
            );
        }

        property.setAssignedAgent(agent);

        log.atInfo()
                .setMessage("Property successfully assigned to agent.")
                .addKeyValue("propertyId", propertyId)
                .addKeyValue("agentId", agentId)
                .addKeyValue("ownerId", ownerId)
                .log();
    }

    @Override
    @Transactional
    public List<PropertyMediaResponse> uploadMedia(
            UUID propertyId,
            List<MultipartFile> files,
            CustomUserDetails currentUser
    ) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " + currentUser.getId())
                );

        PropertyEntity property = getManagedProperty(propertyId, currentUser, user.getAgency());

        validateFiles(files);

        validatePropertyMediaLimit(property, files.size());

        int nextSortOrder = getNextSortOrder(property);

        List<PropertyMediaResponse> responses = new ArrayList<>();

        boolean hasMainImage = propertyMediaRepository
                .existsByPropertyIdAndIsPrimaryTrue(property.getId());

        for (MultipartFile file : files) {

            MediaFolder folder = resolveFolder(file);

            MediaFileEntity media = mediaService.upload(file, folder);

            PropertyMediaEntity propertyMedia = PropertyMediaEntity.builder()
                    .property(property)
                    .media(media)
                    .isPrimary(!hasMainImage)
                    .sortOrder(nextSortOrder++)
                    .build();

            propertyMediaRepository.save(propertyMedia);

            responses.add(
                    propertyMapper.toMediaResponse(propertyMedia)
            );

            hasMainImage = true;
        }

        log.atInfo()
                .setMessage("Property media files uploaded successfully")
                .addKeyValue("propertyId", propertyId)
                .addKeyValue("mediaFileCount", responses.size())
                .log();

        return responses;
    }



//    HELPER METHODS
    private Map<UUID, String> getMainImagesByPropertyIds(List<UUID> propertyIds) {
        if (propertyIds.isEmpty()) {
            return Map.of();
        }

        return propertyMediaRepository.findByPropertyIdInAndIsPrimaryTrue(propertyIds).stream()
                .collect(Collectors.toMap(
                        m -> m.getProperty().getId(),
                        m -> m.getMedia().getFileUrl(),
                        (first, second) -> first
                ));
    }

    private UserEntity getCurrentUser(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id" + userId)
                );

    }

    @NonNull
    private Page<PropertyResponse> getPropertyResponses(Pageable pageable, Specification<PropertyEntity> specification) {
        Page<PropertyEntity> propertyPage = propertyRepository.findAll(specification, pageable);

        Map<UUID, String> mainImageByPropertyId = getMainImagesByPropertyIds(
                propertyPage.getContent().stream().map(PropertyEntity::getId).toList()
        );

        return propertyPage.map(property ->
                propertyMapper.toPublicClientResponseWithImage(
                        property,
                        mainImageByPropertyId.get(property.getId())
                )
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

    private PropertyEntity getManagedProperty(
            UUID propertyId,
            CustomUserDetails currentUser,
            AgencyEntity agency
    ) {

        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Property not found."));

        havePermissionOverProperty(property, agency, currentUser);

        return property;
    }

    private void validateFiles(List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {
            throw new FileStorageException(
                    "At least one file is required."
            );
        }

        if (files.size() > 20) {
            throw new FileStorageException(
                    "Maximum 20 files can be uploaded at once."
            );
        }

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                throw new FileStorageException(
                        "One of the uploaded files is empty."
                );
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                throw new FileStorageException(
                        "Each file must not exceed 10 MB."
                );
            }

            String contentType = file.getContentType();

            if (contentType == null) {
                throw new FileStorageException(
                        "Unknown file type."
                );
            }

            if (!(contentType.startsWith("image/")
                    || contentType.startsWith("video/"))) {

                throw new FileStorageException(
                        "Only image and video files are allowed."
                );
            }
        }
    }

    private void validatePropertyMediaLimit(
            PropertyEntity property,
            int newFiles
    ) {

        long existing =
                propertyMediaRepository.countByPropertyId(
                        property.getId()
                );

        if (existing + newFiles > 20) {

            throw new FileStorageException(
                    "A property can contain a maximum of 20 media files."
            );

        }

    }

    private int getNextSortOrder(PropertyEntity property) {

        Integer max =
                propertyMediaRepository
                        .findMaxSortOrder(property.getId());

        return max == null
                ? 0
                : max + 1;

    }

    private MediaFolder resolveFolder(MultipartFile file) {

        String type = file.getContentType();

        assert type != null;
        if (type.startsWith("image/")) {
            return MediaFolder.PROPERTY_IMAGE;
        }

        if (type.startsWith("video/")) {
            return MediaFolder.PROPERTY_VIDEO;
        }

        throw new FileStorageException(
                "Unsupported media type."
        );

    }

}
