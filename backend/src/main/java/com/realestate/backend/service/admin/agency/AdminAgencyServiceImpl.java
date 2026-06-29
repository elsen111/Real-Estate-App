package com.realestate.backend.service.admin.agency;

import com.realestate.backend.dto.admin.agency.request.AdminAgencyFilterRequest;
import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.mapper.agency.AgencyMapper;
import com.realestate.backend.repository.AgencyRepository;
import com.realestate.backend.repository.specification.AgencySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAgencyServiceImpl implements AdminAgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;

    @Override
    public Page<AdminAgencyResponse> getAllAgencies(AdminAgencyFilterRequest filter, Pageable pageable) {
        Specification<AgencyEntity> specification = AgencySpecification
                .withFilter(filter);

        return agencyRepository.findAll(specification, pageable)
                .map(agencyMapper::toAdminResponse);
    }
}
