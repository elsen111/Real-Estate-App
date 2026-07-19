package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.UpdateAppointmentStatusRequest;
import com.realestate.backend.dto.response.AppointmentResponse;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/me")
    @Operation(summary = "Get client's appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<AppointmentResponse> response = appointmentService.getClientAppointments(currentUser, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Appointment list fetched successfully", response)
        );

    }

    @PatchMapping("/{appointmentId}/cancel")
    @Operation(summary = "Cancel a pending or approved appointment (client user)")
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
    @Operation(summary = "Update appointment status")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointmentStatus(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody UpdateAppointmentStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        AppointmentResponse response = appointmentService.updateStatus(currentUser, appointmentId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Appointment status updated to "
                        + request.getStatus() + " successfully.", response)
        );

    }


}
