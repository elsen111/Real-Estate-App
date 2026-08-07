package com.realestate.backend.service;

import com.realestate.backend.dto.request.CreateAppointmentRequest;
import com.realestate.backend.dto.request.UpdateAppointmentStatusRequest;
import com.realestate.backend.dto.response.AppointmentResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.BusinessException;
import com.realestate.backend.exception.DuplicateAppointmentException;
import com.realestate.backend.exception.ForbiddenException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.AppointmentMapper;
import com.realestate.backend.repository.AppointmentRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private AppointmentMapper appointmentMapper;

    @InjectMocks private AppointmentServiceImpl service;

    private CustomUserDetails clientUser(UUID id) {
        return CustomUserDetails.from(UserEntity.builder().id(id).roles(Set.of(
                RoleEntity.builder().roleName(Role.CLIENT).build())).build());
    }

    @Test
    void createAppointment_throws_whenPropertyNotActive() {
        UUID propertyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PropertyEntity property = PropertyEntity.builder().id(propertyId).status(PropertyStatus.PENDING).build();
        UserEntity client = UserEntity.builder().id(userId).build();

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(userRepository.findById(userId)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.createAppointment(propertyId,
                new CreateAppointmentRequest(), clientUser(userId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createAppointment_throws_whenPendingAppointmentAlreadyExists() {
        UUID propertyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PropertyEntity property = PropertyEntity.builder().id(propertyId).status(PropertyStatus.ACTIVE).build();
        UserEntity client = UserEntity.builder().id(userId).build();

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(userRepository.findById(userId)).thenReturn(Optional.of(client));
        when(appointmentRepository.existsByPropertyIdAndClientIdAndStatus(
                propertyId, userId, AppointmentStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> service.createAppointment(propertyId,
                new CreateAppointmentRequest(), clientUser(userId)))
                .isInstanceOf(DuplicateAppointmentException.class);
    }

    @Test
    void cancelAppointment_throws_whenNotOwnAppointment() {
        UUID appointmentId = UUID.randomUUID();
        UUID otherClientId = UUID.randomUUID();
        AppointmentEntity appointment = AppointmentEntity.builder().id(appointmentId)
                .client(UserEntity.builder().id(otherClientId).build())
                .status(AppointmentStatus.PENDING).build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.cancelAppointment(appointmentId, clientUser(UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelAppointment_throws_whenStatusNotCancellable() {
        UUID appointmentId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        AppointmentEntity appointment = AppointmentEntity.builder().id(appointmentId)
                .client(UserEntity.builder().id(clientId).build())
                .status(AppointmentStatus.COMPLETED).build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.cancelAppointment(appointmentId, clientUser(clientId)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getMyAgencyAppointments_throws_whenCallerHasNoAgencyRole() {
        CustomUserDetails clientOnly = clientUser(UUID.randomUUID());

        assertThatThrownBy(() -> service.getMyAgencyAppointments(clientOnly, null, null, null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateStatus_throws_whenSettingBackToPending() {
        UUID appointmentId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        AgencyEntity agency = AgencyEntity.builder().id(agencyId).build();
        AppointmentEntity appointment = AppointmentEntity.builder().id(appointmentId)
                .agency(agency).status(AppointmentStatus.APPROVED).build();
        CustomUserDetails owner = CustomUserDetails.from(UserEntity.builder().id(UUID.randomUUID())
                .roles(Set.of(RoleEntity.builder().roleName(Role.SUPER_ADMIN).build())).build());

        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest();
        request.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.updateStatus(owner, appointmentId, request))
                .isInstanceOf(com.realestate.backend.exception.BadRequestException.class);
    }

    @Test
    void getAppointmentById_throws_whenAppointmentNotFound() {
        UUID appointmentId = UUID.randomUUID();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getAppointmentById(clientUser(UUID.randomUUID()), appointmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(appointmentId.toString());
    }

    @Test
    void getAppointmentById_returnsAppointment_whenCallerIsSuperAdmin() {
        UUID appointmentId = UUID.randomUUID();
        AppointmentEntity appointment = AppointmentEntity.builder()
                .id(appointmentId)
                .client(UserEntity.builder().id(UUID.randomUUID()).build())
                .status(AppointmentStatus.PENDING)
                .build();
        AppointmentResponse expected = AppointmentResponse.builder().id(appointmentId).build();

        CustomUserDetails superAdmin = userWithRole(UUID.randomUUID(), Role.SUPER_ADMIN);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toResponse(appointment)).thenReturn(expected);

        AppointmentResponse result = service.getAppointmentById(superAdmin, appointmentId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAppointmentById_returnsAppointment_whenCallerIsTheClient() {
        UUID appointmentId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        AppointmentEntity appointment = AppointmentEntity.builder()
                .id(appointmentId)
                .client(UserEntity.builder().id(clientId).build())
                .status(AppointmentStatus.PENDING)
                .build();
        AppointmentResponse expected = AppointmentResponse.builder().id(appointmentId).build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toResponse(appointment)).thenReturn(expected);

        AppointmentResponse result = service.getAppointmentById(clientUser(clientId), appointmentId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAppointmentById_returnsAppointment_whenCallerIsTheAssignedAgent() {
        UUID appointmentId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        AppointmentEntity appointment = AppointmentEntity.builder()
                .id(appointmentId)
                .client(UserEntity.builder().id(UUID.randomUUID()).build())
                .agent(UserEntity.builder().id(agentId).build())
                .status(AppointmentStatus.APPROVED)
                .build();
        AppointmentResponse expected = AppointmentResponse.builder().id(appointmentId).build();

        CustomUserDetails agentUser = userWithRole(agentId, Role.AGENT);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toResponse(appointment)).thenReturn(expected);

        AppointmentResponse result = service.getAppointmentById(agentUser, appointmentId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAppointmentById_returnsAppointment_whenCallerIsAgencyOwnerOfMatchingAgency() {
        UUID appointmentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();

        AgencyEntity agency = AgencyEntity.builder().id(agencyId).build();
        AppointmentEntity appointment = AppointmentEntity.builder()
                .id(appointmentId)
                .client(UserEntity.builder().id(UUID.randomUUID()).build())
                .agency(agency)
                .status(AppointmentStatus.PENDING)
                .build();
        AppointmentResponse expected = AppointmentResponse.builder().id(appointmentId).build();

        UserEntity ownerEntity = UserEntity.builder()
                .id(ownerId)
                .agency(agency)
                .roles(Set.of(RoleEntity.builder().roleName(Role.AGENCY_OWNER).build()))
                .build();
        CustomUserDetails owner = CustomUserDetails.from(ownerEntity);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(ownerEntity));
        when(appointmentMapper.toResponse(appointment)).thenReturn(expected);

        AppointmentResponse result = service.getAppointmentById(owner, appointmentId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAppointmentById_throws_whenAgencyOwnerBelongsToDifferentAgency() {
        UUID appointmentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        AgencyEntity appointmentAgency = AgencyEntity.builder().id(UUID.randomUUID()).build();
        AgencyEntity ownerAgency = AgencyEntity.builder().id(UUID.randomUUID()).build();

        AppointmentEntity appointment = AppointmentEntity.builder()
                .id(appointmentId)
                .client(UserEntity.builder().id(UUID.randomUUID()).build())
                .agency(appointmentAgency)
                .status(AppointmentStatus.PENDING)
                .build();

        UserEntity ownerEntity = UserEntity.builder()
                .id(ownerId)
                .agency(ownerAgency)
                .roles(Set.of(RoleEntity.builder().roleName(Role.AGENCY_OWNER).build()))
                .build();
        CustomUserDetails owner = CustomUserDetails.from(ownerEntity);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(ownerEntity));

        assertThatThrownBy(() -> service.getAppointmentById(owner, appointmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(appointmentId.toString());
    }

    @Test
    void getAppointmentById_throws_whenCallerIsUnrelatedClient() {
        UUID appointmentId = UUID.randomUUID();
        AppointmentEntity appointment = AppointmentEntity.builder()
                .id(appointmentId)
                .client(UserEntity.builder().id(UUID.randomUUID()).build())
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() ->
                service.getAppointmentById(clientUser(UUID.randomUUID()), appointmentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(appointmentId.toString());
    }

//    HELPER METHODS
    private CustomUserDetails userWithRole(UUID id, Role role) {
        return CustomUserDetails.from(UserEntity.builder().id(id).roles(Set.of(
                RoleEntity.builder().roleName(role).build())).build());
    }

}