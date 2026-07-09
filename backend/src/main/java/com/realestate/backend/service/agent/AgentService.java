package com.realestate.backend.service.agent;

import com.realestate.backend.dto.agent.response.AgentResponse;
import com.realestate.backend.dto.property.request.PropertyFilterRequest;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.CustomUserDetailsService;
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
