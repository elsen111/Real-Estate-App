package com.realestate.backend.service.admin.property;

import com.realestate.backend.dto.admin.property.request.AdminPropertyFilterRequest;
import com.realestate.backend.dto.admin.property.response.AdminPropertyResponse;
import com.realestate.backend.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminPropertyService {

    Page<AdminPropertyResponse> getAllProperties(
            AdminPropertyFilterRequest filter,
            Pageable pageable
    );

    String changePropertyStatus(UUID id, PropertyStatus status);

}
