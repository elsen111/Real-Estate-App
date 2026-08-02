package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AgencyAgentFilterRequest;
import com.realestate.backend.dto.request.AgencyFilterRequest;
import com.realestate.backend.dto.request.AgencyPropertyFilterRequest;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.request.UpdateAgencyRequest;
import com.realestate.backend.dto.response.AgencyLogoUploadResponse;
import com.realestate.backend.dto.response.AgencyResponse;
import com.realestate.backend.dto.response.AgencySubscriptionResponse;
import com.realestate.backend.dto.response.AppointmentResponse;
import com.realestate.backend.dto.response.InquiryResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.dto.response.UserResponse;
import com.realestate.backend.enums.AgencyStatus;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AgencyService;
import com.realestate.backend.service.AppointmentService;
import com.realestate.backend.service.InquiryService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyControllerTest {

    @Mock
    private AgencyService agencyService;

    @Mock
    private InquiryService inquiryService;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AgencyController controller;

    private final CustomUserDetails currentUser = mock(CustomUserDetails.class);

    private AgencyResponse buildAgencyResponse(UUID id) {
        return AgencyResponse.builder()
                .id(id)
                .name("Prime Realty")
                .description("Top agency")
                .email("contact@primerealty.com")
                .phoneNumber("+994501112233")
                .website("https://primerealty.com")
                .city("Baku")
                .address("Nizami St. 10")
                .totalAgents(5)
                .status(AgencyStatus.APPROVED)
                .build();
    }

    @Test
    void getCurrentAgency_returnsOk_withAgency() {
        AgencyResponse expected = buildAgencyResponse(UUID.randomUUID());

        when(agencyService.getCurrentAgency(currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<AgencyResponse>> response =
                controller.getCurrentAgency(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Current agency information fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(agencyService).getCurrentAgency(currentUser);
        verifyNoMoreInteractions(agencyService, inquiryService, appointmentService);
    }

    @Test
    void updateOwnAgency_returnsOk_withUpdatedAgency() {
        UpdateAgencyRequest request = new UpdateAgencyRequest();
        request.setName("Prime Realty Updated");
        request.setDescription("Updated description");
        request.setPhoneNumber("+994501112233");
        request.setEmail("contact@primerealty.com");
        request.setWebsite("https://primerealty.com");
        request.setCity("Baku");
        request.setAddress("Nizami St. 15");

        AgencyResponse expected = buildAgencyResponse(UUID.randomUUID());

        when(agencyService.updateOwnAgency(currentUser, request)).thenReturn(expected);

        ResponseEntity<ApiResponse<AgencyResponse>> response =
                controller.updateOwnAgency(currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Agency information updated successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(agencyService).updateOwnAgency(currentUser, request);
    }

    @Test
    void uploadAgencyLogo_returnsOk_withLogoUrl() {
        MultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", "logo-bytes".getBytes());
        AgencyLogoUploadResponse expected = AgencyLogoUploadResponse.builder()
                .logoUrl("https://cdn.example.com/logos/logo.png")
                .build();

        when(agencyService.uploadLogo(file, currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<AgencyLogoUploadResponse>> response =
                controller.uploadAgencyLogo(currentUser, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Agency logo uploaded successfully");
        assertThat(response.getBody().getData().logoUrl()).isEqualTo("https://cdn.example.com/logos/logo.png");

        verify(agencyService).uploadLogo(file, currentUser);
    }

    @Test
    void getMySubscription_returnsOk_withSubscription() {
        AgencySubscriptionResponse expected = AgencySubscriptionResponse.builder()
                .id(UUID.randomUUID())
                .planName("Gold Plan")
                .maxListings(50)
                .usedListings(10)
                .remainingListings(40)
                .maxAgents(10)
                .usedAgents(2)
                .remainingAgents(8)
                .build();

        when(agencyService.getMySubscription(currentUser)).thenReturn(expected);

        ResponseEntity<ApiResponse<AgencySubscriptionResponse>> response =
                controller.getMySubscription(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Subscription information fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(agencyService).getMySubscription(currentUser);
    }

    @Test
    void getAllProperties_returnsOk_withOwnAgencyProperties() {
        AgencyPropertyFilterRequest filter = new AgencyPropertyFilterRequest();
        filter.setQuery("apartment");
        Pageable pageable = Pageable.ofSize(10);

        Page<PropertyResponse> page = new PageImpl<>(List.of(
                PropertyResponse.builder().id(UUID.randomUUID()).title("2-room apartment").build()
        ));

        when(agencyService.getMyAgencyProperties(currentUser, filter, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<PropertyResponse>>> response =
                controller.getAllProperties(currentUser, filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Properties fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(agencyService).getMyAgencyProperties(currentUser, filter, pageable);
    }

    @Test
    void getAllAgencies_returnsOk_withPublicAgencies() {
        AgencyFilterRequest filter = new AgencyFilterRequest();
        filter.setCity("Baku");
        Pageable pageable = Pageable.ofSize(10);

        Page<AgencyResponse> page = new PageImpl<>(List.of(buildAgencyResponse(UUID.randomUUID())));

        when(agencyService.getAllPublicAgencies(filter, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<AgencyResponse>>> response =
                controller.getAllAgencies(filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Agencies fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(agencyService).getAllPublicAgencies(filter, pageable);
    }

    @Test
    void getAgencyPublicInfo_returnsOk_withAgency() {
        UUID agencyId = UUID.randomUUID();
        AgencyResponse expected = buildAgencyResponse(agencyId);

        when(agencyService.getPublicAgencyInfo(agencyId)).thenReturn(expected);

        ResponseEntity<ApiResponse<AgencyResponse>> response =
                controller.getAgencyPublicInfo(agencyId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Agency information fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(agencyService).getPublicAgencyInfo(agencyId);
    }

    @Test
    void getAllAgencies_returnsOk_withPublicPropertiesForSpecificAgency() {
        UUID agencyId = UUID.randomUUID();
        PropertyFilterRequest filter = new PropertyFilterRequest();
        filter.setCity("Baku");
        Pageable pageable = Pageable.ofSize(10);

        Page<PropertyResponse> page = new PageImpl<>(List.of(
                PropertyResponse.builder().id(UUID.randomUUID()).title("Office space").build()
        ));

        when(agencyService.getAgencyProperties(agencyId, filter, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<PropertyResponse>>> response =
                controller.getAllAgencies(agencyId, filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Properties list fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(agencyService).getAgencyProperties(agencyId, filter, pageable);
    }

    @Test
    void getAgencyAgents_returnsOk_withAgentList() {
        UUID agencyId = UUID.randomUUID();
        AgencyAgentFilterRequest filter = new AgencyAgentFilterRequest();
        filter.setEnabled(true);
        Pageable pageable = Pageable.ofSize(10);

        Page<UserResponse> page = new PageImpl<>(List.of(
                UserResponse.builder().id(UUID.randomUUID()).fullName("Agent Smith").build()
        ));

        when(agencyService.getAgencyAgents(agencyId, filter, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<UserResponse>>> response =
                controller.getAgencyAgents(agencyId, filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Agency's agents fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(agencyService).getAgencyAgents(agencyId, filter, pageable);
    }

    @Test
    void removeAgencyLogo_returnsOk_withNoData() {
        ResponseEntity<ApiResponse<Void>> response =
                controller.removeAgencyLogo(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Agency logo removed successfully");
        assertThat(response.getBody().getData()).isNull();

        verify(agencyService).removeAgencyLogo(currentUser);
    }

    @Test
    void getMyAgencyInquiries_returnsOk_withInquiryPage() {
        UUID propertyId = UUID.randomUUID();
        Pageable pageable = Pageable.ofSize(20);

        Page<InquiryResponse> page = new PageImpl<>(List.of(
                InquiryResponse.builder().id(UUID.randomUUID()).propertyId(propertyId).build()
        ));

        when(inquiryService.getMyAgencyInquiries(eq(currentUser), isNull(), eq(propertyId), eq(pageable)))
                .thenReturn(page);

        ResponseEntity<ApiResponse<Page<InquiryResponse>>> response =
                controller.getMyAgencyInquiries(null, propertyId, currentUser, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Inquiry list fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(inquiryService).getMyAgencyInquiries(currentUser, null, propertyId, pageable);
    }

    @Test
    void getMyAgencyAppointments_returnsOk_withAppointmentPage() {
        UUID propertyId = UUID.randomUUID();
        Pageable pageable = Pageable.ofSize(20);

        Page<AppointmentResponse> page = new PageImpl<>(List.of(
                AppointmentResponse.builder().id(UUID.randomUUID()).propertyId(propertyId).build()
        ));

        when(appointmentService.getMyAgencyAppointments(eq(currentUser), isNull(), eq(propertyId), eq(pageable)))
                .thenReturn(page);

        ResponseEntity<ApiResponse<Page<AppointmentResponse>>> response =
                controller.getMyAgencyAppointments(null, propertyId, currentUser, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Appointment list fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(appointmentService).getMyAgencyAppointments(currentUser, null, propertyId, pageable);
    }
}