package com.realestate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "media_files",
        indexes = {
                @Index(name = "idx_media_files_property_id", columnList = "property_id"),
                @Index(name = "idx_media_files_agency_id", columnList = "agency_id"),
                @Index(name = "idx_media_files_purpose", columnList = "media_purpose")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "property_id",
            foreignKey = @ForeignKey(name = "fk_media_files_property")
    )
    private PropertyEntity property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agency_id",
            foreignKey = @ForeignKey(name = "fk_media_files_agency")
    )
    private AgencyEntity agency;

    @Column(name = "file_url", length = 500, nullable = false)
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "media_purpose", length = 50, nullable = false)
    private String mediaPurpose;

    @Builder.Default
    @Column(name = "is_main", nullable = false)
    private boolean isMain = false;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
