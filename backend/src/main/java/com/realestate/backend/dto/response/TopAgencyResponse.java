package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonPropertyOrder(
        {
                "id",
                "name",
                "city",
                "logoUrl",
                "listingCount",
                "averageRating",
                "aggregateViews"
        }
)
public class TopAgencyResponse {

    private UUID id;
    private String name;
    private String city;
    private String logoUrl;
    private Long listingCount;
    private Long aggregateViews;
    private Double averageRating;

    public TopAgencyResponse(UUID id, String name, String city, String logoUrl, Long listingCount, Long aggregateViews, Double averageRating) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
        this.city = city;
        this.listingCount = listingCount;
        this.aggregateViews = aggregateViews;
        this.averageRating = averageRating;
    }

    public TopAgencyResponse(UUID id, String name, String city, String logoUrl, Long listingCount, int aggregateViews, Double averageRating) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.logoUrl = logoUrl;
        this.listingCount = listingCount;
        this.aggregateViews = (long) aggregateViews;
        this.averageRating = averageRating;
    }
}
