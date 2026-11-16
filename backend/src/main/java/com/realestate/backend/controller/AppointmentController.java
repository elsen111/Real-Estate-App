package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.AppointmentFilterRequest;
import com.realestate.backend.dto.request.UpdateAppointmentStatusRequest;
import com.realestate.backend.dto.response.AppointmentResponse;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Tag(
        name = "Appointments",
        description = """
                APIs for managing client appointments, retrieving appointment details,
                cancelling appointments, updating appointment statuses, and administrative
                appointment management.
                """
)
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(
            summary = "Get current client's appointments",
            description = """
                    Retrieves a paginated list of appointments belonging to the currently
                    authenticated client.
                    Supports filtering by appointment status and pagination.
                    Results are sorted by creation date in descending order by default.
                    Requires the CLIENT role.
                    """
    )
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        Page<AppointmentResponse> response = appointmentService.getClientAppointments(currentUser, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Appointment list fetched successfully", response)
        );

    }

    @PatchMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(
            summary = "Cancel a client appointment",
            description = """
                    Cancels an appointment belonging to the currently authenticated client.
                    Only appointments with PENDING or APPROVED status can be cancelled.
                    Requires the CLIENT role.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        appointmentService.cancelAppointment(appointmentId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Appointment cancelled successfully", null)
        );

    }

    @PatchMapping("/{appointmentId}/status")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER','AGENT')")
    @Operation(
            summary = "Update appointment status",
            description = """
                    Updates the status of an existing appointment.
                    The requested status is provided in the request body and validated
                    before processing.
                    Requires the AGENCY_OWNER or AGENT role.
                    """
    )
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointmentStatus(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody UpdateAppointmentStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        AppointmentResponse response = appointmentService.updateStatus(currentUser, appointmentId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Appointment status updated to "
                                + request.getStatus() + " successfully.", response
                )
        );

    }

    @GetMapping("/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get appointment details by ID",
            description = """
                    Retrieves detailed information about a specific appointment using its ID.
                    Access is restricted according to the authenticated user's permissions
                    and appointment ownership or agency membership.
                    Requires authentication.
                    """
    )
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        AppointmentResponse response = appointmentService.getAppointmentById(currentUser, appointmentId);

        return ResponseEntity.ok(
                ApiResponse.success("Appointment fetched successfully.", response)
        );
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @GetMapping
    @Operation(
            summary = "Get all appointments",
            description = """
                    Retrieves a paginated list of appointments across the platform.
                    Supports filtering through appointment filter parameters and pagination.
                    Requires the SUPER_ADMIN or ADMIN role.
                    """
    )
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAllAppointments(
            @ModelAttribute AppointmentFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
    ) {

        Page<AppointmentResponse> response = appointmentService.getAllAppointments(filter, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                "All appointments fetched successfully", response
        ));

    }


}
