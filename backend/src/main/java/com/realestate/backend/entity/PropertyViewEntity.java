package com.realestate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "property_views",
        indexes = {
                @Index(
                        name = "idx_property_views_property_user_viewed_at",
                        columnList = "property_id, user_id, viewed_at"
                ),
                @Index(
                        name = "idx_property_views_property_id",
                        columnList = "property_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyViewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "property_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_property_views_property_id")
    )
    private PropertyEntity property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "viewer_id",
            foreignKey = @ForeignKey(name = "fk_property_views_user_id")
    )
    private UserEntity viewer;

    @CreationTimestamp
    @Column(name = "viewed_at", nullable = false, updatable = false)
    private LocalDateTime viewedAt;
}
