package com.realestate.backend.dto.property.request;

import com.realestate.backend.entity.CategoryEntity;
import com.realestate.backend.enums.ListingType;
import com.realestate.backend.utils.validation.ValidFloorChecker;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@ValidFloorChecker
public class CreatePropertyRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    private UUID assignedAgentId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String district;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Listing type is required")
    private ListingType listingType;

    @NotNull(message = "Area is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Area must be greater than 0")
    private BigDecimal area;

    @Min(value = 0, message = "Rooms cannot be negative")
    private Integer rooms;

    @Min(value = 0, message = "Bathrooms cannot be negative")
    private Integer bathrooms;

    @Min(value = 0, message = "Floor cannot be negative")
    private Integer floor;

    @Min(value = 0, message = "Total floors cannot be negative")
    private Integer totalFloors;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private BigDecimal longitude;

}
