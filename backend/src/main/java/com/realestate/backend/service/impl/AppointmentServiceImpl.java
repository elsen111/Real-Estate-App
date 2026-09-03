package com.realestate.backend.service.impl;

import com.realestate.backend.dto.request.AppointmentFilterRequest;
import com.realestate.backend.dto.request.CreateAppointmentRequest;
import com.realestate.backend.dto.request.UpdateAppointmentStatusRequest;
import com.realestate.backend.dto.response.AppointmentResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.Role;
import com.realestate.backend.exception.*;
import com.realestate.backend.mapper.AppointmentMapper;
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.AppointmentRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.repository.specification.AppointmentSpecification;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.SecurityConstants;
import com.realestate.backend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    private final UserRepository userRepository;

    private final PropertyRepository propertyRepository;

    private final AgencyMemberRepository agencyMemberRepository;


    @Override
    @Transactional
    public AppointmentResponse createAppointment(UUID propertyId, CreateAppointmentRequest request, CustomUserDetails currentUser) {

        PropertyEntity property = propertyRepository.findById(propertyId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Property not found with id: " + propertyId)
                );

        UserEntity client = userRepository.findById(currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
                );

        if(property.getStatus() != PropertyStatus.ACTIVE){
            throw new ResourceNotFoundException("Active property not found with id: " + propertyId);
        }

        boolean hasPendingAppointment = appointmentRepository.existsByPropertyIdAndClientIdAndStatus(
                propertyId,
                client.getId(),
                AppointmentStatus.PENDING
        );

        if (hasPendingAppointment){
            throw new DuplicateAppointmentException("Pending appointment already exists with id: " + propertyId);
        };

        AppointmentEntity newAppointment = AppointmentEntity.builder()
                .property(property)
                .client(client)
                .agency(property.getAgency())
                .agent(property.getAssignedAgent())
                .note(request.getNote())
                .preferredDateTime(request.getPreferredDateTime())
                .build();

        AppointmentEntity savedAppointment = appointmentRepository.saveAndFlush(newAppointment);

        log.atInfo()
                .setMessage("Appointment created for the property.")
                .addKeyValue("appointmentId", savedAppointment.getId())
                .addKeyValue("appointmentType", savedAppointment.getAppointmentType())
                .addKeyValue("propertyId", property.getId())
                .addKeyValue("propertyTitle", property.getTitle())
                .addKeyValue("agencyId", savedAppointment.getAgency().getId())
                .addKeyValue("agencyName", savedAppointment.getAgency().getName())
                .addKeyValue("agentId", savedAppointment.getAgent() != null ? savedAppointment.getAgent().getId() : null)
                .addKeyValue("agentEmail", savedAppointment.getAgent() != null ? savedAppointment.getAgent().getEmail() : null)
                .log();


        return appointmentMapper.toResponse(savedAppointment);

    }

    @Override
    public Page<AppointmentResponse> getClientAppointments(CustomUserDetails currentUser, AppointmentStatus status, Pageable pageable) {

        Page<AppointmentEntity> inquiries = status == null
                ? appointmentRepository.findByClientId(currentUser.getId(), pageable)
                : appointmentRepository.findByClientIdAndStatus(currentUser.getId(), status, pageable);

        return inquiries.map(appointmentMapper::toResponse);

    }

    @Override
    public AppointmentResponse getAppointmentById(
            CustomUserDetails currentUser,
            UUID appointmentId) {

        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Appointment not found with id: " + appointmentId));

        boolean canViewAppointment = false;

        if (hasRole(currentUser, Role.SUPER_ADMIN)) {
            canViewAppointment = true;
        }

        else if (appointment.getClient() != null
                && appointment.getClient().getId().equals(currentUser.getId())) {
            canViewAppointment = true;
        }

        else if (appointment.getAgent() != null
                && appointment.getAgent().getId().equals(currentUser.getId())) {
            canViewAppointment = true;
        }

        else if (hasRole(currentUser, Role.AGENCY_OWNER)) {

            UserEntity authenticatedUser = userRepository.findById(currentUser.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found with id: " + currentUser.getId()));

            canViewAppointment =
                    authenticatedUser.getAgency() != null
                            && appointment.getAgency() != null
                            && authenticatedUser.getAgency().getId()
                            .equals(appointment.getAgency().getId());
        }

        if (!canViewAppointment) {
            throw new ResourceNotFoundException(
                    "Appointment not found with id: " + appointmentId);
        }

        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(UUID appointmentId, CustomUserDetails currentUser) {

        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        if (!appointment.getClient().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Appointment not found with id: " + appointmentId);
        }

        if(
                appointment.getStatus() != AppointmentStatus.PENDING &&
                appointment.getStatus() != AppointmentStatus.APPROVED
        ){
            throw new BusinessException("Only pending and approved appointments can be cancelled.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.saveAndFlush(appointment);


        log.atInfo()
                .setMessage("Appointment cancelled.")
                .addKeyValue("appointmentId", appointment.getId())
                .addKeyValue("appointmentType", appointment.getAppointmentType())
                .addKeyValue("propertyId", appointment.getProperty().getId())
                .addKeyValue("propertyTitle", appointment.getProperty().getTitle())
                .addKeyValue("agencyId", appointment.getAgency().getId())
                .addKeyValue("agencyName", appointment.getAgency().getName())
                .addKeyValue("agentId", appointment.getAgent() != null ? appointment.getAgent().getId() : null)
                .addKeyValue("agentEmail", appointment.getAgent() != null ? appointment.getAgent().getEmail() : null)
                .log();

    }

    @Override
    @Transactional
    public Page<AppointmentResponse> getMyAgencyAppointments(
            CustomUserDetails currentUser,
            AppointmentStatus status,
            UUID propertyId,
            Pageable pageable) {

        if (hasRole(currentUser, Role.AGENCY_OWNER) || hasRole(currentUser, Role.AGENT)) {

            AgencyMemberEntity agencyMember = agencyMemberRepository.findByUser_IdAndActiveTrue(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("You are not an active member of any agency"));

            UUID agencyId = agencyMember.getAgency().getId();

            Page<AppointmentEntity> appointments = appointmentRepository
                    .findByAgencyIdWithFilters(agencyId, status, propertyId, pageable);

            return appointments.map(appointmentMapper::toResponse);
        }

        throw new ForbiddenException("You do not have permission to view agency appointments");
    }

    @Override
    @Transactional
    public AppointmentResponse updateStatus(CustomUserDetails currentUser, UUID appointmentId, UpdateAppointmentStatusRequest request) {

        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId)
                );

        if(!canManageAppointment(appointment, currentUser)){
            throw new ForbiddenException("You do not have permission to update this appointment");
        }

        if(request.getStatus() == AppointmentStatus.PENDING){
            throw new BadRequestException("Status cannot be changed to PENDING");
        }

        if(request.getStatus() == AppointmentStatus.APPROVED){
            appointment.setConfirmedDateTime(LocalDateTime.now());
        }

        AppointmentStatus oldStatus = appointment.getStatus();

        appointment.setStatus(request.getStatus());
        appointment.setResponseNote(request.getResponseNote());

        AppointmentEntity updatedAppointment = appointmentRepository.saveAndFlush(appointment);

        log.atInfo()
                .setMessage("Appointment status changed.")
                .addKeyValue("appointmentId", appointment.getId())
                .addKeyValue("appointmentType", appointment.getAppointmentType())
                .addKeyValue("oldStatus", oldStatus)
                .addKeyValue("newStatus", appointment.getStatus())
                .addKeyValue("propertyId", appointment.getProperty().getId())
                .addKeyValue("propertyTitle", appointment.getProperty().getTitle())
                .addKeyValue("agencyId", appointment.getAgency().getId())
                .addKeyValue("agencyName", appointment.getAgency().getName())
                .addKeyValue("agentId", appointment.getAgent() != null ? appointment.getAgent().getId() : null)
                .addKeyValue("agentEmail", appointment.getAgent() != null ? appointment.getAgent().getEmail() : null)
                .addKeyValue("clientId", appointment.getClient().getId())
                .addKeyValue("clientEmail", appointment.getClient().getEmail())
                .log();


        return appointmentMapper.toResponse(updatedAppointment);
    }

    @Override
    public Page<AppointmentResponse> getAllAppointments(AppointmentFilterRequest filter, Pageable pageable) {

        Specification<AppointmentEntity> specification = AppointmentSpecification.withFilter(filter);

        return appointmentRepository.findAll(specification, pageable)
                .map(appointmentMapper::toResponse);

    }


    //    HELPER METHODS
    private boolean canManageAppointment(AppointmentEntity appointment, CustomUserDetails currentUser) {
        if (hasRole(currentUser, Role.SUPER_ADMIN)) {
            return true;
        }

        UserEntity authenticatedUser = userRepository.findById(currentUser.getId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + currentUser.getId())
        );

        return (hasRole(currentUser, Role.AGENCY_OWNER) || hasRole(currentUser, Role.AGENT))
                && appointment.getAgency() != null
                && agencyMemberRepository.existsByAgencyIdAndUserIdAndActiveTrue(
                appointment.getAgency().getId(), currentUser.getId())
                && appointment.getAgency().getId().equals(authenticatedUser.getAgency().getId());
    }

    public boolean hasRole(CustomUserDetails currentUser, Role role) {
        return currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role.name()));
    }

}
