package com.realestate.backend.service.agent;


import com.realestate.backend.dto.agent.response.AgentResponse;
import com.realestate.backend.entity.AgencyMemberEntity;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.user.UserMapper;
import com.realestate.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public AgentResponse getAgentByUserId(UUID userId) {

        AgencyMemberEntity member = userRepository.findAgentMemberByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with user id: " + userId));

        return userMapper.toAgentWithUserIdResponse(member);

    }
}
