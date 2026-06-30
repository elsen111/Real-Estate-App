package com.realestate.backend.service.admin.property;

import com.realestate.backend.dto.admin.agency.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.admin.agency.request.AdminPropertyFilterRequest;
import com.realestate.backend.dto.admin.property.response.AdminPropertyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminPropertyService {

    Page<AdminPropertyResponse> getAllProperties(
            AdminPropertyFilterRequest filter,
            Pageable pageable
    );

}
