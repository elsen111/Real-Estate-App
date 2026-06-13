package com.realestate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "password_reset_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_password_reset_tokens_token", columnNames = "token")
        },
        indexes = {
                @Index(name = "idx_password_reset_tokens_user_id", columnList = "user_id"),
                @Index(name = "idx_password_reset_tokens_used", columnList = "used"),
                @Index(name = "idx_password_reset_tokens_expiry_date", columnList = "expiry_date"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_password_reset_tokens_user_id")
    )
    private UserEntity user;

    @Column(name = "token", length = 500, nullable = false)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(name = "used", nullable = false)
    private boolean used = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
