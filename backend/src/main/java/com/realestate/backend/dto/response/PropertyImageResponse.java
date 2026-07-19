package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyImageResponse {
    private UUID id;
    private String fileUrl;
    private boolean isMain;
    private int sortOrder;
}
