package com.realestate.backend.controller.agency;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.agency.response.AgencyResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.agency.AgencyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agencies")
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;

    @GetMapping("/me")
    @Operation(summary = "Get current agency information.")
    public ResponseEntity<ApiResponse<AgencyResponse>> getCurrentAgency(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ){

        AgencyResponse response = agencyService.getCurrentAgency(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Current agency information fetched successfully", response)
        );

    }

}
