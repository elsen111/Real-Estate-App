package com.realestate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realestate.backend.dto.request.PropertyStatusRequest;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.security.CustomUserDetailsService;
import com.realestate.backend.security.JwtService;
import com.realestate.backend.service.AdminPropertyService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminPropertyController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = {"ADMIN"})
class AdminPropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AdminPropertyService adminPropertyService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UUID propertyId;
    private PropertyResponse propertyResponse;

    @BeforeEach
    void setUp() {
        propertyId = UUID.randomUUID();

        propertyResponse = PropertyResponse.builder()
                .id(propertyId)
                .title("Modern Downtown Apartment")
                .description("Spacious 2-bedroom apartment in the city center")
                .agencyId(UUID.randomUUID())
                .agencyName("Acme Realty")
                .categoryId(UUID.randomUUID())
                .categoryName("Apartments")
                .price(BigDecimal.valueOf(250000))
                .city("Baku")
                .district("Nasimi")
                .address("123 Main St")
                .rooms(2)
                .bathrooms(1)
                .floor(4)
                .totalFloors(9)
                .status(PropertyStatus.PENDING)
                .featured(false)
                .viewCount(0L)
                .build();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void getAllProperties_returnsPagedProperties() throws Exception {
        when(adminPropertyService.getAllProperties(any(), any()))
                .thenReturn(new PageImpl<>(List.of(propertyResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/admin/properties")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Properties fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].id").value(propertyId.toString()))
                .andExpect(jsonPath("$.data.content[0].title").value("Modern Downtown Apartment"));
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void getAllProperties_returnsFilteredProperties_whenFilterParamsProvided() throws Exception {
        when(adminPropertyService.getAllProperties(any(), any()))
                .thenReturn(new PageImpl<>(List.of(propertyResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/admin/properties")
                        .param("city", "Baku")
                        .param("status", "PENDING")
                        .param("featured", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].city").value("Baku"));
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void changeAgencyStatus_returnsSuccessMessage_whenValidRequest() throws Exception {
        PropertyStatusRequest request = new PropertyStatusRequest();
        request.setStatus(PropertyStatus.ACTIVE);

        when(adminPropertyService.changePropertyStatus(propertyId, PropertyStatus.ACTIVE))
                .thenReturn("Property status updated successfully");

        mockMvc.perform(put("/admin/properties/{propertyId}/status", propertyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Property status updated successfully"));
    }

    @Test
    void changeAgencyStatus_returnsBadRequest_whenStatusIsMissing() throws Exception {
        mockMvc.perform(put("/admin/properties/{propertyId}/status", propertyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeAgencyStatus_returnsBadRequest_whenBodyIsMissing() throws Exception {
        mockMvc.perform(put("/admin/properties/{propertyId}/status", propertyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void changeAgencyStatus_returnsNotFound_whenPropertyDoesNotExist() throws Exception {
        PropertyStatusRequest request = new PropertyStatusRequest();
        request.setStatus(PropertyStatus.REJECTED);

        when(adminPropertyService.changePropertyStatus(eq(propertyId), eq(PropertyStatus.REJECTED)))
                .thenThrow(new ResourceNotFoundException("Property not found"));

        mockMvc.perform(put("/admin/properties/{propertyId}/status", propertyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}