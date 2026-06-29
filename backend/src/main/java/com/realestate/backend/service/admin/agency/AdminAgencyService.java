package com.realestate.backend.service.admin.agency;

import com.realestate.backend.dto.admin.agency.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminAgencyService {

    Page<AdminAgencyResponse> getAllAgencies(
            AdminAgencyFilterRequest filter,
            Pageable pageable
    );

}
