package com.realestate.backend.entity;

import com.realestate.backend.enums.AppointmentStatus;
import com.realestate.backend.enums.AppointmentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.util.Lazy;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "appointments",
        indexes = {
                @Index(name = "idx_appointments_property_id", columnList = "property_id"),
                @Index(name = "idx_appointments_client_id", columnList = "client_id"),
                @Index(name = "idx_appointments_agent_id", columnList = "agent_id"),
                @Index(name = "idx_appointments_status", columnList = "status"),
                @Index(name = "idx_appointments_preferred_date_time", columnList = "preferred_date_time"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "property_id",
            foreignKey = @ForeignKey(name = "fk_appointments_property_id")
    )
    private PropertyEntity property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "client_id",
            foreignKey = @ForeignKey(name = "fk_appointments_client_id")
    )
    private UserEntity client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_id",
            foreignKey = @ForeignKey(name = "fk_appointments_agent_id")
    )
    private UserEntity agent;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", length = 30, nullable = false)
    AppointmentType appointmentType =  AppointmentType.PROPERTY_VIEWING;

    @Column(name = "preferred_date_time", nullable = false)
    private LocalDateTime preferredDateTime;

    @Column(name = "confirmed_date_time")
    private LocalDateTime confirmedDateTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    AppointmentStatus sttus = AppointmentStatus.PENDING;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "response_note", columnDefinition = "TEXT")
    private String responseNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
