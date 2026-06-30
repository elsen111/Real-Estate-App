package com.realestate.backend.mapper.property;

import com.realestate.backend.dto.admin.property.response.AdminAgencyPropertyResponse;
import com.realestate.backend.dto.admin.property.response.AdminPropertyResponse;
import com.realestate.backend.entity.PropertyEntity;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper {

    public AdminAgencyPropertyResponse toAdminResponse(PropertyEntity property) {

        if (property == null) {
            return null;
        }

        return AdminAgencyPropertyResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .price(property.getPrice())
                .city(property.getCity())
                .district(property.getDistrict())
                .address(property.getAddress())
                .listingType(property.getListingType())
                .area(property.getArea())
                .rooms(property.getRooms())
                .bathrooms(property.getBathrooms())
                .floor(property.getFloor())
                .totalFloors(property.getTotalFloors())
                .status(property.getStatus())
                .featured(property.isFeatured())
                .viewCount(property.getViewCount())
                .createdAt(property.getCreatedAt())
                .build();
    }

    public AdminPropertyResponse toAdminPropertyResponse(PropertyEntity property) {

        if (property == null) {
            return null;
        }

        return AdminPropertyResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .agencyName(property.getAgency().getName())
                .price(property.getPrice())
                .city(property.getCity())
                .district(property.getDistrict())
                .address(property.getAddress())
                .listingType(property.getListingType())
                .area(property.getArea())
                .rooms(property.getRooms())
                .bathrooms(property.getBathrooms())
                .floor(property.getFloor())
                .totalFloors(property.getTotalFloors())
                .status(property.getStatus())
                .featured(property.isFeatured())
                .viewCount(property.getViewCount())
                .createdAt(property.getCreatedAt())
                .build();
    }

}
