package com.realestate.backend.service.agent;


import com.realestate.backend.dto.agent.response.AgentResponse;
import com.realestate.backend.dto.property.request.PropertyFilterRequest;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.property.PropertyMapper;
import com.realestate.backend.mapper.user.UserMapper;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.repository.specification.AgencyPropertySpecification;
import com.realestate.backend.repository.specification.PropertySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    @Override
    public AgentResponse getAgentByUserId(UUID userId) {

        AgencyMemberEntity member = userRepository.findAgentMemberByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with user id: " + userId));

        return userMapper.toAgentWithUserIdResponse(member);

    }

    @Override
    public Page<PropertyResponse> getAgentProperties(UUID userId, PropertyFilterRequest filter, Pageable pageable) {

        userRepository.findAgentMemberByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with user id: " + userId));

        Specification<PropertyEntity> specification = PropertySpecification
                .hasAssignedAgentId(userId);

        return propertyRepository.findAll(specification, pageable)
                .map(propertyMapper::toPublicAgencyPropertyResponse);
    }
}
