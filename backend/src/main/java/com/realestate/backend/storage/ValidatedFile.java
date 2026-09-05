package com.realestate.backend.storage;

public record ValidatedFile(
        String originalName,
        String mimeType,
        String extension,
        Long fileSize,
        MediaUploadPolicy policy
) {
}
