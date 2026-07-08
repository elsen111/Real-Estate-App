package com.realestate.backend.service.agency;

import com.realestate.backend.dto.agency.request.UpdateAgencyRequest;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.agency.AgencyMapper;
import com.realestate.backend.repository.AgencyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgencyServiceImpl implements AgencyService {

    private final UserRepository userRepository;

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;

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

    @Transactional
    @Override
    public AgencyResponse updateOwnAgency(
            CustomUserDetails currentUser,
            UpdateAgencyRequest request
    ) {

        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id " + currentUser.getId())
                );

        AgencyEntity agency = agencyRepository.findById(user.getAgency().getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Agency not found with id " + user.getAgency().getId())
                );

        agency.setName(request.getName());
        agency.setDescription(request.getDescription());
        agency.setPhoneNumber(request.getPhoneNumber());
        agency.setEmail(request.getEmail());
        agency.setWebsite(request.getWebsite());
        agency.setCity(request.getCity());
        agency.setAddress(request.getAddress());

        agencyRepository.save(agency);
        return agencyMapper.toSummary(agency);

    }
}
