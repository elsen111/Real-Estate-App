package com.realestate.backend.service;

import com.realestate.backend.dto.request.CreateInquiryRequest;
import com.realestate.backend.dto.request.InquiryFilterRequest;
import com.realestate.backend.dto.request.UpdateInquiryStatusRequest;
import com.realestate.backend.dto.response.InquiryResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.*;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

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

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private AgencyMemberRepository agencyMemberRepository;

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private InquiryMapper inquiryMapper;

    @InjectMocks
    private InquiryServiceImpl service;

    private CustomUserDetails clientUser(UUID id) {
        return CustomUserDetails.from(
                UserEntity.builder()
                        .id(id)
                        .roles(Set.of(
                                RoleEntity.builder()
                                        .roleName(Role.CLIENT)
                                        .build()
                        ))
                        .build()
        );
    }

    private CustomUserDetails userWithRole(UUID id, Role role) {
        return CustomUserDetails.from(
                UserEntity.builder()
                        .id(id)
                        .roles(Set.of(
                                RoleEntity.builder()
                                        .roleName(role)
                                        .build()
                        ))
                        .build()
        );
    }

    @Test
    void createInquiry_throws_whenPropertyNotActive() {
        UUID propertyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .status(PropertyStatus.REJECTED)
                .build();

        UserEntity client = UserEntity.builder()
                .id(userId)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(client));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        assertThatThrownBy(() ->
                service.createInquiry(
                        propertyId,
                        new CreateInquiryRequest(),
                        clientUser(userId)
                )
        )
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void createInquiry_throws_whenOpenInquiryAlreadyExists() {
        UUID propertyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .status(PropertyStatus.ACTIVE)
                .build();

        UserEntity client = UserEntity.builder()
                .id(userId)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(client));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(inquiryRepository.existsByPropertyIdAndClientIdAndStatusNot(
                propertyId,
                userId,
                InquiryStatus.CLOSED
        )).thenReturn(true);

        assertThatThrownBy(() ->
                service.createInquiry(
                        propertyId,
                        new CreateInquiryRequest(),
                        clientUser(userId)
                )
        )
                .isInstanceOf(DuplicateInquiryException.class);
    }

    @Test
    void getMyAgencyInquiries_throws_whenNotActiveAgencyMember() {
        UUID userId = UUID.randomUUID();

        CustomUserDetails client = clientUser(userId);

        when(agencyMemberRepository.findByUser_IdAndActiveTrue(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getMyAgencyInquiries(
                        client,
                        null,
                        null,
                        Pageable.ofSize(10)
                )
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("No active agency membership");
    }

    @Test
    void updateStatus_throws_whenNewStatusNotAllowed() {
        UUID inquiryId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        InquiryEntity inquiry = InquiryEntity.builder()
                .id(inquiryId)
                .agency(agency)
                .status(InquiryStatus.NEW)
                .build();

        CustomUserDetails superAdmin = userWithRole(
                UUID.randomUUID(),
                Role.SUPER_ADMIN
        );

        UpdateInquiryStatusRequest request =
                new UpdateInquiryStatusRequest();

        request.setStatus(InquiryStatus.NEW);

        when(inquiryRepository.findById(inquiryId))
                .thenReturn(Optional.of(inquiry));

        assertThatThrownBy(() ->
                service.updateStatus(
                        superAdmin,
                        inquiryId,
                        request
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Allowed statuses");

        verify(inquiryRepository, never())
                .saveAndFlush(any(InquiryEntity.class));
    }

    @Test
    void updateStatus_throws_whenCallerCannotManageInquiry() {
        UUID inquiryId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        InquiryEntity inquiry = InquiryEntity.builder()
                .id(inquiryId)
                .agency(agency)
                .status(InquiryStatus.NEW)
                .build();

        CustomUserDetails otherClient =
                clientUser(UUID.randomUUID());

        when(inquiryRepository.findById(inquiryId))
                .thenReturn(Optional.of(inquiry));

        UpdateInquiryStatusRequest request =
                new UpdateInquiryStatusRequest();

        request.setStatus(InquiryStatus.CONTACTED);

        assertThatThrownBy(() ->
                service.updateStatus(
                        otherClient,
                        inquiryId,
                        request
                )
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(inquiryRepository, never())
                .saveAndFlush(any(InquiryEntity.class));
    }

    @Test
    void updateStatus_throwsBusinessException_whenTransitionIsNotAllowed() {
        UUID inquiryId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        InquiryEntity inquiry = InquiryEntity.builder()
                .id(inquiryId)
                .agency(agency)
                .status(InquiryStatus.CLOSED)
                .build();

        CustomUserDetails superAdmin = userWithRole(
                UUID.randomUUID(),
                Role.SUPER_ADMIN
        );

        UpdateInquiryStatusRequest request =
                new UpdateInquiryStatusRequest();

        request.setStatus(InquiryStatus.CONTACTED);

        when(inquiryRepository.findById(inquiryId))
                .thenReturn(Optional.of(inquiry));

        assertThatThrownBy(() ->
                service.updateStatus(
                        superAdmin,
                        inquiryId,
                        request
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CLOSED")
                .hasMessageContaining("CONTACTED");

        verify(inquiryRepository, never())
                .saveAndFlush(any(InquiryEntity.class));
    }

    @Test
    void updateStatus_updatesInquiry_whenTransitionIsAllowed() {
        UUID inquiryId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .build();

        UserEntity client = UserEntity.builder()
                .id(clientId)
                .build();

        InquiryEntity inquiry = InquiryEntity.builder()
                .id(inquiryId)
                .agency(agency)
                .property(property)
                .client(client)
                .status(InquiryStatus.NEW)
                .build();

        CustomUserDetails superAdmin = userWithRole(
                UUID.randomUUID(),
                Role.SUPER_ADMIN
        );

        UpdateInquiryStatusRequest request =
                new UpdateInquiryStatusRequest();

        request.setStatus(InquiryStatus.CONTACTED);

        InquiryResponse expected =
                InquiryResponse.builder()
                        .id(inquiryId)
                        .status(InquiryStatus.CONTACTED)
                        .build();

        when(inquiryRepository.findById(inquiryId))
                .thenReturn(Optional.of(inquiry));

        when(inquiryMapper.toResponse(inquiry))
                .thenReturn(expected);

        InquiryResponse result = service.updateStatus(
                superAdmin,
                inquiryId,
                request
        );

        assertThat(result)
                .isEqualTo(expected);

        assertThat(inquiry.getStatus())
                .isEqualTo(InquiryStatus.CONTACTED);

        verify(inquiryMapper)
                .toResponse(inquiry);

        verify(inquiryRepository, never())
                .saveAndFlush(any(InquiryEntity.class));
    }

    @Test
    void updateStatus_updatesInquiry_whenTransitionFromContactedToClosedIsAllowed() {
        UUID inquiryId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .build();

        UserEntity client = UserEntity.builder()
                .id(clientId)
                .build();

        InquiryEntity inquiry = InquiryEntity.builder()
                .id(inquiryId)
                .agency(agency)
                .property(property)
                .client(client)
                .status(InquiryStatus.CONTACTED)
                .build();

        CustomUserDetails superAdmin = userWithRole(
                UUID.randomUUID(),
                Role.SUPER_ADMIN
        );

        UpdateInquiryStatusRequest request =
                new UpdateInquiryStatusRequest();

        request.setStatus(InquiryStatus.CLOSED);

        InquiryResponse expected =
                InquiryResponse.builder()
                        .id(inquiryId)
                        .status(InquiryStatus.CLOSED)
                        .build();

        when(inquiryRepository.findById(inquiryId))
                .thenReturn(Optional.of(inquiry));

        when(inquiryMapper.toResponse(inquiry))
                .thenReturn(expected);

        InquiryResponse result = service.updateStatus(
                superAdmin,
                inquiryId,
                request
        );

        assertThat(result)
                .isEqualTo(expected);

        assertThat(inquiry.getStatus())
                .isEqualTo(InquiryStatus.CLOSED);

        verify(inquiryMapper)
                .toResponse(inquiry);

        verify(inquiryRepository, never())
                .saveAndFlush(any(InquiryEntity.class));
    }

    @Test
    void getAgencyInquiriesById_throws_whenAgencyDoesNotExist() {
        UUID agencyId = UUID.randomUUID();

        InquiryFilterRequest filter =
                new InquiryFilterRequest(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        Pageable pageable = Pageable.ofSize(10);

        when(agencyRepository.existsById(agencyId))
                .thenReturn(false);

        assertThatThrownBy(() ->
                service.getAgencyInquiriesById(
                        agencyId,
                        filter,
                        pageable
                )
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(inquiryRepository, never())
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }

    @Test
    void getAgencyInquiriesById_returnsMappedPage_whenAgencyExists() {
        UUID agencyId = UUID.randomUUID();

        InquiryFilterRequest filter =
                new InquiryFilterRequest(
                        InquiryStatus.NEW,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        Pageable pageable = Pageable.ofSize(10);

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        InquiryEntity inquiryEntity =
                InquiryEntity.builder()
                        .id(UUID.randomUUID())
                        .agency(agency)
                        .status(InquiryStatus.NEW)
                        .message("Interested in this property")
                        .build();

        Page<InquiryEntity> entityPage =
                new PageImpl<>(
                        List.of(inquiryEntity),
                        pageable,
                        1
                );

        InquiryResponse mappedResponse =
                InquiryResponse.builder()
                        .id(inquiryEntity.getId())
                        .status(InquiryStatus.NEW)
                        .message(inquiryEntity.getMessage())
                        .agencyId(agencyId)
                        .build();

        when(agencyRepository.existsById(agencyId))
                .thenReturn(true);

        when(inquiryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(entityPage);

        when(inquiryMapper.toResponse(inquiryEntity))
                .thenReturn(mappedResponse);

        Page<InquiryResponse> result =
                service.getAgencyInquiriesById(
                        agencyId,
                        filter,
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().get(0))
                .isEqualTo(mappedResponse);

        assertThat(result.getContent().get(0).getAgencyId())
                .isEqualTo(agencyId);

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getPageable())
                .isEqualTo(pageable);

        verify(agencyRepository)
                .existsById(agencyId);

        verify(inquiryRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );

        verify(inquiryMapper)
                .toResponse(inquiryEntity);
    }

    @Test
    void getAgencyInquiriesById_returnsEmptyPage_whenAgencyHasNoInquiries() {
        UUID agencyId = UUID.randomUUID();

        InquiryFilterRequest filter =
                new InquiryFilterRequest(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        Pageable pageable = Pageable.ofSize(10);

        when(agencyRepository.existsById(agencyId))
                .thenReturn(true);

        when(inquiryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                )
        );

        Page<InquiryResponse> result =
                service.getAgencyInquiriesById(
                        agencyId,
                        filter,
                        pageable
                );

        assertThat(result.getContent())
                .isEmpty();

        assertThat(result.getTotalElements())
                .isZero();

        verify(inquiryMapper, never())
                .toResponse(any());
    }
}