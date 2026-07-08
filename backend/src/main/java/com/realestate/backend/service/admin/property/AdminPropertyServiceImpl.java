package com.realestate.backend.service.admin.property;

import com.realestate.backend.dto.admin.property.request.AdminPropertyFilterRequest;
import com.realestate.backend.dto.admin.property.response.AdminPropertyResponse;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.property.PropertyMapper;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.specification.AdminPropertySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AdminPropertyServiceImpl implements AdminPropertyService{

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    @Override
    public Page<AdminPropertyResponse> getAllProperties(AdminPropertyFilterRequest filter, Pageable pageable) {

        Specification<PropertyEntity> specification = AdminPropertySpecification
                .withFilter(filter);

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toAdminPropertyResponse);

    }

    @Override
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
