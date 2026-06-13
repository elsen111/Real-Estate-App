package com.realestate.backend.entity;

import com.realestate.backend.enums.InquiryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "inquiries",
        indexes = {
                @Index(name = "idx_inquiries_property_id", columnList = "property_id"),
                @Index(name = "idx_inquiries_client_id", columnList = "client_id"),
                @Index(name = "idx_inquiries_assigned_agent_id", columnList = "assigned_agent_id"),
                @Index(name = "idx_inquiries_status", columnList = "status"),
                @Index(name = "idx_inquiries_created_at", columnList = "created_at"),
        }

)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "property_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_inquiries_property_id")
    )
    private PropertyEntity property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "client_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_inquiries_client_id")
    )
    private UserEntity client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_agent_id",
            foreignKey = @ForeignKey(name = "fk_inquiries_assigned_agent_id")
    )
    private UserEntity assignedAgent;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "preferred_contact_method", length = 30, nullable = false)
    private String preferredContactMethod;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InquiryStatus status = InquiryStatus.NEW;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
