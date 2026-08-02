package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.response.AgentResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AgentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    @Mock
    private AgentService agentService;

    @InjectMocks
    private AgentController controller;

    private final CustomUserDetails currentUser = mock(CustomUserDetails.class);

    private AgentResponse buildAgent(UUID userId, UUID agencyId) {
        AgentResponse agent = AgentResponse.builder()
                .memberId(UUID.randomUUID())
                .agencyId(agencyId)
                .build();
        agent.setId(userId);
        agent.setFullName("John Agent");
        agent.setEmail("john.agent@example.com");
        agent.setEnabled(true);
        return agent;
    }

    @Test
    void getAgencyPublicInfo_returnsOk_withAgent() {
        UUID userId = UUID.randomUUID();
        AgentResponse expected = buildAgent(userId, UUID.randomUUID());

        when(agentService.getAgentByUserId(userId)).thenReturn(expected);

        ResponseEntity<ApiResponse<AgentResponse>> response =
                controller.getAgencyPublicInfo(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Agent information fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(agentService).getAgentByUserId(userId);
        verifyNoMoreInteractions(agentService);
    }

    @Test
    void getAllAgencies_returnsOk_withAgentProperties() {
        UUID userId = UUID.randomUUID();
        PropertyFilterRequest filter = new PropertyFilterRequest();
        filter.setCity("Baku");
        Pageable pageable = Pageable.ofSize(10);

        Page<PropertyResponse> page = new PageImpl<>(List.of(
                PropertyResponse.builder().id(UUID.randomUUID()).title("Assigned listing").build()
        ));

        when(agentService.getAgentProperties(userId, filter, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<PropertyResponse>>> response =
                controller.getAllAgencies(userId, filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Agent's properties fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(agentService).getAgentProperties(userId, filter, pageable);
    }

    @Test
    void getAllAgencies_returnsOk_withEmptyPage_whenNoPropertiesAssigned() {
        UUID userId = UUID.randomUUID();
        PropertyFilterRequest filter = new PropertyFilterRequest();
        Pageable pageable = Pageable.ofSize(10);

        when(agentService.getAgentProperties(any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<ApiResponse<Page<PropertyResponse>>> response =
                controller.getAllAgencies(userId, filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).isEmpty();
    }

    @Test
    void deleteAgent_returnsOk_withNoData() {
        UUID agentId = UUID.randomUUID();

        ResponseEntity<ApiResponse<Void>> response =
                controller.deleteAgent(agentId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Agent removed successfully");
        assertThat(response.getBody().getData()).isNull();

        verify(agentService).deleteAgentFromAgency(agentId, currentUser);
        verifyNoMoreInteractions(agentService);
    }

    @Test
    void deleteAgent_propagatesException_whenServiceThrows() {
        UUID agentId = UUID.randomUUID();

        org.mockito.Mockito.doThrow(new RuntimeException("Agent not found in agency"))
                .when(agentService).deleteAgentFromAgency(agentId, currentUser);

        try {
            controller.deleteAgent(agentId, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Agent not found in agency");
        }

        verify(agentService).deleteAgentFromAgency(agentId, currentUser);
    }
}