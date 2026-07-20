package com.realestate.backend.entity;

import com.realestate.backend.enums.ReviewStatus;
import com.realestate.backend.enums.ReviewTargetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_reviews_reviewer_id", columnList = "reviewer_id"),
                @Index(name = "idx_reviews_agency_id", columnList = "agency_id"),
                @Index(name = "idx_reviews_property_id", columnList = "property_id"),
                @Index(name = "idx_reviews_rating", columnList = "rating"),
                @Index(name = "idx_reviews_status", columnList = "status"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "reviewer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_reviews_reviewer_id")
    )
    private UserEntity reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agency_id",
            foreignKey = @ForeignKey(name = "fk_reviews_agency_id")
    )
    private AgencyEntity agency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "property_id",
            foreignKey = @ForeignKey(name = "fk_reviews_property_id")
    )
    private PropertyEntity property;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "target", nullable = false)
    private ReviewTargetType target;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private ReviewStatus status = ReviewStatus.APPROVED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
