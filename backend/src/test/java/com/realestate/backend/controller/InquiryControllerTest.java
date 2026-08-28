package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.UpdateInquiryStatusRequest;
import com.realestate.backend.dto.response.InquiryResponse;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.ratelimit.RateLimitFilter;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InquiryControllerTest {

    @Mock
    private InquiryService inquiryService;

    @Mock
    private RateLimitFilter rateLimitFilter;

    @InjectMocks
    private InquiryController controller;

    private final CustomUserDetails currentUser = mock(CustomUserDetails.class);

    private InquiryResponse buildInquiry(UUID id, InquiryStatus status) {
        return InquiryResponse.builder()
                .id(id)
                .propertyId(UUID.randomUUID())
                .agencyId(UUID.randomUUID())
                .clientId(UUID.randomUUID())
                .propertyTitle("3-room apartment")
                .clientFullName("Jane Doe")
                .status(status)
                .build();
    }

    @Test
    void getMyInquiries_returnsOk_withFilteredInquiries() {
        Pageable pageable = Pageable.ofSize(20);
        Page<InquiryResponse> page = new PageImpl<>(List.of(
                buildInquiry(UUID.randomUUID(), InquiryStatus.NEW)
        ));

        when(inquiryService.getClientInquiries(currentUser, InquiryStatus.NEW, pageable)).thenReturn(page);

        ResponseEntity<ApiResponse<Page<InquiryResponse>>> response =
                controller.getMyInquiries(InquiryStatus.NEW, currentUser, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Inquiry list fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(inquiryService).getClientInquiries(currentUser, InquiryStatus.NEW, pageable);
        verifyNoMoreInteractions(inquiryService);
    }

    @Test
    void getMyInquiries_returnsOk_withAllInquiries_whenStatusIsNull() {
        Pageable pageable = Pageable.ofSize(20);
        Page<InquiryResponse> page = new PageImpl<>(List.of(
                buildInquiry(UUID.randomUUID(), InquiryStatus.NEW),
                buildInquiry(UUID.randomUUID(), InquiryStatus.CLOSED)
        ));

        when(inquiryService.getClientInquiries(eq(currentUser), isNull(), eq(pageable))).thenReturn(page);

        ResponseEntity<ApiResponse<Page<InquiryResponse>>> response =
                controller.getMyInquiries(null, currentUser, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).hasSize(2);

        verify(inquiryService).getClientInquiries(currentUser, null, pageable);
    }

    @Test
    void getInquiryById_returnsOk_withInquiry() {
        UUID inquiryId = UUID.randomUUID();
        InquiryResponse expected = buildInquiry(inquiryId, InquiryStatus.CONTACTED);

        when(inquiryService.getInquiryById(currentUser, inquiryId)).thenReturn(expected);

        ResponseEntity<ApiResponse<InquiryResponse>> response =
                controller.getInquiryById(inquiryId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Inquiry fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(inquiryService).getInquiryById(currentUser, inquiryId);
        verifyNoMoreInteractions(inquiryService);
    }

    @Test
    void getInquiryById_propagatesException_whenInquiryNotFound() {
        UUID inquiryId = UUID.randomUUID();

        when(inquiryService.getInquiryById(currentUser, inquiryId))
                .thenThrow(new RuntimeException("Inquiry not found"));

        try {
            controller.getInquiryById(inquiryId, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Inquiry not found");
        }

        verify(inquiryService).getInquiryById(currentUser, inquiryId);
    }

    @Test
    void updateInquiryStatus_returnsOk_withUpdatedInquiry() {
        UUID inquiryId = UUID.randomUUID();
        UpdateInquiryStatusRequest request = new UpdateInquiryStatusRequest();
        request.setStatus(InquiryStatus.CLOSED);

        InquiryResponse expected = buildInquiry(inquiryId, InquiryStatus.CLOSED);

        when(inquiryService.updateStatus(currentUser, inquiryId, request)).thenReturn(expected);

        ResponseEntity<ApiResponse<InquiryResponse>> response =
                controller.updateInquiryStatus(inquiryId, request, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Inquiry status updated successfully");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(inquiryService).updateStatus(currentUser, inquiryId, request);
        verifyNoMoreInteractions(inquiryService);
    }

    @Test
    void updateInquiryStatus_propagatesException_whenServiceThrows() {
        UUID inquiryId = UUID.randomUUID();
        UpdateInquiryStatusRequest request = new UpdateInquiryStatusRequest();
        request.setStatus(InquiryStatus.CONTACTED);

        when(inquiryService.updateStatus(currentUser, inquiryId, request))
                .thenThrow(new RuntimeException("Inquiry cannot be updated"));

        try {
            controller.updateInquiryStatus(inquiryId, request, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Inquiry cannot be updated");
        }

        verify(inquiryService).updateStatus(currentUser, inquiryId, request);
    }
}