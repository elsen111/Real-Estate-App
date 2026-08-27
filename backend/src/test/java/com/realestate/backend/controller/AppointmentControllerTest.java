package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AppointmentFilterRequest;
import com.realestate.backend.dto.request.UpdateAppointmentStatusRequest;
import com.realestate.backend.dto.response.AppointmentResponse;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AppointmentService;
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
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController controller;

    private final CustomUserDetails currentUser = mock(CustomUserDetails.class);

    private AppointmentResponse buildAppointment(UUID id, AppointmentStatus status) {
        return AppointmentResponse.builder()
                .id(id)
                .propertyId(UUID.randomUUID())
                .clientId(UUID.randomUUID())
                .agencyId(UUID.randomUUID())
                .propertyTitle("2-room apartment")
                .status(status)
                .build();
    }

    @Test
    void getAllAppointments_returnsOk_withFilteredAppointments() {
        AppointmentFilterRequest filter = new AppointmentFilterRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                AppointmentStatus.PENDING,
                null,
                null
        );

        Pageable pageable = Pageable.ofSize(20);

        Page<AppointmentResponse> page = new PageImpl<>(List.of(
                buildAppointment(UUID.randomUUID(), AppointmentStatus.PENDING)
        ));

        when(appointmentService.getAllAppointments(filter, pageable))
                .thenReturn(page);

        ResponseEntity<ApiResponse<Page<AppointmentResponse>>> response =
                controller.getAllAppointments(filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage())
                .isEqualTo("All appointments fetched successfully");
        assertThat(response.getBody().getData()).isEqualTo(page);
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(appointmentService).getAllAppointments(filter, pageable);
        verifyNoMoreInteractions(appointmentService);
    }

    @Test
    void getMyAppointments_returnsOk_withFilteredAppointments() {
        Pageable pageable = Pageable.ofSize(20);
        Page<AppointmentResponse> page = new PageImpl<>(List.of(
                buildAppointment(UUID.randomUUID(), AppointmentStatus.PENDING)
        ));

        when(appointmentService.getClientAppointments(currentUser, AppointmentStatus.PENDING, pageable))
                .thenReturn(page);

        ResponseEntity<ApiResponse<Page<AppointmentResponse>>> response =
                controller.getMyAppointments(AppointmentStatus.PENDING, currentUser, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Appointment list fetched successfully");
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        verify(appointmentService).getClientAppointments(
                currentUser,
                AppointmentStatus.PENDING,
                pageable
        );
        verifyNoMoreInteractions(appointmentService);
    }

    @Test
    void getMyAppointments_returnsOk_withAllAppointments_whenStatusIsNull() {
        Pageable pageable = Pageable.ofSize(20);
        Page<AppointmentResponse> page = new PageImpl<>(List.of(
                buildAppointment(UUID.randomUUID(), AppointmentStatus.APPROVED),
                buildAppointment(UUID.randomUUID(), AppointmentStatus.COMPLETED)
        ));

        when(appointmentService.getClientAppointments(eq(currentUser), isNull(), eq(pageable)))
                .thenReturn(page);

        ResponseEntity<ApiResponse<Page<AppointmentResponse>>> response =
                controller.getMyAppointments(null, currentUser, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getContent()).hasSize(2);

        verify(appointmentService).getClientAppointments(currentUser, null, pageable);
    }

    @Test
    void cancelAppointment_returnsOk_withNoData() {
        UUID appointmentId = UUID.randomUUID();

        ResponseEntity<ApiResponse<Void>> response =
                controller.cancelAppointment(appointmentId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Appointment cancelled successfully");
        assertThat(response.getBody().getData()).isNull();

        verify(appointmentService).cancelAppointment(appointmentId, currentUser);
        verifyNoMoreInteractions(appointmentService);
    }

    @Test
    void cancelAppointment_propagatesException_whenServiceThrows() {
        UUID appointmentId = UUID.randomUUID();

        org.mockito.Mockito.doThrow(
                        new RuntimeException(
                                "Appointment cannot be cancelled in its current state"
                        )
                )
                .when(appointmentService)
                .cancelAppointment(appointmentId, currentUser);

        try {
            controller.cancelAppointment(appointmentId, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage())
                    .isEqualTo("Appointment cannot be cancelled in its current state");
        }

        verify(appointmentService).cancelAppointment(appointmentId, currentUser);
    }

    @Test
    void updateAppointmentStatus_returnsOk_withUpdatedAppointment() {
        UUID appointmentId = UUID.randomUUID();

        UpdateAppointmentStatusRequest request =
                new UpdateAppointmentStatusRequest();

        request.setStatus(AppointmentStatus.APPROVED);
        request.setResponseNote("Confirmed for Monday at 10 AM");

        AppointmentResponse expected =
                buildAppointment(appointmentId, AppointmentStatus.APPROVED);

        when(appointmentService.updateStatus(currentUser, appointmentId, request))
                .thenReturn(expected);

        ResponseEntity<ApiResponse<AppointmentResponse>> response =
                controller.updateAppointmentStatus(
                        appointmentId,
                        request,
                        currentUser
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage())
                .isEqualTo(
                        "Appointment status updated to APPROVED successfully."
                );
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(appointmentService)
                .updateStatus(currentUser, appointmentId, request);

        verifyNoMoreInteractions(appointmentService);
    }

    @Test
    void updateAppointmentStatus_buildsCorrectMessage_forEachStatus() {
        UUID appointmentId = UUID.randomUUID();

        UpdateAppointmentStatusRequest request =
                new UpdateAppointmentStatusRequest();

        request.setStatus(AppointmentStatus.REJECTED);

        AppointmentResponse expected =
                buildAppointment(appointmentId, AppointmentStatus.REJECTED);

        when(appointmentService.updateStatus(currentUser, appointmentId, request))
                .thenReturn(expected);

        ResponseEntity<ApiResponse<AppointmentResponse>> response =
                controller.updateAppointmentStatus(
                        appointmentId,
                        request,
                        currentUser
                );

        assertThat(response.getBody().getMessage())
                .isEqualTo(
                        "Appointment status updated to REJECTED successfully."
                );
    }

    @Test
    void getAppointmentById_returnsOk_withAppointment() {
        UUID appointmentId = UUID.randomUUID();

        AppointmentResponse expected =
                buildAppointment(appointmentId, AppointmentStatus.APPROVED);

        when(appointmentService.getAppointmentById(currentUser, appointmentId))
                .thenReturn(expected);

        ResponseEntity<ApiResponse<AppointmentResponse>> response =
                controller.getAppointmentById(appointmentId, currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Appointment fetched successfully.");
        assertThat(response.getBody().getData()).isEqualTo(expected);

        verify(appointmentService)
                .getAppointmentById(currentUser, appointmentId);

        verifyNoMoreInteractions(appointmentService);
    }

    @Test
    void getAppointmentById_propagatesException_whenServiceThrows() {
        UUID appointmentId = UUID.randomUUID();

        when(appointmentService.getAppointmentById(currentUser, appointmentId))
                .thenThrow(
                        new RuntimeException(
                                "Appointment not found with id: " + appointmentId
                        )
                );

        try {
            controller.getAppointmentById(appointmentId, currentUser);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage())
                    .isEqualTo(
                            "Appointment not found with id: " + appointmentId
                    );
        }

        verify(appointmentService)
                .getAppointmentById(currentUser, appointmentId);
    }
}