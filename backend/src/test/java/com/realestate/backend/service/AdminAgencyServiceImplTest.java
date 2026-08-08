package com.realestate.backend.service;

import com.realestate.backend.dto.request.UpdateAgencyRequest;
import com.realestate.backend.dto.response.AdminAgencyResponse;
import com.realestate.backend.dto.response.AgencySubscriptionResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.AgencyStatus;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.AgencyMapper;
import com.realestate.backend.mapper.SubscriptionPlanMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.service.impl.AdminAgencyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAgencyServiceImplTest {

    @Mock private AgencyRepository agencyRepository;
    @Mock private AgencyMapper agencyMapper;
    @Mock private AgencySubscriptionRepository agencySubscriptionRepository;
    @Mock private SubscriptionPlanMapper subscriptionMapper;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock private AgencyService agencyService;

    @InjectMocks private AdminAgencyServiceImpl service;

    private AgencyEntity agency;
    private UUID agencyId;

    @BeforeEach
    void setUp() {
        agencyId = UUID.randomUUID();
        agency = AgencyEntity.builder().id(agencyId).name("Acme Realty")
                .status(AgencyStatus.PENDING).isDeleted(false).build();
    }

    @Test
    void changeAgencyStatus_updatesStatus_whenAgencyExists() {
        when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));

        String result = service.changeAgencyStatus(agencyId, AgencyStatus.APPROVED);

        assertThat(agency.getStatus()).isEqualTo(AgencyStatus.APPROVED);
        assertThat(result).contains("Acme Realty").contains("APPROVED");
        verify(agencyRepository).save(agency);
    }

    @Test
    void changeAgencyStatus_throws_whenAgencyNotFound() {
        when(agencyRepository.findById(agencyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeAgencyStatus(agencyId, AgencyStatus.APPROVED))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void softDeleteAgency_marksAgencyDeleted() {
        when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));

        service.softDeleteAgency(agencyId);

        assertThat(agency.getIsDeleted()).isTrue();
        verify(agencyRepository).save(agency);
    }

    @Test
    void createAgencySubscription_throws_whenPlanNotActive() {
        UUID planId = UUID.randomUUID();
        SubscriptionPlanEntity plan = SubscriptionPlanEntity.builder().id(planId).durationDays(30).build();

        when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionPlanRepository.existsByIdAndActiveTrue(planId)).thenReturn(false);

        assertThatThrownBy(() -> service.createAgencySubscription(agencyId, planId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void createAgencySubscription_throws_whenAgencyNotApproved() {
        UUID planId = UUID.randomUUID();
        SubscriptionPlanEntity plan = SubscriptionPlanEntity.builder().id(planId).durationDays(30).build();
        agency.setStatus(AgencyStatus.PENDING);

        when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionPlanRepository.existsByIdAndActiveTrue(planId)).thenReturn(true);

        assertThatThrownBy(() -> service.createAgencySubscription(agencyId, planId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not been approved");
    }

    @Test
    void createAgencySubscription_succeeds_whenAllChecksPass() {
        UUID planId = UUID.randomUUID();
        agency.setStatus(AgencyStatus.APPROVED);
        SubscriptionPlanEntity plan = SubscriptionPlanEntity.builder().id(planId).durationDays(30).build();
        AgencySubscriptionEntity saved = AgencySubscriptionEntity.builder()
                .agency(agency).plan(plan).startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30)).status(SubscriptionStatus.ACTIVE).build();

        when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionPlanRepository.existsByIdAndActiveTrue(planId)).thenReturn(true);
        when(agencySubscriptionRepository.existsByAgencyIdAndStatus(agencyId, SubscriptionStatus.ACTIVE)).thenReturn(false);
        when(agencySubscriptionRepository.saveAndFlush(any())).thenReturn(saved);
        when(subscriptionMapper.toAdminResponse(saved)).thenReturn(AgencySubscriptionResponse.builder().build());

        AgencySubscriptionResponse response = service.createAgencySubscription(agencyId, planId);

        assertThat(response).isNotNull();
        verify(agencySubscriptionRepository).saveAndFlush(any());
    }

    @Test
    void getAgencySubscription_throws_whenNoActiveSubscription() {
        when(agencyRepository.findById(agencyId)).thenReturn(Optional.of(agency));
        when(agencySubscriptionRepository.findFirstByAgencyIdAndStatusOrderByEndDateDesc(agencyId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAgencySubscription(agencyId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAgency_delegatesToAgencyService_andReturnsMappedResponse() {
        UpdateAgencyRequest request = new UpdateAgencyRequest();
        request.setName("Acme Realty Updated");
        request.setDescription("An even better leading agency");
        request.setPhoneNumber("+994501234567");
        request.setEmail("updated@acme-realty.com");
        request.setWebsite("https://acme-realty.com");
        request.setCity("Baku");
        request.setAddress("Nizami Street 12");

        AgencyEntity updatedAgency = AgencyEntity.builder()
                .id(agencyId)
                .name(request.getName())
                .description(request.getDescription())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .website(request.getWebsite())
                .city(request.getCity())
                .address(request.getAddress())
                .status(AgencyStatus.APPROVED)
                .isDeleted(false)
                .build();

        AdminAgencyResponse expectedResponse = AdminAgencyResponse.builder()
                .id(agencyId)
                .name(request.getName())
                .email(request.getEmail())
                .status(AgencyStatus.APPROVED)
                .isDeleted(false)
                .build();

        when(agencyService.updateAgency(agencyId, request)).thenReturn(updatedAgency);
        when(agencyMapper.toAdminResponse(updatedAgency)).thenReturn(expectedResponse);

        AdminAgencyResponse result = service.updateAgency(agencyId, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(agencyId);
        assertThat(result.getName()).isEqualTo("Acme Realty Updated");
        assertThat(result.getEmail()).isEqualTo("updated@acme-realty.com");
        verify(agencyService).updateAgency(agencyId, request);
        verify(agencyMapper).toAdminResponse(updatedAgency);
    }

    @Test
    void updateAgency_throws_whenAgencyNotFound() {
        UpdateAgencyRequest request = new UpdateAgencyRequest();
        request.setName("Acme Realty Updated");
        request.setEmail("updated@acme-realty.com");

        when(agencyService.updateAgency(any(UUID.class), any(UpdateAgencyRequest.class)))
                .thenThrow(new ResourceNotFoundException("Agency not found with id: " + agencyId));

        assertThatThrownBy(() -> service.updateAgency(agencyId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Agency not found");

        verify(agencyMapper, never()).toAdminResponse(any());
    }

    @Test
    void updateAgency_throws_whenEmailAlreadyExistsForAnotherAgency() {
        UpdateAgencyRequest request = new UpdateAgencyRequest();
        request.setName("Acme Realty Updated");
        request.setEmail("taken@acme-realty.com");

        when(agencyService.updateAgency(any(UUID.class), any(UpdateAgencyRequest.class)))
                .thenThrow(new BadRequestException("Email already exists for another agency."));

        assertThatThrownBy(() -> service.updateAgency(agencyId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already exists");

        verify(agencyMapper, never()).toAdminResponse(any());
    }
}