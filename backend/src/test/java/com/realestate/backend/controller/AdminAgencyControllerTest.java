package com.realestate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realestate.backend.dto.request.AgencyStatusRequest;
import com.realestate.backend.dto.request.UpdateAgencyRequest;
import com.realestate.backend.dto.response.AdminAgencyResponse;
import com.realestate.backend.dto.response.AgencySubscriptionResponse;
import com.realestate.backend.enums.AgencyStatus;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.security.CustomUserDetailsService;
import com.realestate.backend.security.JwtService;
import com.realestate.backend.service.AdminAgencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminAgencyController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = {"ADMIN"})
class AdminAgencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AdminAgencyService adminAgencyService; // (or CategoryService for the other test)

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UUID agencyId;
    private UUID subscriptionId;
    private AdminAgencyResponse agencyResponse;
    private AgencySubscriptionResponse subscriptionResponse;

    @BeforeEach
    void setUp() {
        agencyId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();

        agencyResponse = AdminAgencyResponse.builder()
                .id(agencyId)
                .name("Acme Realty")
                .email("contact@acme-realty.com")
                .city("Baku")
                .status(AgencyStatus.PENDING)
                .isDeleted(false)
                .build();

        subscriptionResponse = AgencySubscriptionResponse.builder()
                .id(UUID.randomUUID())
                .planId(subscriptionId)
                .planName("Premium")
                .price(BigDecimal.valueOf(99.99))
                .durationDays(30)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .subscriptionStatus(SubscriptionStatus.ACTIVE)
                .maxListings(50)
                .usedListings(5)
                .remainingListings(45)
                .maxAgents(10)
                .usedAgents(2)
                .remainingAgents(8)
                .build();
    }

    private UpdateAgencyRequest validUpdateAgencyRequest() {
        UpdateAgencyRequest request = new UpdateAgencyRequest();
        request.setName("Acme Realty Updated");
        request.setDescription("An even better leading agency");
        request.setPhoneNumber("+994501234567");
        request.setEmail("updated@acme-realty.com");
        request.setWebsite("https://acme-realty.com");
        request.setCity("Baku");
        request.setAddress("Nizami Street 12");
        return request;
    }

    @Test
    void getAllAgencies_returnsPagedAgencies() throws Exception {
        when(adminAgencyService.getAllAgencies(any(), any()))
                .thenReturn(new PageImpl<>(List.of(agencyResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/admin/agencies")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Agencies fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].id").value(agencyId.toString()))
                .andExpect(jsonPath("$.data.content[0].name").value("Acme Realty"));
    }

    @Test
    void getAgencyById_returnsAgency_whenAgencyExists() throws Exception {
        when(adminAgencyService.getAgencyById(agencyId)).thenReturn(agencyResponse);

        mockMvc.perform(get("/admin/agencies/{agencyId}", agencyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Agency fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(agencyId.toString()))
                .andExpect(jsonPath("$.data.email").value("contact@acme-realty.com"));
    }

    @Test
    void getAgencyById_returnsNotFound_whenAgencyDoesNotExist() throws Exception {
        when(adminAgencyService.getAgencyById(agencyId))
                .thenThrow(new ResourceNotFoundException("Agency not found"));

        mockMvc.perform(get("/admin/agencies/{agencyId}", agencyId))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeAgencyStatus_returnsSuccessMessage_whenValidRequest() throws Exception {
        AgencyStatusRequest request = new AgencyStatusRequest();
        request.setStatus(AgencyStatus.APPROVED);

        when(adminAgencyService.changeAgencyStatus(agencyId, AgencyStatus.APPROVED))
                .thenReturn("Agency status updated successfully");

        mockMvc.perform(put("/admin/agencies/{agencyId}/status", agencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Agency status updated successfully"));
    }

    @Test
    void changeAgencyStatus_returnsBadRequest_whenStatusIsMissing() throws Exception {
        mockMvc.perform(put("/admin/agencies/{agencyId}/status", agencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAccount_returnsSuccessMessage_whenAgencyExists() throws Exception {
        when(adminAgencyService.softDeleteAgency(agencyId))
                .thenReturn("Agency deleted successfully");

        mockMvc.perform(delete("/admin/agencies/{agencyId}", agencyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Agency deleted successfully"));
    }

    @Test
    void assignSubscriptionPlan_returnsCreatedSubscription() throws Exception {
        when(adminAgencyService.createAgencySubscription(agencyId, subscriptionId))
                .thenReturn(subscriptionResponse);

        mockMvc.perform(post("/admin/agencies/{agencyId}/subscription-plans/{subscriptionId}",
                        agencyId, subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Agency assigned successfully"))
                .andExpect(jsonPath("$.data.planName").value("Premium"))
                .andExpect(jsonPath("$.data.remainingListings").value(45));
    }

    @Test
    void getAgencySubscription_returnsSubscription_whenExists() throws Exception {
        when(adminAgencyService.getAgencySubscription(agencyId)).thenReturn(subscriptionResponse);

        mockMvc.perform(get("/admin/agencies/{agencyId}/subscription-plan", agencyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Agency subscription fetched successfully"))
                .andExpect(jsonPath("$.data.planId").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.data.subscriptionStatus").value("ACTIVE"));
    }

    @Test
    void getAgencySubscription_returnsNotFound_whenNoSubscriptionExists() throws Exception {
        when(adminAgencyService.getAgencySubscription(agencyId))
                .thenThrow(new ResourceNotFoundException("No subscription found for this agency"));

        mockMvc.perform(get("/admin/agencies/{agencyId}/subscription-plan", agencyId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAgency_returnsUpdatedAgency_whenValidRequest() throws Exception {
        UpdateAgencyRequest request = validUpdateAgencyRequest();

        AdminAgencyResponse updatedResponse = AdminAgencyResponse.builder()
                .id(agencyId)
                .name(request.getName())
                .description(request.getDescription())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .website(request.getWebsite())
                .city(request.getCity())
                .address(request.getAddress())
                .status(AgencyStatus.APPROVED)
                .isDeleted(false)
                .build();

        when(adminAgencyService.updateAgency(eq(agencyId), any(UpdateAgencyRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/admin/agencies/{agencyId}", agencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Agency information updated successfully"))
                .andExpect(jsonPath("$.data.id").value(agencyId.toString()))
                .andExpect(jsonPath("$.data.name").value("Acme Realty Updated"))
                .andExpect(jsonPath("$.data.email").value("updated@acme-realty.com"))
                .andExpect(jsonPath("$.data.website").value("https://acme-realty.com"));

        verify(adminAgencyService).updateAgency(eq(agencyId), any(UpdateAgencyRequest.class));
    }

    @Test
    void updateAgency_returnsNotFound_whenAgencyDoesNotExist() throws Exception {
        UpdateAgencyRequest request = validUpdateAgencyRequest();

        when(adminAgencyService.updateAgency(eq(agencyId), any(UpdateAgencyRequest.class)))
                .thenThrow(new ResourceNotFoundException("Agency not found with id: " + agencyId));

        mockMvc.perform(put("/admin/agencies/{agencyId}", agencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAgency_returnsBadRequest_whenEmailAlreadyExistsForAnotherAgency() throws Exception {
        UpdateAgencyRequest request = validUpdateAgencyRequest();

        when(adminAgencyService.updateAgency(eq(agencyId), any(UpdateAgencyRequest.class)))
                .thenThrow(new BadRequestException("Email already exists for another agency."));

        mockMvc.perform(put("/admin/agencies/{agencyId}", agencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAgency_returnsBadRequest_whenNameIsBlank() throws Exception {
        UpdateAgencyRequest request = validUpdateAgencyRequest();
        request.setName(" ");

        mockMvc.perform(put("/admin/agencies/{agencyId}", agencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminAgencyService, never())
                .updateAgency(any(UUID.class), any(UpdateAgencyRequest.class));
    }

    @Test
    void updateAgency_returnsBadRequest_whenEmailIsInvalid() throws Exception {
        UpdateAgencyRequest request = validUpdateAgencyRequest();
        request.setEmail("not-an-email");

        mockMvc.perform(put("/admin/agencies/{agencyId}", agencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminAgencyService, never())
                .updateAgency(any(UUID.class), any(UpdateAgencyRequest.class));
    }

    @Test
    void updateAgency_returnsBadRequest_whenPhoneNumberIsInvalid() throws Exception {
        UpdateAgencyRequest request = validUpdateAgencyRequest();
        request.setPhoneNumber("abc-not-a-phone");

        mockMvc.perform(put("/admin/agencies/{agencyId}", agencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminAgencyService, never())
                .updateAgency(any(UUID.class), any(UpdateAgencyRequest.class));
    }
}