package com.realestate.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "fileUrl",
        "isMain",
        "sortOrder"
})
public class PropertyImageResponse {
    private UUID id;
    private String fileUrl;
    private boolean isMain;
    private int sortOrder;
}
