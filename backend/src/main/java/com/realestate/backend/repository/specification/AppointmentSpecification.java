package com.realestate.backend.repository.specification;

import com.realestate.backend.dto.request.AppointmentFilterRequest;
import com.realestate.backend.entity.AppointmentEntity;
import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.AppointmentType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentSpecification {

    public AppointmentSpecification() {}

    public static Specification<AppointmentEntity> withFilter(
            AppointmentFilterRequest filterRequest
    ) {

        if(filterRequest == null) {
            Specification.where((Specification<Object>) null);
        };

        assert filterRequest != null;

        return Specification.where(hasPropertyId(filterRequest.propertyId()))
                .and(hasPropertyTitle(filterRequest.propertyTitle()))
                .and(hasClientId(filterRequest.clientId()))
                .and(hasClientName(filterRequest.clientName()))
                .and(hasClientEmail(filterRequest.clientEmail()))
                .and(hasAgentId(filterRequest.agentId()))
                .and(hasAgentName(filterRequest.agentName()))
                .and(hasAgencyId(filterRequest.agencyId()))
                .and(hasAgencyName(filterRequest.agencyName()))
                .and(hasType(filterRequest.type()))
                .and(hasStatus(filterRequest.status()))
                .and(createTimeBetween(filterRequest.createdAfter(), filterRequest.createdBefore()));

    }

//    HELPER METHODS
    public static Specification<AppointmentEntity> hasPropertyId(UUID propertyId) {
        return (root, query, cb) -> {
            if (propertyId == null) return null;
            Join<AppointmentEntity, ?> propertyJoin = getOrCreateJoin(root, "property");
            return cb.equal(propertyJoin.get("id"), propertyId);
        };
    }

    public static Specification<AppointmentEntity> hasPropertyTitle(String propertyTitle) {
        return (root, query, cb) -> {
            if (propertyTitle == null || propertyTitle.isBlank()) return null;
            Join<AppointmentEntity, ?> propertyJoin = getOrCreateJoin(root, "property");
            return cb.like(cb.lower(propertyJoin.get("title")), "%" + propertyTitle.trim().toLowerCase() + "%");
        };
    }

    public static Specification<AppointmentEntity> hasClientId(UUID clientId) {

        return (root, query, cb) -> {
            if (clientId == null) return null;
            Join<AppointmentEntity, ?> clientJoin = getOrCreateJoin(root, "client");
            return cb.equal(clientJoin.get("id"), clientId);
        };
    }

    public static Specification<AppointmentEntity> hasClientName(String clientName) {
        return (root, query, cb) -> {
            if (clientName == null || clientName.isBlank()) return null;
            Join<AppointmentEntity, ?> clientJoin = getOrCreateJoin(root, "client");
            return cb.like(cb.lower(clientJoin.get("fullName")), "%" + clientName.trim().toLowerCase() + "%");
        };
    }

    public static Specification<AppointmentEntity> hasClientEmail(String clientEmail) {

        return (root, query, cb) -> {
            if (clientEmail == null || clientEmail.isBlank()) return null;
            Join<AppointmentEntity, ?> clientJoin = getOrCreateJoin(root, "client");
            return cb.equal(clientJoin.get("email"), clientEmail);
        };

    }

    public static Specification<AppointmentEntity> hasAgentId(UUID agentId) {

        return (root, query, cb) -> {
            if (agentId == null) return null;
            Join<AppointmentEntity, ?> agentJoin = getOrCreateJoin(root, "agent");
            return cb.equal(agentJoin.get("id"), agentId);
        };
    }

    public static Specification<AppointmentEntity> hasAgentName(String agentName) {

        return (root, query, cb) -> {
            if (agentName == null || agentName.isBlank()) return null;
            Join<AppointmentEntity, ?> agentJoin = getOrCreateJoin(root, "agent");
            return cb.like(cb.lower(agentJoin.get("fullName")), "%" + agentName.trim().toLowerCase() + "%");
        };

    }

    public static Specification<AppointmentEntity> hasType(
            AppointmentType type
    ) {
        return (
                ((root, query, cb) ->
                        type == null ? null
                        : cb.equal(root.get("appointmentType"), type)
                        )
                );
    }

    public static Specification<AppointmentEntity> hasStatus(AppointmentStatus status) {
        return (root, criteriaQuery, criteriaBuilder) ->
                status == null ? null
                        : criteriaBuilder.equal(root.get("status"), status
                );
    }

    public static Specification<AppointmentEntity> hasAgencyId(UUID agencyId) {

        return (root, query, cb) -> {
            if (agencyId == null) return null;
            Join<AppointmentEntity, ?> agencyJoin = getOrCreateJoin(root, "agency");
            return cb.equal(agencyJoin.get("id"), agencyId);
        };

    }

    public static Specification<AppointmentEntity> hasAgencyName(String agencyName) {

        return (root, query, cb) -> {
            if (agencyName == null || agencyName.isBlank()) return null;
            Join<AppointmentEntity, ?> agencyJoin = getOrCreateJoin(root, "agency");
            return cb.like(cb.lower(agencyJoin.get("name")), "%" + agencyName.trim().toLowerCase() + "%");
        };

    }

    private static Specification<AppointmentEntity> createTimeBetween(LocalDateTime createdAfter, LocalDateTime createdBefore) {
        return (root, query, cb) -> {
            if (createdAfter == null && createdBefore == null) {
                return null;
            }
            if (createdAfter != null && createdBefore != null) {
                return cb.between(root.get("createdAt"), createdAfter, createdBefore);
            }
            if (createdAfter != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }

    @SuppressWarnings("unchecked")
    private static Join<AppointmentEntity, ?> getOrCreateJoin(Root<AppointmentEntity> root, String attributeName) {
        return root.getJoins().stream()
                .filter(join -> attributeName.equals(join.getAttribute().getName()))
                .findFirst()
                .map(join -> (Join<AppointmentEntity, ?>) join)
                .orElseGet(() -> root.join(attributeName, JoinType.INNER));
    }


}
