package com.realestate.backend.controller.admin;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.admin.agency.request.AdminPropertyFilterRequest;
import com.realestate.backend.dto.admin.agency.response.AdminAgencyResponse;
import com.realestate.backend.dto.admin.property.response.AdminPropertyResponse;
import com.realestate.backend.service.admin.property.AdminPropertyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/properties")
@RequiredArgsConstructor
public class AdminPropertyController {

    private final AdminPropertyService  adminPropertyService;

    @GetMapping
    @Operation(summary = "Get all properties")
    public ResponseEntity<ApiResponse<Page<AdminPropertyResponse>>> getAllProperties(
            @ModelAttribute AdminPropertyFilterRequest filter,
            @PageableDefault(sort = "createdAt")
            Pageable pageable
            ) {

        Page<AdminPropertyResponse> response = adminPropertyService.getAllProperties(filter, pageable);

        ApiResponse<Page<AdminPropertyResponse>> apiResponse =
                ApiResponse.success("Properties fetched successfully", response);

        return ResponseEntity.ok(apiResponse);

    }

}
