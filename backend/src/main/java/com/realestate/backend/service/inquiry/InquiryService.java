package com.realestate.backend.service.inquiry;

import com.realestate.backend.dto.inquiry.request.CreateInquiryRequest;
import com.realestate.backend.dto.inquiry.response.InquiryClientResponse;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InquiryService {

    InquiryClientResponse createInquiry(UUID propertyId, CreateInquiryRequest request, CustomUserDetails currentUser);

    Page<InquiryClientResponse> getClientInquiries(CustomUserDetails currentUser, InquiryStatus status, Pageable pageable);

}
