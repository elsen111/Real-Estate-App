package com.realestate.backend.storage;

import com.realestate.backend.enums.MediaFolder;
import com.realestate.backend.exception.FileStorageException;

import java.util.Map;
import java.util.Set;

public enum MediaUploadPolicy {

    USER_PROFILE(
            5 * 1024 * 1024L,
            Map.of(
                    "image/jpeg", Set.of("jpg", "jpeg"),
                    "image/png", Set.of("png"),
                    "image/webp", Set.of("webp")
            )
    ),

    AGENCY_LOGO(
            5 * 1024 * 1024L,
            Map.of(
                    "image/jpeg", Set.of("jpg", "jpeg"),
                    "image/png", Set.of("png"),
                    "image/webp", Set.of("webp")
            )
    ),

    PROPERTY_MEDIA(
            25 * 1024 * 1024L,
            Map.of(
                    "image/jpeg", Set.of("jpg", "jpeg"),
                    "image/png", Set.of("png"),
                    "image/webp", Set.of("webp"),
                    "video/mp4", Set.of("mp4"),
                    "video/webm", Set.of("webm")
            )
    );

    private final long maxFileSize;
    private final Map<String, Set<String>> allowedMimeTypes;

    MediaUploadPolicy(
            long maxFileSize,
            Map<String, Set<String>> allowedMimeTypes
    ) {
        this.maxFileSize = maxFileSize;
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public Set<String> getAllowedMimeTypes() {
        return allowedMimeTypes.keySet();
    }

    public boolean isMimeTypeAllowed(String mimeType) {
        return allowedMimeTypes.containsKey(mimeType);
    }

    public boolean isExtensionAllowed(
            String mimeType,
            String extension
    ) {
        return allowedMimeTypes
                .getOrDefault(mimeType, Set.of())
                .contains(extension.toLowerCase());
    }

    public String extensionFor(String mimeType) {

        Set<String> extensions = allowedMimeTypes.get(mimeType);

        if (extensions == null || extensions.isEmpty()) {
            throw new FileStorageException(
                    "Unsupported file type."
            );
        }

        return switch (mimeType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "video/mp4" -> "mp4";
            case "video/webm" -> "webm";
            default -> throw new FileStorageException(
                    "Unsupported file type."
            );
        };
    }

    public MediaFolder resolveFolder(String mimeType) {

        return switch (this) {

            case USER_PROFILE ->
                    MediaFolder.USER_PROFILE;

            case AGENCY_LOGO ->
                    MediaFolder.AGENCY_LOGO;

            case PROPERTY_MEDIA -> {

                if (mimeType.startsWith("image/")) {
                    yield MediaFolder.PROPERTY_IMAGE;
                }

                if (mimeType.startsWith("video/")) {
                    yield MediaFolder.PROPERTY_VIDEO;
                }

                throw new FileStorageException(
                        "Unsupported property media type."
                );
            }
        };
    }
}