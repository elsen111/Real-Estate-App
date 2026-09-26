package com.realestate.backend.entity;

import com.realestate.backend.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "agency_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_agency_members_agency_user",
                        columnNames = {"agency_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_agency_members_user_id", columnList = "user_id"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agency_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_agency_members_agency_id")
    )
    private AgencyEntity agency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_agency_members_user_id")
    )
    private UserEntity user;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "added_by",
            foreignKey = @ForeignKey(name = "fk_agency_members_added_by")
    )
    private UserEntity addedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "removed_by",
            foreignKey = @ForeignKey(name = "fk_agency_members_removed_by")
    )
    private UserEntity removedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
