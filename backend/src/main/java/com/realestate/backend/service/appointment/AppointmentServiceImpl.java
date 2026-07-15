package com.realestate.backend.service.appointment;

import com.realestate.backend.dto.appointment.request.CreateAppointmentRequest;
import com.realestate.backend.dto.appointment.response.AppointmentResponse;
import com.realestate.backend.entity.AppointmentEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.exception.DuplicateAppointmentException;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.mapper.appointment.AppointmentMapper;
import com.realestate.backend.repository.AppointmentRepository;
import com.realestate.backend.repository.PropertyRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;


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

}
