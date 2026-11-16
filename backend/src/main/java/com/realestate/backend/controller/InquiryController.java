package com.realestate.backend.controller;

import com.realestate.backend.common.response.ApiResponse;
import com.realestate.backend.dto.request.UpdateInquiryStatusRequest;
import com.realestate.backend.dto.response.InquiryResponse;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.security.principal.CustomUserDetails;
import com.realestate.backend.service.InquiryService;
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
@RequestMapping("/inquiries")
@RequiredArgsConstructor
@Tag(
        name = "Inquiries",
        description = """
                APIs for managing property inquiries, including retrieving client inquiries,
                accessing individual inquiry details, and updating inquiry statuses.
                """
)
public class InquiryController {

    private final InquiryService inquiryService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(
            summary = "Get the authenticated client's inquiries",
            description = """
                    Retrieves a paginated list of inquiries submitted by the currently
                    authenticated client.
                    Supports filtering by inquiry status and pagination.
                    Results are sorted by creation date in descending order by default.
                    Requires the CLIENT role.
                    """
    )
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getMyInquiries(
            @RequestParam(required = false) InquiryStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<InquiryResponse> response = inquiryService.getClientInquiries(currentUser, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry list fetched successfully", response)
        );

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{inquiryId}")
    @Operation(
            summary = "Get inquiry details by ID",
            description = """
                    Retrieves detailed information about a specific inquiry using its unique identifier.
                    Access is restricted according to the authenticated user's permissions
                    and relationship with the inquiry.
                    Requires authentication.
                    """
    )
    public ResponseEntity<ApiResponse<InquiryResponse>> getInquiryById(
            @PathVariable UUID inquiryId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        InquiryResponse response = inquiryService.getInquiryById(currentUser, inquiryId);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry fetched successfully", response)
        );

    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{inquiryId}/status")
    @Operation(
            summary = "Update inquiry status",
            description = """
                    Updates the status of an existing property inquiry.
                    The requested status is provided in the request body and validated
                    before processing.
                    Access is subject to the authenticated user's authorization
                    and relationship with the inquiry.
                    Requires authentication.
                    """
    )
    public ResponseEntity<ApiResponse<InquiryResponse>> updateInquiryStatus(
            @PathVariable UUID inquiryId,
            @Valid @RequestBody UpdateInquiryStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {

        InquiryResponse response = inquiryService.updateStatus(currentUser, inquiryId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Inquiry status updated successfully", response)
        );

    }
}
