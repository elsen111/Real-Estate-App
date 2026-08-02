package com.realestate.backend.service;

import com.realestate.backend.dto.request.PropertyRequest;
import com.realestate.backend.dto.request.PropertyStatusRequest;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.BadRequestException;
import com.realestate.backend.exception.ConflictException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.repository.*;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private UserRepository userRepository;
    @Mock private AgencySubscriptionRepository agencySubscriptionRepository;

    @InjectMocks private PropertyServiceImpl service;

    private CustomUserDetails agencyUser(UUID id) {
        return CustomUserDetails.from(UserEntity.builder().id(id)
                .roles(Set.of(RoleEntity.builder().roleName(Role.AGENCY_OWNER).build())).build());
    }

    @Test
    void createProperty_throws_whenUserHasNoAgency() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).agency(null).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.createProperty(new PropertyRequest(), agencyUser(userId)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must belong to an agency");
    }

    @Test
    void createProperty_throws_whenNoActiveSubscription() {
        UUID userId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(UUID.randomUUID()).build();
        UserEntity user = UserEntity.builder().id(userId).agency(agency).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(agencySubscriptionRepository.findFirstByAgency_IdAndStatusOrderByEndDateDesc(agency.getId(), SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createProperty(new PropertyRequest(), agencyUser(userId)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("doesn't have an active subscription");
    }

    @Test
    void createProperty_throws_whenSubscriptionExpired() {
        UUID userId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(UUID.randomUUID()).build();
        UserEntity user = UserEntity.builder().id(userId).agency(agency).build();
        SubscriptionPlanEntity plan = SubscriptionPlanEntity.builder().maxListings(10).build();
        AgencySubscriptionEntity subscription = AgencySubscriptionEntity.builder()
                .agency(agency).plan(plan).endDate(LocalDate.now().minusDays(1)).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(agencySubscriptionRepository.findFirstByAgency_IdAndStatusOrderByEndDateDesc(agency.getId(), SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> service.createProperty(new PropertyRequest(), agencyUser(userId)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void createProperty_throws_whenListingLimitReached() {
        UUID userId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(UUID.randomUUID()).build();
        UserEntity user = UserEntity.builder().id(userId).agency(agency).build();
        SubscriptionPlanEntity plan = SubscriptionPlanEntity.builder().maxListings(5).build();
        AgencySubscriptionEntity subscription = AgencySubscriptionEntity.builder()
                .agency(agency).plan(plan).endDate(LocalDate.now().plusDays(10)).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(agencySubscriptionRepository.findFirstByAgency_IdAndStatusOrderByEndDateDesc(agency.getId(), SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(subscription));
        when(propertyRepository.countByAgencyId(agency.getId())).thenReturn(5L);

        assertThatThrownBy(() -> service.createProperty(new PropertyRequest(), agencyUser(userId)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Listing limit reached");
    }

    @Test
    void updateProperty_throws_whenPropertyBelongsToAnotherAgency() {
        UUID userId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        AgencyEntity myAgency = AgencyEntity.builder().id(UUID.randomUUID()).build();
        AgencyEntity otherAgency = AgencyEntity.builder().id(UUID.randomUUID()).build();
        UserEntity user = UserEntity.builder().id(userId).agency(myAgency).build();
        PropertyEntity property = PropertyEntity.builder().id(propertyId).agency(otherAgency).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        assertThatThrownBy(() -> service.updateProperty(propertyId, new PropertyRequest(), agencyUser(userId)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("another agency");
    }

    @Test
    void updateStatus_throws_whenStatusNotAllowedForAgencies() {
        UUID userId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(UUID.randomUUID()).build();
        UserEntity user = UserEntity.builder().id(userId).agency(agency).build();
        PropertyEntity property = PropertyEntity.builder().id(propertyId).agency(agency).build();

        PropertyStatusRequest request = new PropertyStatusRequest();
        request.setStatus(PropertyStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        assertThatThrownBy(() -> service.updateStatus(propertyId, request, agencyUser(userId)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SOLD, RENTED");
    }

    @Test
    void softDeleteProperty_throws_whenPropertyNotFound() {
        UUID propertyId = UUID.randomUUID();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.softDeleteProperty(propertyId, agencyUser(UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPropertyDetailsById_throws_whenPropertyHiddenFromPublic() {
        UUID propertyId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(UUID.randomUUID()).build();
        PropertyEntity property = PropertyEntity.builder().id(propertyId)
                .agency(agency).status(PropertyStatus.REJECTED).build();

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        assertThatThrownBy(() -> service.getPropertyDetailsById(propertyId, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}