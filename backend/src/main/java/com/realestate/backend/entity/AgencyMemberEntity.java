package com.realestate.backend.entity;

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
                @Index(name = "idx_agency_members_member_type", columnList = "member_type")
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

        @Column(name = "position", length = 100)
        private String position;

        @Column(name = "member_type", length = 30, nullable = false)
        private String memberType;

        @Builder.Default
        @Column(name = "active", nullable = false)
        private boolean active = true;

        @CreationTimestamp
        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

}
