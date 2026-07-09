package com.realestate.backend.controller.agent;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.dto.agent.response.AgentResponse;
import com.realestate.backend.service.agent.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get agent information.")
    public ResponseEntity<ApiResponse<AgentResponse>> getAgencyPublicInfo(
            @PathVariable UUID userId
    ){

         AgentResponse response = agentService.getAgentByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Agent information fetched successfully", response)
        );

    }

}
