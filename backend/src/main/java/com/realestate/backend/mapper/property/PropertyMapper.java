package com.realestate.backend.mapper.property;

import com.realestate.backend.dto.admin.property.response.AdminAgencyPropertyResponse;
import com.realestate.backend.dto.agent.response.AgentResponse;
import com.realestate.backend.dto.media.response.PropertyImageResponse;
import com.realestate.backend.dto.property.request.PropertyRequest;
import com.realestate.backend.dto.property.response.*;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    AdminAgencyPropertyResponse toAdminResponse(PropertyEntity property);

    PropertyResponse toAdminPropertyResponse(PropertyEntity property);

    @Mapping(target = "status", ignore = true)
    PropertyResponse toPublicAgencyPropertyResponse(PropertyEntity property);

    @Mapping(target = "agencyId", source = "agency.id")
    @Mapping(target = "agencyName", source = "agency.name")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "assignedAgentId", source = "assignedAgent.id")
    @Mapping(target = "assignedAgentName", source = "assignedAgent.fullName")
    PropertyResponse toCreateResponse(PropertyEntity property);

    @Mapping(target = "propertyType", source = "category.name")
    @Mapping(target = "agency", source = "agency")
    @Mapping(target = "agent", source = "assignedAgent")
    @Mapping(target = "propertyStatus", source = "status")
    @Mapping(target = "images", ignore = true)
    PropertyDetailResponse toDetailResponse(PropertyEntity property);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "assignedAgent", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "featured", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PropertyEntity toEntity(PropertyRequest request);

//    Delete this method and use the second one next to below to give response after media implementation
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "agencyId", source = "agency.id")
    @Mapping(target = "agencyName", source = "agency.name")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "assignedAgentId", source = "assignedAgent.id")
    @Mapping(target = "assignedAgentName", source = "assignedAgent.fullName")
    PropertyResponse toPublicClientResponse(PropertyEntity property);

//    After media implementation connect the method above with this method by changing its name to toPublicClientResponse
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "agencyId", source = "property.agency.id")
    @Mapping(target = "agencyName", source = "property.agency.name")
    @Mapping(target = "categoryId", source = "property.category.id")
    @Mapping(target = "categoryName", source = "property.category.name")
    @Mapping(target = "assignedAgentId", source = "property.assignedAgent.id")
    @Mapping(target = "assignedAgentName", source = "property.assignedAgent.fullName")
    @Mapping(target = "mainImageUrl", source = "mainImageUrl")
    PropertyResponse toPublicClientResponseWithImage(PropertyEntity property, String mainImageUrl);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agency", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "assignedAgent", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(PropertyRequest request, @MappingTarget PropertyEntity property);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "agency", ignore = true)
    AgentResponse toAgentResponse(UserEntity agent);

    List<PropertyImageResponse> toImageResponseList(List<PropertyImageResponse> images);

    @Mapping(target = "properties", source = "properties")
    @Mapping(target = "cities", source = "cities")
    @Mapping(target = "districts", source = "districts")
    PropertySearchSuggestionResponse toSuggestionsResponse(
            List<PropertySuggestionResponse> properties,
            List<String> cities,
            List<String> districts
    );

//    @Mapping(target = "mainImageUrl", source = "mainImageUrl")
//    PropertyMapResponse toPropertyMapResponse(PropertyEntity property, String mainImageUrl);

    PropertyMapResponse toPropertyMapResponse(PropertyEntity property);

}
