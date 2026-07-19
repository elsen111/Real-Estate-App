package com.realestate.backend.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PropertyMediaResponse(

        UUID id,
        String fileUrl,
        String fileName,
        String fileType,
        Long fileSize,
        boolean isPrimary,
        Integer sortOrder

) {
}
