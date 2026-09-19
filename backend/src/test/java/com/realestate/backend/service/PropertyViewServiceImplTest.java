package com.realestate.backend.service;

import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.PropertyViewEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.repository.PropertyViewRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.PropertyViewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyViewServiceImplTest {

    @Mock
    private PropertyViewRepository propertyViewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserDetails currentUser;

    private PropertyViewServiceImpl service;

    private PropertyEntity property;
    private UserEntity viewer;

    @BeforeEach
    void setUp() {
        service = new PropertyViewServiceImpl(
                propertyViewRepository,
                userRepository
        );

        property = PropertyEntity.builder()
                .id(UUID.randomUUID())
                .viewCount(10L)
                .build();

        viewer = UserEntity.builder()
                .id(UUID.randomUUID())
                .build();
    }

    @Test
    void recordView_shouldCreateViewAndIncrementCount_whenAuthenticatedUserHasNotViewedRecently() {
        UUID userId = viewer.getId();

        when(currentUser.getId()).thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(viewer));

        when(propertyViewRepository
                .existsByPropertyIdAndViewerIdAndViewedAtAfter(
                        eq(property.getId()),
                        eq(userId),
                        any()
                ))
                .thenReturn(false);

        service.recordView(property, currentUser);

        assertThat(property.getViewCount())
                .isEqualTo(11L);

        verify(propertyViewRepository)
                .save(any(PropertyViewEntity.class));
    }

    @Test
    void recordView_shouldNotCreateView_whenAuthenticatedUserViewedRecently() {
        UUID userId = viewer.getId();

        when(currentUser.getId()).thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(viewer));

        when(propertyViewRepository
                .existsByPropertyIdAndViewerIdAndViewedAtAfter(
                        eq(property.getId()),
                        eq(userId),
                        any()
                ))
                .thenReturn(true);

        service.recordView(property, currentUser);

        assertThat(property.getViewCount())
                .isEqualTo(10L);

        verify(propertyViewRepository, never())
                .save(any(PropertyViewEntity.class));
    }

    @Test
    void recordView_shouldThrowException_whenAuthenticatedUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(currentUser.getId()).thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.recordView(property, currentUser)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: " + userId);

        assertThat(property.getViewCount())
                .isEqualTo(10L);

        verify(propertyViewRepository, never())
                .save(any(PropertyViewEntity.class));
    }

    @Test
    void recordView_shouldCreateViewAndIncrementCount_whenUserIsAnonymous() {

        service.recordView(property, null);

        assertThat(property.getViewCount())
                .isEqualTo(11L);

        ArgumentCaptor<PropertyViewEntity> captor =
                ArgumentCaptor.forClass(PropertyViewEntity.class);

        verify(propertyViewRepository)
                .save(captor.capture());

        PropertyViewEntity savedView = captor.getValue();

        assertThat(savedView.getProperty())
                .isSameAs(property);

        assertThat(savedView.getViewer())
                .isNull();

        verifyNoInteractions(userRepository);
    }

    @Test
    void recordView_shouldCreateAnotherView_whenAnonymousUserRequestsAgain() {

        service.recordView(property, null);
        service.recordView(property, null);

        assertThat(property.getViewCount())
                .isEqualTo(12L);

        verify(propertyViewRepository, times(2))
                .save(any(PropertyViewEntity.class));

        verifyNoInteractions(userRepository);
    }
}