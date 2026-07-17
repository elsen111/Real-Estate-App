package com.realestate.backend.storage;

import com.realestate.backend.enums.MediaType;
import lombok.Builder;
import lombok.Getter;

@Builder
public record UploadedFile(

        String storageKey,
        String fileUrl,
        String originalName,
        String mimeType,
        Long fileSize

) {
}