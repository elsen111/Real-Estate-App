package com.realestate.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class ReviewRequest {

    @NotNull(message = "Rating score is required")
    @Range(min = 1, max = 5, message = "Rating score must be between 1 and 5")
    private Integer rating;

    @Size(min = 5, max = 2000, message = "Comment length should be between 5 and 2000 characters.")
    private String comment;

}
