package com.realestate.backend.service.property;

import com.realestate.backend.dto.property.request.CreatePropertyRequest;
import com.realestate.backend.dto.property.response.PropertyResponse;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.security.CustomUserDetails;

public interface PropertyService {

    PropertyResponse createProperty(CreatePropertyRequest createdProperty, CustomUserDetails currentUser);

}
