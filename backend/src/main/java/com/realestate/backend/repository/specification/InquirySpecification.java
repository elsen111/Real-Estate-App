package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.request.InquiryFilterRequest;
import com.realestate.backend.entity.InquiryEntity;
import com.realestate.backend.enums.InquiryStatus;
import com.realestate.backend.enums.InquiryType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class InquirySpecification {

    public InquirySpecification() {}

    public static Specification<InquiryEntity> withAgencyIdFilter(
            UUID agencyId,
            InquiryFilterRequest filterRequest
    ) {

        if(filterRequest == null) {
            Specification.where((Specification<Object>) null);
        };

        assert filterRequest != null;

        return Specification.where(hasAgencyId(agencyId))
                .and(hasStatus(filterRequest.status()))
                .and(hasAgentId(filterRequest.agentId()))
                .and(hasAgentName(filterRequest.agentName()))
                .and(hasClientId(filterRequest.clientId()))
                .and(hasClientEmail(filterRequest.clientEmail()))
                .and(hasClientName(filterRequest.clientName()))
                .and(hasPropertyId(filterRequest.propertyId()))
                .and(hasPropertyTitle(filterRequest.propertyTitle()))
                .and(hasContactMethod(filterRequest.contact()))
                .and(createTimeBetween(filterRequest.startDate(), filterRequest.endDate()));

    }

//    HELPER METHODS
    public static Specification<InquiryEntity> hasStatus(InquiryStatus status) {
        return (root, criteriaQuery, criteriaBuilder) ->
                status == null ? null
                : criteriaBuilder.equal(root.get("status"), status
        );
    }

    public static Specification<InquiryEntity> hasAgencyId(UUID agencyId) {

                return((root, query, criteriaBuilder) ->
                agencyId == null ? null
                : criteriaBuilder.equal(root.join("agency").get("id"), agencyId)
                );

    }

    public static Specification<InquiryEntity> hasAgentId(UUID agentId) {

        return (root, query, cb) -> {
            if (agentId == null) return null;
            Join<InquiryEntity, ?> agentJoin = getOrCreateJoin(root, "assignedAgent");
            return cb.equal(agentJoin.get("id"), agentId);
        };
    }

    public static Specification<InquiryEntity> hasAgentName(String agentName) {

        return (root, query, cb) -> {
            if (agentName == null || agentName.isBlank()) return null;
            Join<InquiryEntity, ?> agentJoin = getOrCreateJoin(root, "assignedAgent");
            return cb.like(cb.lower(agentJoin.get("fullName")), "%" + agentName.trim().toLowerCase() + "%");
        };

    }

    public static Specification<InquiryEntity> hasClientId(UUID clientId) {

        return (root, query, cb) -> {
            if (clientId == null) return null;
            Join<InquiryEntity, ?> agentJoin = getOrCreateJoin(root, "client");
            return cb.equal(agentJoin.get("id"), clientId);
        };
    }

    public static Specification<InquiryEntity> hasClientEmail(String clientEmail) {

        return (root, query, cb) -> {
            if (clientEmail == null || clientEmail.isBlank()) return null;
            Join<InquiryEntity, ?> agentJoin = getOrCreateJoin(root, "client");
            return cb.equal(root.join("client").get("email"), clientEmail);
        };

    }

    public static Specification<InquiryEntity> hasClientName(String clientName) {
        return (root, query, cb) -> {
            if (clientName == null || clientName.isBlank()) return null;
            Join<InquiryEntity, ?> clientJoin = getOrCreateJoin(root, "client");
            return cb.like(cb.lower(clientJoin.get("fullName")), "%" + clientName.trim().toLowerCase() + "%");
        };
    }

    public static Specification<InquiryEntity> hasPropertyId(UUID propertyId) {
        return (root, query, cb) -> {
            if (propertyId == null) return null;
            Join<InquiryEntity, ?> propertyJoin = getOrCreateJoin(root, "property");
            return cb.equal(propertyJoin.get("id"), propertyId);
        };
    }

    public static Specification<InquiryEntity> hasPropertyTitle(String propertyTitle) {
        return (root, query, cb) -> {
            if (propertyTitle == null || propertyTitle.isBlank()) return null;
            Join<InquiryEntity, ?> propertyJoin = getOrCreateJoin(root, "property");
            return cb.like(cb.lower(propertyJoin.get("title")), "%" + propertyTitle.trim().toLowerCase() + "%");
        };
    }


    public static Specification<InquiryEntity> hasContactMethod(InquiryType contact) {
        return (
                ((root, query, criteriaBuilder) ->
                        contact == null ?  null
                        : criteriaBuilder.equal(root.get("preferredContactMethod"), contact)
                        )
                );
    }

    private static Specification<InquiryEntity> createTimeBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) {
                return null;
            }
            if (startDate != null && endDate != null) {
                return cb.between(root.get("createdAt"), startDate, endDate);
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
        };
    }

    @SuppressWarnings("unchecked")
    private static Join<InquiryEntity, ?> getOrCreateJoin(Root<InquiryEntity> root, String attributeName) {
        return root.getJoins().stream()
                .filter(join -> attributeName.equals(join.getAttribute().getName()))
                .findFirst()
                .map(join -> (Join<InquiryEntity, ?>) join)
                .orElseGet(() -> root.join(attributeName, JoinType.INNER));
    }

}
