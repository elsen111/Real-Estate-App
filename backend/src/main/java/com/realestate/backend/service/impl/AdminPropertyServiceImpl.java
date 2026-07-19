package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.specification.PropertySpecification;
import com.realestate.backend.service.AdminPropertyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AdminPropertyServiceImpl implements AdminPropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    @Override
    public Page<PropertyResponse> getAllProperties(PropertyFilterRequest filter, Pageable pageable) {

        Specification<PropertyEntity> specification = PropertySpecification
                .withFilter(filter);

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toAdminPropertyResponse);

    }

    @Override
    @Transactional
    public String changePropertyStatus(UUID id, PropertyStatus status) {

        PropertyEntity property =  propertyRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Property not found with id: " + id)
                );

        property.setStatus(status);

        propertyRepository.save(property);

        return "\"" + property.getTitle() + "\"'s status changed to " + status.toString();
    }

}
