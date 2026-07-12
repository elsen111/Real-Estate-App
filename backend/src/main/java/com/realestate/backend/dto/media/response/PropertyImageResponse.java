package com.realestate.backend.dto.media.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PropertyImageResponse {
    private UUID id;
    private String fileUrl;
    private boolean isMain;
    private int sortOrder;
}
