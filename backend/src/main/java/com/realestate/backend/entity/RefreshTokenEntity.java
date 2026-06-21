package com.realestate.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_refresh_tokens_token", columnNames = "token_hash")
        },
        indexes = {
                @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
                @Index(name = "idx_refresh_tokens_revoked", columnList = "revoked"),
                @Index(name = "idx_refresh_tokens_expiry_date", columnList = "expiry_date"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "id", nullable = false, updatable = false)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(
                name = "user_id",
                nullable = false,
                foreignKey = @ForeignKey(name = "fk_refresh_tokens_user_id")
        )
        private UserEntity user;

        @Column(name = "token_hash", length = 500, nullable = false)
        private String tokenHash;

        @Column(name = "expiry_date", nullable = false)
        private LocalDateTime expiryDate;

        @Builder.Default
        @Column(name = "revoked", nullable = false)
        private boolean revoked = false;

        @Column(name = "revoked_at")
        private LocalDateTime revokedAt;

        @Column(name = "replaced_by_token_hash")
        private String replacedByTokenHash;

        @Column(name = "ip_address", length = 100)
        private String ipAddress;

        @Column(name = "user_agent", columnDefinition = "TEXT")
        private String userAgent;

        @CreationTimestamp
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        public void revoke(String replacedByTokenHash) {
                this.revoked = true;
                this.revokedAt = LocalDateTime.now();
                this.replacedByTokenHash = replacedByTokenHash;
        }
        
        public boolean isExpired() {
                return LocalDateTime.now().isAfter(this.expiryDate);
        }

}
