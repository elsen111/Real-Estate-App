package com.realestate.backend.storage;

import lombok.Builder;

@Builder
public record UploadedFile(

        String storageKey,
        String fileUrl,
        String originalName,
        String mimeType,
        String extension,
        Long fileSize

) {
}