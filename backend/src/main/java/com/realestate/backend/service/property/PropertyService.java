package com.realestate.backend.service.property;

import com.realestate.backend.dto.property.request.PropertyRequest;
import com.realestate.backend.dto.property.request.PropertyPublicFilterRequest;
import com.realestate.backend.dto.property.request.PropertyStatusRequest;
import com.realestate.backend.dto.property.response.PropertyDetailResponse;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.dto.property.response.PropertySearchSuggestionResponse;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PropertyService {

    PropertyResponse createProperty(PropertyRequest createdProperty, CustomUserDetails currentUser);

    Page<PropertyResponse> getAllPublicProperties(
            PropertyPublicFilterRequest filter,
            Pageable pageable
    );

    PropertyDetailResponse getPropertyDetailsById(UUID propertyId, CustomUserDetails currentUser);

    PropertyResponse updateProperty(UUID propertyId, PropertyRequest updatedProperty, CustomUserDetails currentUser);

    void updateStatus(UUID propertyId, PropertyStatusRequest request, CustomUserDetails currentUser);

    PropertyResponse toggleFeaturedProperty(UUID propertyId, CustomUserDetails currentUser);

    void softDeleteProperty(UUID propertyId, CustomUserDetails currentUser);

    Page<PropertyResponse> getFeaturedProperties(
            PropertyPublicFilterRequest filter,
            Pageable pageable
    );

    Page<PropertyResponse> getRecentProperties(
            PropertyPublicFilterRequest filter,
            int size
    );

    Page<PropertyResponse> getSimilarProperties(
            UUID propertyId,
            Pageable pageable
    );

    PropertySearchSuggestionResponse getSearchSuggestions(String keyword);

}
