package com.realestate.backend.service;

import com.realestate.backend.dto.request.CreateInquiryRequest;
import com.realestate.backend.dto.request.UpdateInquiryStatusRequest;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.DuplicateInquiryException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.InquiryRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.InquiryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InquiryServiceImplTest {

    @Mock private InquiryRepository inquiryRepository;
    @Mock private UserRepository userRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private AgencyMemberRepository agencyMemberRepository;

    @InjectMocks private InquiryServiceImpl service;

    private CustomUserDetails clientUser(UUID id) {
        return CustomUserDetails.from(UserEntity.builder().id(id)
                .roles(Set.of(RoleEntity.builder().roleName(Role.CLIENT).build())).build());
    }

    @Test
    void createInquiry_throws_whenPropertyNotActive() {
        UUID propertyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PropertyEntity property = PropertyEntity.builder().id(propertyId).status(PropertyStatus.REJECTED).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(UserEntity.builder().id(userId).build()));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        assertThatThrownBy(() -> service.createInquiry(propertyId, new CreateInquiryRequest(), clientUser(userId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createInquiry_throws_whenOpenInquiryAlreadyExists() {
        UUID propertyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PropertyEntity property = PropertyEntity.builder().id(propertyId).status(PropertyStatus.ACTIVE).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(UserEntity.builder().id(userId).build()));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(inquiryRepository.existsByPropertyIdAndClientIdAndStatusNot(propertyId, userId, InquiryStatus.CLOSED))
                .thenReturn(true);

        assertThatThrownBy(() -> service.createInquiry(propertyId, new CreateInquiryRequest(), clientUser(userId)))
                .isInstanceOf(DuplicateInquiryException.class);
    }

    @Test
    void getMyAgencyInquiries_throws_whenNotActiveAgencyMember() {
        CustomUserDetails owner = clientUser(UUID.randomUUID());
        when(agencyMemberRepository.findByUser_IdAndActiveTrue(owner.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMyAgencyInquiries(owner, null, null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_throws_whenNewStatusNotAllowed() {
        UUID inquiryId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(agencyId).build();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).agency(agency).status(InquiryStatus.NEW).build();
        CustomUserDetails superAdmin = CustomUserDetails.from(UserEntity.builder().id(UUID.randomUUID())
                .roles(Set.of(RoleEntity.builder().roleName(Role.SUPER_ADMIN).build())).build());

        UpdateInquiryStatusRequest request = new UpdateInquiryStatusRequest();
        request.setStatus(InquiryStatus.NEW); // not in ALLOWED_STATUSES_FOR_UPDATE

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));

        assertThatThrownBy(() -> service.updateStatus(superAdmin, inquiryId, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateStatus_throws_whenCallerCannotManageInquiry() {
        UUID inquiryId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(agencyId).build();
        InquiryEntity inquiry = InquiryEntity.builder().id(inquiryId).agency(agency).status(InquiryStatus.NEW).build();
        CustomUserDetails otherClient = clientUser(UUID.randomUUID());

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(inquiry));

        UpdateInquiryStatusRequest request = new UpdateInquiryStatusRequest();
        request.setStatus(InquiryStatus.CONTACTED);

        assertThatThrownBy(() -> service.updateStatus(otherClient, inquiryId, request))
                .isInstanceOf(ForbiddenException.class);
    }
}