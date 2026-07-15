package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.appointment.request.CreateAppointmentRequest;
import com.realestate.backend.dto.appointment.response.AppointmentResponse;
import com.realestate.backend.dto.inquiry.request.CreateInquiryRequest;
import com.realestate.backend.dto.inquiry.response.InquiryResponse;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.appointment.AppointmentService;
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
@RequestMapping
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/properties/{propertyId}/appointments ")
    @Operation(summary = "Create a new appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment (
            @PathVariable UUID propertyId,
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        AppointmentResponse response = appointmentService.createAppointment(propertyId, request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Appointment created successfully", response)
        );

    }

    @GetMapping("/appointments/me")
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

    @PatchMapping("/appointments/{appointmentId}/cancel")
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


}
