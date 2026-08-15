package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.specification.PropertySpecification;
import com.realestate.backend.service.AdminPropertyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPropertyServiceImpl implements AdminPropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    private static final Map<PropertyStatus, Set<PropertyStatus>> ALLOWED_STATUS_TRANSITIONS =
            Map.of(
                    PropertyStatus.PENDING,
                    Set.of(
                            PropertyStatus.ACTIVE,
                            PropertyStatus.REJECTED
                    ),

                    PropertyStatus.ACTIVE,
                    Set.of(
                            PropertyStatus.RENTED,
                            PropertyStatus.SOLD,
                            PropertyStatus.REJECTED,
                            PropertyStatus.DELETED
                    ),

                    PropertyStatus.REJECTED,
                    Set.of(
                            PropertyStatus.PENDING
                    ),

                    PropertyStatus.SOLD,
                    Set.of(),

                    PropertyStatus.RENTED,
                    Set.of(),

                    PropertyStatus.CANCELED,
                    Set.of(),

                    PropertyStatus.DELETED,
                    Set.of()

            );

    @Override
    public Page<PropertyResponse> getAllProperties(PropertyFilterRequest filter, Pageable pageable) {

        Specification<PropertyEntity> specification = PropertySpecification
                .withFilter(filter);

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toAdminPropertyResponse);

    }

    @Override
    @Transactional
    public String changePropertyStatus(UUID id, PropertyStatus newStatus) {

        PropertyEntity property =  propertyRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Property not found with id: " + id)
                );

        PropertyStatus currentStatus = property.getStatus();
        validateStatusTransition(currentStatus, newStatus);

        property.setStatus(newStatus);

        propertyRepository.save(property);

        log.info(
                "Property status changed successfully: propertyId={}, title='{}', oldStatus={}, newStatus={}",
                property.getId(),
                property.getTitle(),
                currentStatus,
                newStatus
        );

        return "'" + property.getTitle() + "'s status changed to " + newStatus.toString();
    }

//    HELPER METHODS
    private void validateStatusTransition(
            PropertyStatus currentStatus,
            PropertyStatus newStatus
    ) {

        if(currentStatus == newStatus) {
            log.warn(
                    "Property status change rejected: property already has status={}",
                    currentStatus
            );

            throw new BadRequestException(
                    "Property is already in status: " + currentStatus
            );
        }

        Set<PropertyStatus> allowedStatuses = ALLOWED_STATUS_TRANSITIONS
                .getOrDefault(currentStatus, Set.of());

        if(!allowedStatuses.contains(newStatus)) {
            log.warn(
                    "Invalid property status transition attempted: currentStatus={}, requestedStatus={}",
                    currentStatus,
                    newStatus
            );

            throw new BadRequestException(
                    "Cannot change property status from "
                    + currentStatus
                    + " to "
                    + newStatus
            );
        }

    }

}
