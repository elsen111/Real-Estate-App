package com.realestate.backend.mapper;

import com.realestate.backend.dto.request.ReviewRequest;
import com.realestate.backend.dto.response.ReviewResponse;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.ReviewEntity;
import com.realestate.backend.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "target", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "property.id", source = "propertyId")
    @Mapping(target = "reviewer", source = "reviewer")
    @Mapping(target = "agency", source = "agency")
    ReviewEntity toEntity(ReviewRequest request, UUID propertyId, UserEntity reviewer, AgencyEntity agency);

    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "reviewerId", source = "reviewer.id")
    @Mapping(target = "agencyId", source = "agency.id")
    @Mapping(target = "reviewerName", source = "reviewer.fullName")
    @Mapping(target = "reviewerEmail", source = "reviewer.email")
    @Mapping(target = "reviewerAvatarUrl", source = "reviewer.profilePhotoUrl.media.fileUrl")
    ReviewResponse toResponse(ReviewEntity reviewEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "target", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "agency", ignore = true)
    @Mapping(target = "reviewer", source = "reviewer")
    void toEntity(ReviewRequest request, UserEntity reviewer, @MappingTarget ReviewEntity reviewEntity);

}
