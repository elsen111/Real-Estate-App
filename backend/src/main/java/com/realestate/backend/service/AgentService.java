package com.realestate.backend.service;

import com.realestate.backend.dto.response.AgentResponse;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AgentService {

    AgentResponse getAgentByUserId(UUID userId);

    Page<PropertyResponse> getAgentProperties(
            UUID  userId,
            PropertyFilterRequest filter,
            Pageable pageable
    );

    void deleteAgentFromAgency(UUID agentId, CustomUserDetails currentUser );
}
