package com.realestate.backend.service;

import com.realestate.backend.dto.response.AgencySubscriptionResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.SubscriptionStatus;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.repository.*;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.AgencyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgencyServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private AgencyRepository agencyRepository;
    @Mock private AgencySubscriptionRepository agencySubscriptionRepository;
    @Mock private PropertyRepository propertyRepository;

    @InjectMocks private AgencyServiceImpl service;

    private CustomUserDetails currentUser(UUID userId) {
        return CustomUserDetails.from(UserEntity.builder().id(userId).roles(Set.of()).build());
    }

    @Test
    void getCurrentAgency_throws_whenUserHasNoAgency() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(userId).agency(null).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.getCurrentAgency(currentUser(userId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMySubscription_throws_whenNoActiveSubscription() {
        UUID userId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(UUID.randomUUID()).build();
        UserEntity user = UserEntity.builder().id(userId).agency(agency).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(agencySubscriptionRepository.findByAgencyAndStatus(agency, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMySubscription(currentUser(userId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMySubscription_calculatesRemainingListingsAndAgents() {
        UUID userId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(UUID.randomUUID()).build();
        UserEntity user = UserEntity.builder().id(userId).agency(agency).build();
        SubscriptionPlanEntity plan = SubscriptionPlanEntity.builder()
                .id(UUID.randomUUID()).name("Gold").price(BigDecimal.TEN)
                .durationDays(30).maxListings(10).maxAgents(5).build();
        AgencySubscriptionEntity subscription = AgencySubscriptionEntity.builder()
                .agency(agency).plan(plan).status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(30)).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(agencySubscriptionRepository.findByAgencyAndStatus(agency, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(subscription));
        when(propertyRepository.countByAgencyIdAndStatusIn(
                agency.getId(),
                List.of(PropertyStatus.PENDING, PropertyStatus.ACTIVE)
        )).thenReturn(3L);
        when(userRepository.countByAgency(agency)).thenReturn(2L);

        AgencySubscriptionResponse response = service.getMySubscription(currentUser(userId));

        assertThat(response.getRemainingListings()).isEqualTo(7);
        assertThat(response.getRemainingAgents()).isEqualTo(3);
    }

    @Test
    void getPublicAgencyInfo_throws_whenAgencyNotFound() {
        UUID agencyId = UUID.randomUUID();
        when(agencyRepository.findById(agencyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPublicAgencyInfo(agencyId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}