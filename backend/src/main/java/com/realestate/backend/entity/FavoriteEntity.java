package com.realestate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "favorites",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_favorites_user_property",
                        columnNames = {"user_id", "property_id"}
                )
        },
        indexes = {
                @Index(name = "idx_favorites_property_id", columnList = "property_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_favorites_user_id")
    )
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "property_id",
            foreignKey = @ForeignKey(name = "fk_favorites_property_id")
    )
    private PropertyEntity property;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
