package com.realestate.backend.service;

import com.realestate.backend.dto.request.AgencyAgentFilterRequest;
import com.realestate.backend.dto.request.AgencyFilterRequest;
import com.realestate.backend.dto.request.AgencyPropertyFilterRequest;
import com.realestate.backend.dto.request.PropertyFilterRequest;
import com.realestate.backend.dto.request.UpdateAgencyRequest;
import com.realestate.backend.dto.response.AgencyLogoUploadResponse;
import com.realestate.backend.dto.response.AgencyMemberResponse;
import com.realestate.backend.dto.response.AgencyResponse;
import com.realestate.backend.dto.response.AgencySubscriptionResponse;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.AgencyMapper;
import com.realestate.backend.mapper.AgencyMemberMapper;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.mapper.UserMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.AgencyServiceImpl;
import com.realestate.backend.storage.MediaUploadPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgencyServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private AgencyMapper agencyMapper;

    @Mock
    private AgencySubscriptionRepository agencySubscriptionRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private PropertyMapper propertyMapper;

    @Mock
    private AgencyMediaRepository agencyMediaRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private AgencyMemberMapper agencyMemberMapper;

    @Mock
    private AgencyMemberRepository agencyMemberRepository;

    @InjectMocks
    private AgencyServiceImpl service;

    private CustomUserDetails currentUser(UUID userId) {
        return CustomUserDetails.from(
                UserEntity.builder()
                        .id(userId)
                        .roles(Set.of())
                        .build()
        );
    }

    @Test
    void getCurrentAgency_throws_whenUserHasNoAgency() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(null)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                service.getCurrentAgency(currentUser(userId))
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMySubscription_throws_whenNoActiveSubscription() {
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(agency)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(agencySubscriptionRepository.findByAgencyAndStatus(
                agency,
                SubscriptionStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getMySubscription(currentUser(userId))
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMySubscription_calculatesRemainingListingsAndAgents() {
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(agency)
                .build();

        SubscriptionPlanEntity plan = SubscriptionPlanEntity.builder()
                .id(UUID.randomUUID())
                .name("Gold")
                .price(BigDecimal.TEN)
                .durationDays(30)
                .maxListings(10)
                .maxAgents(5)
                .build();

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .agency(agency)
                        .plan(plan)
                        .status(SubscriptionStatus.ACTIVE)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(agencySubscriptionRepository.findByAgencyAndStatus(
                agency,
                SubscriptionStatus.ACTIVE
        )).thenReturn(Optional.of(subscription));

        when(propertyRepository.countByAgencyIdAndStatusIn(
                agency.getId(),
                List.of(
                        PropertyStatus.PENDING,
                        PropertyStatus.ACTIVE
                )
        )).thenReturn(3L);

        when(userRepository.countByAgency(agency))
                .thenReturn(2L);

        AgencySubscriptionResponse response =
                service.getMySubscription(currentUser(userId));

        assertThat(response.getRemainingListings())
                .isEqualTo(7);

        assertThat(response.getRemainingAgents())
                .isEqualTo(3);
    }

    @Test
    void getPublicAgencyInfo_throws_whenAgencyNotFound() {
        UUID agencyId = UUID.randomUUID();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getPublicAgencyInfo(agencyId)
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateOwnAgency_updatesAgencyAndReturnsResponse() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Old Agency")
                .email("old@example.com")
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(agency)
                .build();

        UpdateAgencyRequest request = mock(UpdateAgencyRequest.class);

        when(request.getName()).thenReturn("New Agency");
        when(request.getDescription()).thenReturn("New description");
        when(request.getPhoneNumber()).thenReturn("+994501234567");
        when(request.getEmail()).thenReturn("new@example.com");
        when(request.getWebsite()).thenReturn("https://newagency.com");
        when(request.getCity()).thenReturn("Baku");
        when(request.getAddress()).thenReturn("Nizami Street");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(agencyRepository.existsByEmail("new@example.com"))
                .thenReturn(false);

        AgencyResponse response = mock(AgencyResponse.class);

        when(agencyMapper.toAgencyOwnerResponse(agency))
                .thenReturn(response);

        AgencyResponse result =
                service.updateOwnAgency(
                        currentUser(userId),
                        request
                );

        assertThat(result).isSameAs(response);

        assertThat(agency.getName())
                .isEqualTo("New Agency");

        assertThat(agency.getDescription())
                .isEqualTo("New description");

        assertThat(agency.getPhoneNumber())
                .isEqualTo("+994501234567");

        assertThat(agency.getEmail())
                .isEqualTo("new@example.com");

        assertThat(agency.getWebsite())
                .isEqualTo("https://newagency.com");

        assertThat(agency.getCity())
                .isEqualTo("Baku");

        assertThat(agency.getAddress())
                .isEqualTo("Nizami Street");

        verify(agencyMapper)
                .toAgencyOwnerResponse(agency);
    }

    @Test
    void updateOwnAgency_throws_whenUserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.updateOwnAgency(
                        currentUser(userId),
                        mock(UpdateAgencyRequest.class)
                )
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMyAgencyProperties_returnsMappedProperties() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(agency)
                .build();

        AgencyPropertyFilterRequest filter =
                mock(AgencyPropertyFilterRequest.class);

        Pageable pageable = PageRequest.of(0, 10);

        PropertyEntity property = PropertyEntity.builder()
                .id(UUID.randomUUID())
                .agency(agency)
                .build();

        PropertyResponse propertyResponse =
                mock(PropertyResponse.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(propertyRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of(property))
        );

        when(propertyMapper.toAdminPropertyResponse(property))
                .thenReturn(propertyResponse);

        Page<PropertyResponse> result =
                service.getMyAgencyProperties(
                        currentUser(userId),
                        filter,
                        pageable
                );

        assertThat(result.getContent())
                .containsExactly(propertyResponse);

        verify(propertyRepository)
                .findAll(any(Specification.class), eq(pageable));

        verify(propertyMapper)
                .toAdminPropertyResponse(property);
    }

    @Test
    void getAllPublicAgencies_returnsMappedAgencies() {
        AgencyFilterRequest filter =
                mock(AgencyFilterRequest.class);

        Pageable pageable = PageRequest.of(0, 10);

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .name("Prime Realty")
                .build();

        AgencyResponse response =
                mock(AgencyResponse.class);

        when(agencyRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of(agency))
        );

        when(agencyMapper.toPublicAgencyListItem(agency))
                .thenReturn(response);

        Page<AgencyResponse> result =
                service.getAllPublicAgencies(
                        filter,
                        pageable
                );

        assertThat(result.getContent())
                .containsExactly(response);

        verify(agencyRepository)
                .findAll(any(Specification.class), eq(pageable));

        verify(agencyMapper)
                .toPublicAgencyListItem(agency);
    }

    @Test
    void getAgencyProperties_returnsMappedProperties() {
        UUID agencyId = UUID.randomUUID();

        PropertyFilterRequest filter =
                mock(PropertyFilterRequest.class);

        Pageable pageable = PageRequest.of(0, 10);

        PropertyEntity property = PropertyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        PropertyResponse response =
                mock(PropertyResponse.class);

        when(propertyRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of(property))
        );

        when(propertyMapper.toPublicAgencyPropertyResponse(property))
                .thenReturn(response);

        Page<PropertyResponse> result =
                service.getAgencyProperties(
                        agencyId,
                        filter,
                        pageable
                );

        assertThat(result.getContent())
                .containsExactly(response);

        verify(propertyRepository)
                .findAll(any(Specification.class), eq(pageable));

        verify(propertyMapper)
                .toPublicAgencyPropertyResponse(property);
    }

    @Test
    void getAgencyAgents_returnsMappedAgents() {
        UUID agencyId = UUID.randomUUID();

        AgencyAgentFilterRequest filter =
                mock(AgencyAgentFilterRequest.class);

        Pageable pageable = PageRequest.of(0, 10);

        AgencyMemberEntity member =
                AgencyMemberEntity.builder()
                        .id(UUID.randomUUID())
                        .role(Role.AGENT)
                        .active(true)
                        .build();

        AgencyMemberResponse response =
                mock(AgencyMemberResponse.class);

        when(agencyMemberRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(
                new PageImpl<>(List.of(member))
        );

        when(agencyMemberMapper.toAgentResponse(member))
                .thenReturn(response);

        Page<AgencyMemberResponse> result =
                service.getAgencyAgents(
                        agencyId,
                        filter,
                        pageable
                );

        assertThat(result.getContent())
                .containsExactly(response);

        verify(agencyMemberRepository)
                .findAll(any(Specification.class), eq(pageable));

        verify(agencyMemberMapper)
                .toAgentResponse(member);
    }

    @Test
    void uploadLogo_uploadsAndSavesNewLogo_whenNoExistingLogo() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Prime Realty")
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(agency)
                .build();

        MultipartFile file = new MockMultipartFile(
                "file",
                "logo.png",
                "image/png",
                "logo-content".getBytes()
        );

        MediaFileEntity uploadedMedia =
                MediaFileEntity.builder()
                        .id(UUID.randomUUID())
                        .fileUrl("https://storage.example.com/logo.png")
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(agencyMediaRepository.findByAgencyId(agencyId))
                .thenReturn(Optional.empty());

        when(mediaService.upload(
                file,
                MediaUploadPolicy.AGENCY_LOGO
        )).thenReturn(uploadedMedia);

        AgencyLogoUploadResponse response =
                service.uploadLogo(
                        file,
                        currentUser(userId)
                );

        assertThat(response).isNotNull();
        assertThat(response.logoUrl())
                .isEqualTo("https://storage.example.com/logo.png");

        verify(mediaService)
                .upload(file, MediaUploadPolicy.AGENCY_LOGO);

        verify(agencyMediaRepository)
                .save(any(AgencyMediaEntity.class));

        verify(agencyMediaRepository, never())
                .delete(any());

        verify(mediaService, never())
                .delete(any());
    }

    @Test
    void uploadLogo_replacesExistingLogo() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Prime Realty")
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(agency)
                .build();

        MediaFileEntity oldMedia =
                MediaFileEntity.builder()
                        .id(UUID.randomUUID())
                        .fileUrl("https://storage.example.com/old-logo.png")
                        .build();

        AgencyMediaEntity existingLogo =
                AgencyMediaEntity.builder()
                        .id(UUID.randomUUID())
                        .agency(agency)
                        .media(oldMedia)
                        .build();

        MultipartFile file = new MockMultipartFile(
                "file",
                "new-logo.png",
                "image/png",
                "new-logo-content".getBytes()
        );

        MediaFileEntity uploadedMedia =
                MediaFileEntity.builder()
                        .id(UUID.randomUUID())
                        .fileUrl("https://storage.example.com/new-logo.png")
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(agencyMediaRepository.findByAgencyId(agencyId))
                .thenReturn(Optional.of(existingLogo));

        when(mediaService.upload(
                file,
                MediaUploadPolicy.AGENCY_LOGO
        )).thenReturn(uploadedMedia);

        AgencyLogoUploadResponse response =
                service.uploadLogo(
                        file,
                        currentUser(userId)
                );

        assertThat(response.logoUrl())
                .isEqualTo("https://storage.example.com/new-logo.png");

        verify(agencyMediaRepository)
                .delete(existingLogo);

        verify(mediaService)
                .delete(oldMedia);

        verify(agencyMediaRepository)
                .save(any(AgencyMediaEntity.class));

        verify(mediaService)
                .upload(file, MediaUploadPolicy.AGENCY_LOGO);
    }

    @Test
    void removeAgencyLogo_removesExistingLogo() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(agency)
                .build();

        MediaFileEntity media =
                MediaFileEntity.builder()
                        .id(UUID.randomUUID())
                        .fileUrl("https://storage.example.com/logo.png")
                        .build();

        AgencyMediaEntity agencyMedia =
                AgencyMediaEntity.builder()
                        .id(UUID.randomUUID())
                        .agency(agency)
                        .media(media)
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(agencyMediaRepository.findByAgencyId(agencyId))
                .thenReturn(Optional.of(agencyMedia));

        service.removeAgencyLogo(currentUser(userId));

        verify(agencyMediaRepository)
                .delete(agencyMedia);

        verify(mediaService)
                .delete(media);
    }

    @Test
    void removeAgencyLogo_throws_whenLogoNotFound() {
        UUID userId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(agency)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(agencyMediaRepository.findByAgencyId(agencyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.removeAgencyLogo(currentUser(userId))
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(agencyMediaRepository, never())
                .delete(any());

        verify(mediaService, never())
                .delete(any());
    }

    @Test
    void updateAgency_updatesAllFields() {
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .name("Old Agency")
                .description("Old description")
                .phoneNumber("+994000000000")
                .email("old@example.com")
                .website("https://old.com")
                .city("Baku")
                .address("Old address")
                .build();

        UpdateAgencyRequest request =
                mock(UpdateAgencyRequest.class);

        when(request.getName()).thenReturn("New Agency");
        when(request.getDescription()).thenReturn("New description");
        when(request.getPhoneNumber()).thenReturn("+994501234567");
        when(request.getEmail()).thenReturn("new@example.com");
        when(request.getWebsite()).thenReturn("https://new.com");
        when(request.getCity()).thenReturn("Ganja");
        when(request.getAddress()).thenReturn("New address");

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(agencyRepository.existsByEmail("new@example.com"))
                .thenReturn(false);

        AgencyEntity result =
                service.updateAgency(
                        agencyId,
                        request
                );

        assertThat(result).isSameAs(agency);

        assertThat(agency.getName())
                .isEqualTo("New Agency");

        assertThat(agency.getDescription())
                .isEqualTo("New description");

        assertThat(agency.getPhoneNumber())
                .isEqualTo("+994501234567");

        assertThat(agency.getEmail())
                .isEqualTo("new@example.com");

        assertThat(agency.getWebsite())
                .isEqualTo("https://new.com");

        assertThat(agency.getCity())
                .isEqualTo("Ganja");

        assertThat(agency.getAddress())
                .isEqualTo("New address");
    }

    @Test
    void updateAgency_throws_whenAgencyNotFound() {
        UUID agencyId = UUID.randomUUID();

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.updateAgency(
                        agencyId,
                        mock(UpdateAgencyRequest.class)
                )
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAgency_throwsConflict_whenEmailBelongsToAnotherAgency() {
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .email("old@example.com")
                .build();

        UpdateAgencyRequest request =
                mock(UpdateAgencyRequest.class);

        when(request.getEmail())
                .thenReturn("existing@example.com");

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(agencyRepository.existsByEmail("existing@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.updateAgency(
                        agencyId,
                        request
                )
        )
                .isInstanceOf(ConflictException.class);

        assertThat(agency.getEmail())
                .isEqualTo("old@example.com");
    }

    @Test
    void updateAgency_doesNotThrowConflict_whenEmailBelongsToSameAgency() {
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(agencyId)
                .email("same@example.com")
                .build();

        UpdateAgencyRequest request =
                mock(UpdateAgencyRequest.class);

        when(request.getName()).thenReturn("Updated Agency");
        when(request.getDescription()).thenReturn("Updated description");
        when(request.getPhoneNumber()).thenReturn("+994501234567");
        when(request.getEmail()).thenReturn("same@example.com");
        when(request.getWebsite()).thenReturn("https://same.com");
        when(request.getCity()).thenReturn("Baku");
        when(request.getAddress()).thenReturn("Updated address");

        when(agencyRepository.findById(agencyId))
                .thenReturn(Optional.of(agency));

        when(agencyRepository.existsByEmail("same@example.com"))
                .thenReturn(true);

        AgencyEntity result =
                service.updateAgency(
                        agencyId,
                        request
                );

        assertThat(result).isSameAs(agency);
        assertThat(result.getName())
                .isEqualTo("Updated Agency");

        assertThat(result.getEmail())
                .isEqualTo("same@example.com");
    }
}