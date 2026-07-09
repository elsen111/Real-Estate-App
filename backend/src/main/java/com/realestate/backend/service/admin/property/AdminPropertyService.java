package com.realestate.backend.service.admin.property;

import com.realestate.backend.dto.property.request.PropertyFilterRequest;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminPropertyService {

    Page<PropertyResponse> getAllProperties(
            PropertyFilterRequest filter,
            Pageable pageable
    );

    String changePropertyStatus(UUID id, PropertyStatus status);

}
