package com.realestate.backend.entity;

import com.realestate.backend.enums.ListingType;
import com.realestate.backend.enums.PropertyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "properties",
        indexes = {
                @Index(name = "idx_properties_agency_id", columnList = "agency_id"),
                @Index(name = "idx_properties_category_id", columnList = "category_id"),
                @Index(name = "idx_properties_assigned_agent_id", columnList = "assigned_agent_id"),
                @Index(name = "idx_properties_city", columnList = "city"),
                @Index(name = "idx_properties_district", columnList = "district"),
                @Index(name = "idx_properties_listing_type", columnList = "listing_type"),
                @Index(name = "idx_properties_status", columnList = "status"),
                @Index(name = "idx_properties_price", columnList = "price"),
                @Index(name = "idx_properties_location", columnList = "latitude, longitude"),
                @Index(name = "idx_properties_featured", columnList = "featured"),
                @Index(name = "idx_properties_created_at", columnList = "created_at"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agency_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_properties_agency_id")
    )
    private AgencyEntity agency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_properties_category_id")
    )
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_agent_id",
            foreignKey = @ForeignKey(name = "fk_properties_assigned_agent_id")
    )
    private UserEntity assignedAgent;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "city", length = 100, nullable = false)
    private String city;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "address", nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "listing_type", nullable = false)
    private ListingType listingType;

    @Column(name = "area", precision = 10, scale = 2, nullable = false)
    private BigDecimal area;

    @Column(name = "rooms")
    private Integer rooms;

    @Column(name = "bathrooms")
    private Integer bathrooms;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @Column(name = "latitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal longitude;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private PropertyStatus status = PropertyStatus.ACTIVE;

    @Builder.Default
    @Column(name = "featured", nullable = false)
    private boolean featured = false;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private long viewCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
