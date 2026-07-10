package com.realestate.backend.controller.property;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.agent.response.AgentResponse;
import com.realestate.backend.dto.property.request.CreatePropertyRequest;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.property.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @Operation(summary = "Create a new property.")
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Valid @RequestBody CreatePropertyRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
            ){

        PropertyResponse response = propertyService.createProperty(request, currentUser);

        return ResponseEntity.ok(
                ApiResponse.success("Property created successfully", response)
        );

    }

}
