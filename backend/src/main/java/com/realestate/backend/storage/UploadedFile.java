package com.realestate.backend.storage;

public record UploadedFile(

        String storageKey,
        String url,
        String originalName,
        String mimeType,
        Long fileSize

) {
}