package com.realestate.backend.entity;

import com.realestate.backend.enums.AgencyMediaPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "property_media",
        indexes = {
                @Index(name = "idx_property_media_property", columnList = "property_id"),
                @Index(name = "idx_property_media_media", columnList = "media_id"),
                @Index(name = "idx_property_media_primary", columnList = "is_primary"),
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyMediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "property_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_property_media_property")
    )
    private PropertyEntity  property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "media_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_property_media_media")
    )
    private MediaFileEntity media;

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    Boolean isPrimary = false;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
