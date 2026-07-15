package com.realestate.backend.service.inquiry;

import com.realestate.backend.dto.inquiry.request.CreateInquiryRequest;
import com.realestate.backend.dto.inquiry.response.InquiryResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.DuplicateInquiryException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.exception.UnauthorizedException;
import com.realestate.backend.mapper.inquiry.InquiryMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
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
    private final AgencyMemberRepository agencyMemberRepository;
    private final AgencyRepository agencyRepository;

    @Override
    @Transactional
    public InquiryResponse createInquiry(UUID propertyId, CreateInquiryRequest request, CustomUserDetails currentUser) {

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

        return inquiryMapper.toResponse(savedInquiry);

    }

    @Override
    public Page<InquiryResponse> getClientInquiries(CustomUserDetails currentUser, InquiryStatus status, Pageable pageable) {

        UserEntity client = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        Page<InquiryEntity> inquiries = status == null
                ? inquiryRepository.findByClientId(currentUser.getId(), pageable)
                : inquiryRepository.findByClientIdAndStatus(currentUser.getId(), status, pageable);

        return inquiries.map(inquiryMapper::toResponse);

    }

    @Override
    public Page<InquiryResponse> getMyAgencyInquiries(
            CustomUserDetails currentUser, InquiryStatus status,
            UUID propertyId, Pageable pageable) {

        AgencyMemberEntity agencyMember = agencyMemberRepository.findByUser_IdAndActiveTrue(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "You are not an active member of any agency"));

        UUID agencyId = agencyMember.getAgency().getId();

        if (!hasRole(currentUser, "AGENCY_OWNER") && !hasRole(currentUser, "AGENT")) {
            throw new ForbiddenException("You do not have permission to view agency inquiries");
        }

        Page<InquiryEntity> inquiries = inquiryRepository
                .findByAgencyIdWithFilters(agencyId, status, propertyId, pageable);

        return inquiries.map(inquiryMapper::toResponse);
    }

    @Override
    public InquiryResponse getInquiryById(CustomUserDetails currentUser, UUID inquiryId) {

        InquiryEntity inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found with id: " + inquiryId));

        if (!canViewInquiry(inquiry, currentUser)) {
            throw new ResourceNotFoundException("Inquiry not found with id: " + inquiryId);
        }

        return inquiryMapper.toResponse(inquiry);
    }

    private boolean canViewInquiry(InquiryEntity inquiry, CustomUserDetails currentUser) {
        if (hasRole(currentUser, "SUPER_ADMIN")) {
            return true;
        }

        boolean isOwnInquiry = inquiry.getClient().getId().equals(currentUser.getId());

        boolean isAgencyMember = inquiry.getAgency() != null
                && agencyMemberRepository.existsByAgencyIdAndUserIdAndActiveTrue(
                inquiry.getAgency().getId(), currentUser.getId());

        return isOwnInquiry || isAgencyMember;
    }

    private boolean canViewAgencyInquiries(UUID agencyId, CustomUserDetails currentUser) {
        if (hasRole(currentUser, "SUPER_ADMIN")) {
            return true;
        }

        boolean isAuthorizedRole = hasRole(currentUser, "AGENCY_OWNER") || hasRole(currentUser, "AGENT");

        return isAuthorizedRole
                && agencyMemberRepository.existsByAgencyIdAndUserIdAndActiveTrue(agencyId, currentUser.getId());
    }

    private boolean hasRole(CustomUserDetails user, String roleName) {
        String target = SecurityConstants.ROLE_PREFIX + roleName;
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(target::equals);
    }
}
