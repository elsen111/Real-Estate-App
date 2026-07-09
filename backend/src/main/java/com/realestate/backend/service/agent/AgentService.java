package com.realestate.backend.service.agent;

import com.realestate.backend.dto.agent.response.AgentResponse;

import java.util.UUID;

public interface AgentService {
    AgentResponse getAgentByUserId(UUID userId);
}
