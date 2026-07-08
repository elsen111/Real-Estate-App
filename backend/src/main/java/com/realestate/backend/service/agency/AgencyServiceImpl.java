package com.realestate.backend.service.agency;

import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.agency.AgencyMapper;
import com.realestate.backend.repository.AgencyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgencyServiceImpl implements AgencyService {

    private final UserRepository userRepository;

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;

//    @PreAuthorize("hasRole('AGENCY_ADMIN')")
    @Override
    public AgencyResponse getCurrentAgency(CustomUserDetails currentUser) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " + currentUser.getId())
                );

        AgencyEntity currentAgency = user.getAgency();

        if(currentAgency == null) {
            throw new ResourceNotFoundException("No agency associated with this user id: " + currentUser.getId());
        }

        return agencyMapper.toSummary(currentAgency);

    }
}
