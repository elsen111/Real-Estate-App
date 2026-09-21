package com.realestate.backend.service;

import com.realestate.backend.dto.request.AssignAgentToPropertyRequest;
import com.realestate.backend.dto.request.PropertyRequest;
import com.realestate.backend.dto.request.PropertyStatusRequest;
import com.realestate.backend.dto.response.PropertyResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.PropertyMapper;
import com.realestate.backend.repository.*;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AgencySubscriptionRepository agencySubscriptionRepository;

    @Mock
    private PropertyViewService propertyViewService;

    @Mock
    private PropertyMapper propertyMapper;

    @Mock
    private PropertyViewRepository propertyViewRepository;

    @InjectMocks
    private PropertyServiceImpl service;

    private CustomUserDetails agencyUser(UUID id) {
        return CustomUserDetails.from(
                UserEntity.builder()
                        .id(id)
                        .roles(Set.of(
                                RoleEntity.builder()
                                        .roleName(Role.AGENCY_OWNER)
                                        .build()
                        ))
                        .build()
        );
    }

    @Test
    void createProperty_throws_whenUserHasNoAgency() {
        UUID userId = UUID.randomUUID();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(null)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                service.createProperty(
                        new PropertyRequest(),
                        agencyUser(userId)
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Agency not found associated with your profile.");    }

    @Test
    void createProperty_throws_whenNoActiveSubscription() {
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

        when(agencySubscriptionRepository
                .findFirstByAgency_IdAndStatusOrderByEndDateDesc(
                        agency.getId(),
                        SubscriptionStatus.ACTIVE
                ))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createProperty(
                        new PropertyRequest(),
                        agencyUser(userId)
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("doesn't have an active subscription");
    }

    @Test
    void createProperty_throws_whenSubscriptionExpired() {
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(agency)
                .build();

        SubscriptionPlanEntity plan = SubscriptionPlanEntity.builder()
                .maxListings(10)
                .build();

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .agency(agency)
                        .plan(plan)
                        .endDate(LocalDate.now().minusDays(1))
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(agencySubscriptionRepository
                .findFirstByAgency_IdAndStatusOrderByEndDateDesc(
                        agency.getId(),
                        SubscriptionStatus.ACTIVE
                ))
                .thenReturn(Optional.of(subscription));

        assertThatThrownBy(() ->
                service.createProperty(
                        new PropertyRequest(),
                        agencyUser(userId)
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void createProperty_throws_whenListingLimitReached() {
        UUID userId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(agency)
                .build();

        SubscriptionPlanEntity plan = SubscriptionPlanEntity.builder()
                .maxListings(5)
                .build();

        AgencySubscriptionEntity subscription =
                AgencySubscriptionEntity.builder()
                        .agency(agency)
                        .plan(plan)
                        .endDate(LocalDate.now().plusDays(10))
                        .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(agencySubscriptionRepository
                .findFirstByAgency_IdAndStatusOrderByEndDateDesc(
                        agency.getId(),
                        SubscriptionStatus.ACTIVE
                ))
                .thenReturn(Optional.of(subscription));

        when(propertyRepository.countByAgencyId(agency.getId()))
                .thenReturn(5L);

        assertThatThrownBy(() ->
                service.createProperty(
                        new PropertyRequest(),
                        agencyUser(userId)
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Listing limit reached");
    }

    @Test
    void updateProperty_throws_whenPropertyBelongsToAnotherAgency() {
        UUID userId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        AgencyEntity myAgency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        AgencyEntity otherAgency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(myAgency)
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .agency(otherAgency)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        assertThatThrownBy(() ->
                service.updateProperty(
                        propertyId,
                        new PropertyRequest(),
                        agencyUser(userId)
                )
        )
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("another agency");
    }

    @Test
    void updateStatus_throws_whenStatusNotAllowedForAgencies() {
        UUID userId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity user = UserEntity.builder()
                .id(userId)
                .agency(agency)
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .agency(agency)
                .build();

        PropertyStatusRequest request = new PropertyStatusRequest();
        request.setStatus(PropertyStatus.ACTIVE);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        assertThatThrownBy(() ->
                service.updateStatus(
                        propertyId,
                        request,
                        agencyUser(userId)
                )
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SOLD, RENTED");
    }

    @Test
    void softDeleteProperty_throws_whenPropertyNotFound() {
        UUID propertyId = UUID.randomUUID();

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.softDeleteProperty(
                        propertyId,
                        agencyUser(UUID.randomUUID())
                )
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPropertyDetailsById_throws_whenPropertyHiddenFromPublic() {
        UUID propertyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .agency(agency)
                .status(PropertyStatus.REJECTED)
                .build();

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        assertThatThrownBy(() ->
                service.getPropertyDetailsById(propertyId, null)
        )
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private UserEntity buildAgentUser(
            UUID id,
            AgencyEntity agency,
            boolean enabled,
            boolean deleted
    ) {
        return UserEntity.builder()
                .id(id)
                .fullName("Jane Agent")
                .agency(agency)
                .enabled(enabled)
                .deleted(deleted)
                .roles(Set.of(
                        RoleEntity.builder()
                                .roleName(Role.AGENT)
                                .build()
                ))
                .build();
    }

    @Test
    void assignAgentToProperty_succeeds_whenAllValidationsPass() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .agency(agency)
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .agency(agency)
                .status(PropertyStatus.ACTIVE)
                .build();

        UserEntity agent = buildAgentUser(
                agentId,
                agency,
                true,
                false
        );

        AssignAgentToPropertyRequest request =
                new AssignAgentToPropertyRequest();

        request.setAgentId(agentId);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(userRepository.findById(agentId))
                .thenReturn(Optional.of(agent));

        service.assignAgentToProperty(
                propertyId,
                request,
                agencyUser(ownerId)
        );

        assertThat(property.getAssignedAgent())
                .isEqualTo(agent);
    }

    @Test
    void assignAgentToProperty_throws_whenOwnerNotFound() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        AssignAgentToPropertyRequest request =
                new AssignAgentToPropertyRequest();

        request.setAgentId(UUID.randomUUID());

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.assignAgentToProperty(
                        propertyId,
                        request,
                        agencyUser(ownerId)
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Owner not found with id: " + ownerId);
    }

    @Test
    void assignAgentToProperty_throws_whenOwnerHasNoAgency() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .agency(null)
                .build();

        AssignAgentToPropertyRequest request =
                new AssignAgentToPropertyRequest();

        request.setAgentId(UUID.randomUUID());

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        assertThatThrownBy(() ->
                service.assignAgentToProperty(
                        propertyId,
                        request,
                        agencyUser(ownerId)
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Agency not found associated with the user: " + ownerId);
    }

    @Test
    void assignAgentToProperty_throws_whenPropertyNotFound() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .agency(agency)
                .build();

        AssignAgentToPropertyRequest request =
                new AssignAgentToPropertyRequest();

        request.setAgentId(UUID.randomUUID());

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.assignAgentToProperty(
                        propertyId,
                        request,
                        agencyUser(ownerId)
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Property not found");
    }

    @Test
    void assignAgentToProperty_throws_whenPropertyBelongsToAnotherAgency() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        AgencyEntity myAgency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        AgencyEntity otherAgency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .agency(myAgency)
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .agency(otherAgency)
                .status(PropertyStatus.ACTIVE)
                .build();

        AssignAgentToPropertyRequest request =
                new AssignAgentToPropertyRequest();

        request.setAgentId(UUID.randomUUID());

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        assertThatThrownBy(() ->
                service.assignAgentToProperty(
                        propertyId,
                        request,
                        agencyUser(ownerId)
                )
        )
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("another agency");
    }

    @Test
    void assignAgentToProperty_throws_whenPropertyIsNotActive() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .agency(agency)
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .agency(agency)
                .status(PropertyStatus.PENDING)
                .build();

        AssignAgentToPropertyRequest request =
                new AssignAgentToPropertyRequest();

        request.setAgentId(UUID.randomUUID());

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        assertThatThrownBy(() ->
                service.assignAgentToProperty(
                        propertyId,
                        request,
                        agencyUser(ownerId)
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(
                        "only be assigned to active properties"
                );
    }

    @Test
    void assignAgentToProperty_throws_whenAgentNotFound() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .agency(agency)
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .agency(agency)
                .status(PropertyStatus.ACTIVE)
                .build();

        AssignAgentToPropertyRequest request =
                new AssignAgentToPropertyRequest();

        request.setAgentId(agentId);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(userRepository.findById(agentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.assignAgentToProperty(
                        propertyId,
                        request,
                        agencyUser(ownerId)
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Agent not found");
    }

    @Test
    void assignAgentToProperty_throws_whenAgentIsDisabled() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .agency(agency)
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .agency(agency)
                .status(PropertyStatus.ACTIVE)
                .build();

        UserEntity disabledAgent = buildAgentUser(
                agentId,
                agency,
                false,
                false
        );

        AssignAgentToPropertyRequest request =
                new AssignAgentToPropertyRequest();

        request.setAgentId(agentId);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(userRepository.findById(agentId))
                .thenReturn(Optional.of(disabledAgent));

        assertThatThrownBy(() ->
                service.assignAgentToProperty(
                        propertyId,
                        request,
                        agencyUser(ownerId)
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Agent not found");
    }

    @Test
    void assignAgentToProperty_throws_whenPropertyAlreadyAssignedToSameAgent() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .agency(agency)
                .build();

        UserEntity agent = buildAgentUser(
                agentId,
                agency,
                true,
                false
        );

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .agency(agency)
                .status(PropertyStatus.ACTIVE)
                .assignedAgent(agent)
                .build();

        AssignAgentToPropertyRequest request =
                new AssignAgentToPropertyRequest();

        request.setAgentId(agentId);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        assertThatThrownBy(() ->
                service.assignAgentToProperty(
                        propertyId,
                        request,
                        agencyUser(ownerId)
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already assigned");
    }

    @Test
    void assignAgentToProperty_throws_whenAgentBelongsToAnotherAgency() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();

        AgencyEntity myAgency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        AgencyEntity otherAgency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .agency(myAgency)
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .agency(myAgency)
                .status(PropertyStatus.ACTIVE)
                .build();

        UserEntity agent = buildAgentUser(
                agentId,
                otherAgency,
                true,
                false
        );

        AssignAgentToPropertyRequest request =
                new AssignAgentToPropertyRequest();

        request.setAgentId(agentId);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(userRepository.findById(agentId))
                .thenReturn(Optional.of(agent));

        assertThatThrownBy(() ->
                service.assignAgentToProperty(
                        propertyId,
                        request,
                        agencyUser(ownerId)
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Agent not found with id: " + agentId);
    }

    @Test
    void assignAgentToProperty_throws_whenSelectedUserIsNotAnAgent() {
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID nonAgentId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder()
                .id(UUID.randomUUID())
                .build();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .agency(agency)
                .build();

        PropertyEntity property = PropertyEntity.builder()
                .id(propertyId)
                .agency(agency)
                .status(PropertyStatus.ACTIVE)
                .build();

        UserEntity nonAgentUser = UserEntity.builder()
                .id(nonAgentId)
                .agency(agency)
                .enabled(true)
                .deleted(false)
                .roles(Set.of(
                        RoleEntity.builder()
                                .roleName(Role.AGENCY_OWNER)
                                .build()
                ))
                .build();

        AssignAgentToPropertyRequest request =
                new AssignAgentToPropertyRequest();

        request.setAgentId(nonAgentId);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(property));

        when(userRepository.findById(nonAgentId))
                .thenReturn(Optional.of(nonAgentUser));

        assertThatThrownBy(() ->
                service.assignAgentToProperty(
                        propertyId,
                        request,
                        agencyUser(ownerId)
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Agent not found with id: " + nonAgentId);
    }

    @Test
    void getPopularProperties_returnsActivePropertiesMappedToResponses() {
        Pageable pageable = Pageable.ofSize(10);

        UUID propertyId1 = UUID.randomUUID();
        UUID propertyId2 = UUID.randomUUID();

        PropertyEntity property1 = PropertyEntity.builder()
                .id(propertyId1)
                .status(PropertyStatus.ACTIVE)
                .build();

        PropertyEntity property2 = PropertyEntity.builder()
                .id(propertyId2)
                .status(PropertyStatus.ACTIVE)
                .build();

        PropertyResponse response1 = PropertyResponse.builder()
                .id(propertyId1)
                .title("Luxury Apartment")
                .status(PropertyStatus.ACTIVE)
                .build();

        PropertyResponse response2 = PropertyResponse.builder()
                .id(propertyId2)
                .title("Modern Villa")
                .status(PropertyStatus.ACTIVE)
                .build();

        Page<PropertyEntity> propertyPage =
                new PageImpl<>(
                        List.of(property1, property2),
                        pageable,
                        2
                );

        when(propertyViewRepository.findPopularProperties(
                any(LocalDateTime.class),
                eq(PropertyStatus.ACTIVE),
                eq(pageable)
        )).thenReturn(propertyPage);

        when(propertyMapper.toPublicClientResponse(property1))
                .thenReturn(response1);

        when(propertyMapper.toPublicClientResponse(property2))
                .thenReturn(response2);

        Page<PropertyResponse> result =
                service.getPopularProperties(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).containsExactly(
                response1,
                response2
        );
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(10);

        verify(propertyViewRepository).findPopularProperties(
                any(LocalDateTime.class),
                eq(PropertyStatus.ACTIVE),
                eq(pageable)
        );

        verify(propertyMapper).toPublicClientResponse(property1);
        verify(propertyMapper).toPublicClientResponse(property2);
    }
}