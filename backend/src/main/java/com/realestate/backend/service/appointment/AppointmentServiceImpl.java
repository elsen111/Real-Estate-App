package com.realestate.backend.service.appointment;

import com.realestate.backend.dto.appointment.request.CreateAppointmentRequest;
import com.realestate.backend.dto.appointment.request.UpdateAppointmentStatusRequest;
import com.realestate.backend.dto.appointment.response.AppointmentResponse;
import com.realestate.backend.entity.*;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.*;
import com.realestate.backend.mapper.appointment.AppointmentMapper;
import com.realestate.backend.repository.AgencyMemberRepository;
import com.realestate.backend.repository.AppointmentRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
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

    }

    @Override
    @Transactional
    public Page<AppointmentResponse> getMyAgencyAppointments(
            CustomUserDetails currentUser,
            AppointmentStatus status,
            UUID propertyId,
            Pageable pageable) {

        if (hasRole(currentUser, "AGENCY_OWNER") || hasRole(currentUser, "AGENT")) {

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

        appointment.setStatus(request.getStatus());
        appointment.setResponseNote(request.getResponseNote());

        AppointmentEntity updatedAppointment = appointmentRepository.saveAndFlush(appointment);

        return appointmentMapper.toResponse(updatedAppointment);
    }

    private boolean hasRole(CustomUserDetails user, String roleName) {
        String target = SecurityConstants.ROLE_PREFIX + roleName;
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(target::equals);
    }

    private boolean canManageAppointment(AppointmentEntity appointment, CustomUserDetails currentUser) {
        if (hasRole(currentUser, "SUPER_ADMIN")) {
            return true;
        }

        return (hasRole(currentUser, "AGENCY_OWNER") || hasRole(currentUser, "AGENT"))
                && appointment.getAgency() != null
                && agencyMemberRepository.existsByAgencyIdAndUserIdAndActiveTrue(
                appointment.getAgency().getId(), currentUser.getId());
    }

}
