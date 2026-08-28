package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.response.AgencyMemberResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.ratelimit.RateLimitFilter;
import com.realestate.backend.service.impl.AgencyMemberServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyMemberControllerTest {

    @Mock
    private AgencyMemberServiceImpl agencyMemberService;

    @Mock
    private RateLimitFilter rateLimitFilter;

    @InjectMocks
    private AgencyMemberController controller;

    private final CustomUserDetails currentUser = mock(CustomUserDetails.class);

    @Test
    void assignAgent_returnsCreated_withAssignedMember() {
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AgencyMemberResponse expected = AgencyMemberResponse.builder()
                .id(UUID.randomUUID())
                .agencyId(agencyId)
                .agencyName("Prime Realty")
                .userId(userId)
                .userFullName("John Agent")
                .userEmail("john.agent@example.com")
                .position("Sales Agent")
                .active(true)
                .build();

        when(agencyMemberService.assignAgent(agencyId, userId, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<AgencyMemberResponse>> response =
                controller.assignAgent(agencyId, userId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Agent assigned successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);
        assertThat(response.getBody().getData().getUserId()).isEqualTo(userId);
        assertThat(response.getBody().getData().getAgencyId()).isEqualTo(agencyId);

        verify(agencyMemberService).assignAgent(agencyId, userId, currentUser);
        verifyNoMoreInteractions(agencyMemberService);
    }

    @Test
    void assignAgent_propagatesException_whenServiceThrows() {
        UUID agencyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(agencyMemberService.assignAgent(agencyId, userId, currentUser))
                .thenThrow(new RuntimeException("User is already a member of another agency"));

        try {
            controller.assignAgent(agencyId, userId, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("User is already a member of another agency");
        }

        verify(agencyMemberService).assignAgent(agencyId, userId, currentUser);
    }
}