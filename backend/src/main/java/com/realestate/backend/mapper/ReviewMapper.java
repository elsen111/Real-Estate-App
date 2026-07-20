package com.realestate.backend.mapper;

import com.realestate.backend.dto.request.CreateReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.ReviewEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.ReviewTargetType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "target", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "property.id", source = "propertyId")
    @Mapping(target = "reviewer", source = "reviewer")
    @Mapping(target = "agency", source = "agency")
    ReviewEntity toEntity(CreateReviewRequest request, UUID propertyId, UserEntity reviewer, AgencyEntity agency);

    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "reviewerId", source = "reviewer.id")
    @Mapping(target = "agencyId", source = "agency.id")
    @Mapping(target = "reviewerName", source = "reviewer.fullName")
    @Mapping(target = "reviewerEmail", source = "reviewer.email")
    ReviewResponse toResponse(ReviewEntity reviewEntity);

}
