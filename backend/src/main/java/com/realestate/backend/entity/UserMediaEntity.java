package com.realestate.backend.entity;

import com.realestate.backend.enums.AgencyMediaPurpose;
import com.realestate.backend.enums.UserMediaPurpose;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_media",
        indexes = {
                @Index(name = "idx_user_media_user", columnList = "user_id"),
                @Index(name = "idx_user_media_media", columnList = "media_id"),
                @Index(name = "idx_user_media_purpose", columnList = "purpose")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_media_user")
    )
    private UserEntity  user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "media_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_media_media")
    )
    private MediaFileEntity mediaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 30)
    private UserMediaPurpose purpose;

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    Boolean isPrimary = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
