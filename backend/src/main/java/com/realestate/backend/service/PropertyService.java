package com.realestate.backend.service;

import com.realestate.backend.dto.request.PropertyMapFilterRequest;
import com.realestate.backend.dto.request.PropertyRequest;
import com.realestate.backend.dto.request.PropertyPublicFilterRequest;
import com.realestate.backend.dto.request.PropertyStatusRequest;
import com.realestate.backend.dto.response.*;
import com.realestate.backend.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

    Page<PropertyMapResponse> getMapProperties(PropertyMapFilterRequest request, Pageable pageable);

    List<PropertyMediaResponse> uploadMedia(
            UUID propertyId,
            List<MultipartFile> files,
            CustomUserDetails currentUser
    );

    List<PropertyMediaResponse> getPropertyMedia(UUID propertyId);

    List<SetPropertyMediaResponse> setPrimaryImage(UUID propertyId, UUID propertyMediaId, CustomUserDetails currentUser);

    void removePropertyMediaFile(CustomUserDetails currentUser, UUID propertyMediaId);

}
