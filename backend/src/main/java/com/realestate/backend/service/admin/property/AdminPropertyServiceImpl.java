package com.realestate.backend.service.admin.property;

import com.realestate.backend.dto.admin.agency.request.AdminPropertyFilterRequest;
import com.realestate.backend.dto.admin.property.response.AdminPropertyResponse;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.mapper.property.PropertyMapper;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.specification.PropertySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AdminPropertyServiceImpl implements AdminPropertyService{

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    @Override
    public Page<AdminPropertyResponse> getAllProperties(AdminPropertyFilterRequest filter, Pageable pageable) {

        Specification<PropertyEntity> specification = PropertySpecification
                .withFilter(filter);

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toAdminPropertyResponse);

    }

}
