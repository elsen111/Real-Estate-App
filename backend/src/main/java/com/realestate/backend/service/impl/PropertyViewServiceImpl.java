package com.realestate.backend.service.impl;

import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.PropertyViewEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.exception.ResourceNotFoundException;
import com.realestate.backend.repository.PropertyViewRepository;
import com.realestate.backend.repository.UserRepository;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.PropertyViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PropertyViewServiceImpl implements PropertyViewService {

    private static final long VIEW_COOLDOWN_HOURS = 24;

    private final PropertyViewRepository propertyViewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void recordView(PropertyEntity property, CustomUserDetails user) {

        if (user == null) {
            createNewRecord(property, null);
            return;
        }

        UserEntity viewer = userRepository.findById(user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + user.getId()
                        )
                );


        LocalDateTime threshold = LocalDateTime.now().minusHours(VIEW_COOLDOWN_HOURS);

        boolean alreadyViewedDuringLastDay = propertyViewRepository.existsByPropertyIdAndViewerIdAndViewedAtAfter(
                property.getId(),
                viewer.getId(),
                threshold
        );

        if (!alreadyViewedDuringLastDay) {
            createNewRecord(property, viewer);
        }

    }

    void createNewRecord(PropertyEntity property, UserEntity viewer) {
        PropertyViewEntity propertyView = PropertyViewEntity
                .builder()
                .property(property)
                .viewer(viewer)
                .build();

        propertyViewRepository.save(propertyView);

        property.setViewCount(property.getViewCount() + 1);
    }

}
