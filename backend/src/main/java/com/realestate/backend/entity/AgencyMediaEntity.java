package com.realestate.backend.entity;

import com.realestate.backend.enums.AgencyMediaPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "agency_media",
        indexes = {
                @Index(name = "idx_agency_media_agency", columnList = "agency_id"),
                @Index(name = "idx_agency_media_media", columnList = "media_id"),
                @Index(name = "idx_agency_media_purpose", columnList = "purpose")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyMediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agency_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_agency_media_agency")
    )
    private AgencyEntity  agency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "media_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_agency_media_media")
    )
    private MediaFileEntity mediaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 30)
    private AgencyMediaPurpose purpose;

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    Boolean isPrimary = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
