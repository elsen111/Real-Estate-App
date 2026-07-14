package com.realestate.backend.service.inquiry;

import com.realestate.backend.dto.inquiry.request.CreateInquiryRequest;
import com.realestate.backend.dto.inquiry.response.InquiryClientResponse;
import com.realestate.backend.entity.InquiryEntity;
import com.realestate.backend.security.CustomUserDetails;

import java.util.UUID;

public interface InquiryService {

    InquiryClientResponse createInquiry(UUID propertyId, CreateInquiryRequest request, CustomUserDetails currentUser);

}
