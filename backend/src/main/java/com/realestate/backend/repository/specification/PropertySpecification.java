package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.agency.request.AgencyFilterRequest;
import com.realestate.backend.dto.property.request.PropertyFilterRequest;
import com.realestate.backend.dto.property.request.PropertyPublicFilterRequest;
import com.realestate.backend.entity.AgencyEntity;
import com.realestate.backend.entity.PropertyEntity;
import com.realestate.backend.enums.AgencyStatus;
import com.realestate.backend.enums.ListingType;
import com.realestate.backend.enums.PropertyStatus;
import com.realestate.backend.enums.PropertyType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import tools.jackson.databind.annotation.JsonAppend;

import java.math.BigDecimal;
import java.util.UUID;

public class PropertySpecification {

    public PropertySpecification() {}

    public static Specification<PropertyEntity> withFilter(
            PropertyFilterRequest filterRequest
    ) {

        if(filterRequest == null) {
            Specification.where((Specification<Object>) null);
        };

        assert filterRequest != null;

        return Specification.where(hasCity(filterRequest.getCity()))
                .and(hasStatus(filterRequest.getStatus()))
                .and(hasAgencyName(filterRequest.getAgencyName()))
                .and(isFeatured(filterRequest.getFeatured()))
                .and(hasQuery(filterRequest.getQuery()));

    }

    public static Specification<PropertyEntity> withPublicFilter(
            PropertyFilterRequest filterRequest
    ) {

        Specification<PropertyEntity> spec = Specification
                .where(hasStatus(PropertyStatus.ACTIVE));

        if (filterRequest == null) {
            return spec;
        }

        return spec
                .and(hasCity(filterRequest.getCity()))
                .and(hasAgencyName(filterRequest.getAgencyName()))
                .and(isFeatured(filterRequest.getFeatured()))
                .and(hasQuery(filterRequest.getQuery()));
    }

    public static Specification<PropertyEntity> withDetailedPublicFilter(
            PropertyPublicFilterRequest filterRequest
    ) {

        Specification<PropertyEntity> spec = Specification
                .where(hasStatus(PropertyStatus.ACTIVE));

        if (filterRequest == null) {
            return spec;
        }

        return spec
                .and(hasDistrict(filterRequest.getDistrict()))
                .and(hasCity(filterRequest.getCity()))
                .and(hasAgencyName(filterRequest.getAgencyName()))
                .and(hasPropertyType(filterRequest.getPropertyType()))
                .and(hasListingType(filterRequest.getListingType()))
                .and(priceBetween(filterRequest.getMinPrice(), filterRequest.getMaxPrice()))
                .and(areaBetween(filterRequest.getMinArea(), filterRequest.getMaxArea()))
                .and(hasRooms(filterRequest.getRoomCount()))
                .and(isFeatured(filterRequest.getFeatured()))
                .and(hasQuery(filterRequest.getQuery()));
    }

    public static Specification<PropertyEntity> withFeaturedPublicFilter(
            PropertyPublicFilterRequest filterRequest
    ) {
        Specification<PropertyEntity> spec = Specification
                .where(hasStatus(PropertyStatus.ACTIVE));

        if (filterRequest == null) {
            return spec;
        }

        return spec
                .and(hasCity(filterRequest.getCity()))
                .and(hasAgencyName(filterRequest.getAgencyName()))
                .and(hasPropertyType(filterRequest.getPropertyType()))
                .and(hasListingType(filterRequest.getListingType()))
                .and(priceBetween(filterRequest.getMinPrice(), filterRequest.getMaxPrice()))
                .and(areaBetween(filterRequest.getMinArea(), filterRequest.getMaxArea()))
                .and(hasQuery(filterRequest.getQuery()));
    }

    public static Specification<PropertyEntity> withRecentFilter(
            PropertyPublicFilterRequest filterRequest
    ) {
        Specification<PropertyEntity> spec = Specification
                .where(hasStatus(PropertyStatus.ACTIVE));

        if (filterRequest == null) {
            return spec;
        }

        return spec
                .and(hasCity(filterRequest.getCity()))
                .and(hasPropertyType(filterRequest.getPropertyType()))
                .and(hasListingType(filterRequest.getListingType()))
                .and(hasQuery(filterRequest.getQuery()));
    }

    public static Specification<PropertyEntity> withSimilarityFilter(PropertyEntity reference) {

        return Specification.where(hasStatus(PropertyStatus.ACTIVE))
                .and(hasCity(reference.getCity()))
                .and(hasCategoryId(reference.getCategory().getId()))
                .and(hasListingType(reference.getListingType()))
                .and(excludePropertyId(reference.getId()));
    }

    public static Specification<PropertyEntity> withSimilarityFilterRelaxed(PropertyEntity reference) {

        BigDecimal price = reference.getPrice();
        BigDecimal minPrice = price.multiply(BigDecimal.valueOf(0.8));
        BigDecimal maxPrice = price.multiply(BigDecimal.valueOf(1.2));

        BigDecimal area = reference.getArea();
        BigDecimal minArea = area.multiply(BigDecimal.valueOf(0.75));
        BigDecimal maxArea = area.multiply(BigDecimal.valueOf(1.25));

        return Specification.where(hasStatus(PropertyStatus.ACTIVE))
                .and(hasListingType(reference.getListingType()))
                .and(areaBetween(minArea, maxArea))
                .and(priceBetween(minPrice, maxPrice))
                .and(hasDistrict(reference.getDistrict()))
                .and(excludePropertyId(reference.getId()));
    }


    private static Specification<PropertyEntity> hasCity(String city) {
        return ((root, query, cb) -> city == null ? null : cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase()));
    }

    private static Specification<PropertyEntity> hasAgencyName(String agencyName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(agencyName)) {
                return null;
            }
            Join<PropertyEntity, AgencyEntity> agencyJoin = root.join("agency", JoinType.INNER);
            return cb.like(cb.lower(agencyJoin.get("name")), "%" + agencyName.trim().toLowerCase() + "%");
        };
    }

    private static Specification<PropertyEntity> hasStatus(PropertyStatus status) {
        return ((root, query, criteriaBuilder) -> status == null ? null : criteriaBuilder.equal(root.get("status"), status));
    }

    public static Specification<PropertyEntity> isFeatured(Boolean isFeatured) {
        return ((root, query, criteriaBuilder) -> isFeatured == null ? null : criteriaBuilder.equal(root.get("featured"), isFeatured));
    }

    private static Specification<PropertyEntity> hasQuery(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(keyword)) {
                return null;
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title").as(String.class)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description").as(String.class)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("address").as(String.class)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("district").as(String.class)), pattern)
            );

        };
    }

    private static Specification<PropertyEntity> hasDistrict(String district) {
        return (root, query, cb) -> !StringUtils.hasText(district)
                ? null
                : cb.equal(cb.lower(root.get("district")), district.trim().toLowerCase());
    }

    private static Specification<PropertyEntity> hasPropertyType(PropertyType propertyType) {
        return (root, query, cb) -> propertyType == null
                ? null
                : cb.equal(root.get("propertyType"), propertyType);
    }

    private static Specification<PropertyEntity> hasListingType(ListingType listingType) {
        return (root, query, cb) -> listingType == null
                ? null
                : cb.equal(root.get("listingType"), listingType);
    }

    private static Specification<PropertyEntity> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) {
                return null;
            }
            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get("price"), minPrice, maxPrice);
            }
            if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            }
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    private static Specification<PropertyEntity> hasRooms(Integer rooms) {
        return (root, query, cb) -> rooms == null
                ? null
                : cb.equal(root.get("rooms"), rooms);
    }

    private static Specification<PropertyEntity> areaBetween(BigDecimal minArea, BigDecimal maxArea) {
        return (root, query, cb) -> {
            if (minArea == null && maxArea == null) {
                return null;
            }
            if (minArea != null && maxArea != null) {
                return cb.between(root.get("area"), minArea, maxArea);
            }
            if (minArea != null) {
                return cb.greaterThanOrEqualTo(root.get("area"), minArea);
            }
            return cb.lessThanOrEqualTo(root.get("area"), maxArea);
        };
    }

    public static Specification<PropertyEntity> hasAssignedAgentId(UUID agentId) {
        return (root, query, cb) -> agentId == null
                ? null
                : cb.equal(root.get("assignedAgent").get("id"), agentId);
    }

    public static Specification<PropertyEntity> hasAgencyId(UUID agencyId) {
        return (root, query, cb) -> agencyId == null
                ? null
                : cb.equal(root.get("agency").get("id"), agencyId);
    }

    private static Specification<PropertyEntity> hasCategoryId(UUID  categoryId) {
        return (root, query, cb) -> categoryId == null
                ? null
                : cb.equal(root.get("category").get("id"), categoryId);
    }

    private static Specification<PropertyEntity> excludePropertyId(UUID propertyId) {
        return (root, query, cb) -> propertyId == null
            ? null
            : cb.notEqual(root.get("id"), propertyId);
    }


}
