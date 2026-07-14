package com.realestate.backend.service.inquiry;

import com.realestate.backend.dto.inquiry.request.CreateInquiryRequest;
import com.realestate.backend.dto.inquiry.response.InquiryClientResponse;
import com.realestate.backend.entity.InquiryEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.DuplicateInquiryException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.inquiry.InquiryMapper;
import com.realestate.backend.repository.InquiryRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService{

    private final InquiryRepository inquiryRepository;
    private final InquiryMapper inquiryMapper;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    @Override
    @Transactional
    public InquiryClientResponse createInquiry(UUID propertyId, CreateInquiryRequest request, CustomUserDetails currentUser) {

        UserEntity client = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Property not found with id: " + propertyId)
                );

        if(property.getStatus() != PropertyStatus.ACTIVE){
            throw new ResourceNotFoundException("Active property not found with id: " + propertyId);
        }

        boolean hasOpenInquiry = inquiryRepository.existsByPropertyIdAndClientIdAndStatusNot(
                propertyId,
                client.getId(),
                InquiryStatus.CLOSED
        );

        if (hasOpenInquiry){
            throw new DuplicateInquiryException("Inquiry already exists with id: " + propertyId);
        };

        InquiryEntity newInquiry = InquiryEntity.builder()
                .property(property)
                .client(client)
                .agency(property.getAgency())
                .assignedAgent(property.getAssignedAgent())
                .message(request.getMessage())
                .preferredContactMethod(request.getPreferredContactMethod())
                .build();

        InquiryEntity savedInquiry = inquiryRepository.saveAndFlush(newInquiry);

        return inquiryMapper.toClientResponse(savedInquiry);

    }
}
