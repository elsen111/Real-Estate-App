package com.realestate.backend.service;

import com.realestate.backend.dto.request.CreateInquiryRequest;
import com.realestate.backend.dto.request.InquiryFilterRequest;
import com.realestate.backend.dto.request.UpdateInquiryStatusRequest;
import com.realestate.backend.dto.response.InquiryResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.DuplicateInquiryException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.InquiryMapper;
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.AgencyRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InquiryServiceImplTest {

    @Mock private InquiryRepository inquiryRepository;
    @Mock private UserRepository userRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private AgencyMemberRepository agencyMemberRepository;
    @Mock private AgencyRepository agencyRepository;
    @Mock private InquiryMapper inquiryMapper;

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

    // ----- New method: getAgencyInquiriesById -----

    @Test
    void getAgencyInquiriesById_throws_whenAgencyDoesNotExist() {
        UUID agencyId = UUID.randomUUID();
        InquiryFilterRequest filter = new InquiryFilterRequest(
                null, null, null, null, null, null, null, null, null, null, null);
        Pageable pageable = Pageable.ofSize(10);

        when(agencyRepository.existsById(agencyId)).thenReturn(false);

        assertThatThrownBy(() -> service.getAgencyInquiriesById(agencyId, filter, pageable))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(inquiryRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAgencyInquiriesById_returnsMappedPage_whenAgencyExists() {
        UUID agencyId = UUID.randomUUID();
        InquiryFilterRequest filter = new InquiryFilterRequest(
                InquiryStatus.NEW, null, null, null, null, null, null, null, null, null, null);
        Pageable pageable = Pageable.ofSize(10);

        AgencyEntity agency = AgencyEntity.builder().id(agencyId).build();
        InquiryEntity inquiryEntity = InquiryEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .status(InquiryStatus.NEW)
                .message("Interested in this property")
                .build();
        Page<InquiryEntity> entityPage = new PageImpl<>(List.of(inquiryEntity));

        InquiryResponse mappedResponse = InquiryResponse.builder()
                .id(inquiryEntity.getId())
                .status(InquiryStatus.NEW)
                .message(inquiryEntity.getMessage())
                .agencyId(agencyId)
                .build();

        when(agencyRepository.existsById(agencyId)).thenReturn(true);
        when(inquiryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(entityPage);
        when(inquiryMapper.toResponse(inquiryEntity)).thenReturn(mappedResponse);

        Page<InquiryResponse> result = service.getAgencyInquiriesById(agencyId, filter, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mappedResponse);
        assertThat(result.getContent().get(0).getAgencyId()).isEqualTo(agencyId);

        verify(agencyRepository).existsById(agencyId);
        verify(inquiryRepository).findAll(any(Specification.class), eq(pageable));
        verify(inquiryMapper).toResponse(inquiryEntity);
    }

    @Test
    void getAgencyInquiriesById_returnsEmptyPage_whenAgencyHasNoInquiries() {
        UUID agencyId = UUID.randomUUID();
        InquiryFilterRequest filter = new InquiryFilterRequest(
                null, null, null, null, null, null, null, null, null, null, null);
        Pageable pageable = Pageable.ofSize(10);

        when(agencyRepository.existsById(agencyId)).thenReturn(true);
        when(inquiryRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<InquiryResponse> result = service.getAgencyInquiriesById(agencyId, filter, pageable);

        assertThat(result.getContent()).isEmpty();

        verify(inquiryMapper, never()).toResponse(any());
    }
}