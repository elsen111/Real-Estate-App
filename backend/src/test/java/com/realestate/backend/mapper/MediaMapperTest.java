package com.realestate.backend.mapper;

import com.realestate.backend.entity.MediaFileEntity;
import com.realestate.backend.enums.MediaType;
import com.realestate.backend.exception.StorageException;
import com.realestate.backend.storage.UploadedFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

public class MediaMapperTest {

    private MediaMapper mediaMapper;

    @BeforeEach
    void setup() {
        mediaMapper = Mappers.getMapper(MediaMapper.class);
    }

    @Test
    void shouldMapImageFile() {

        UploadedFile uploadedFile = createImageUploadedFile();

        MediaFileEntity entity = mediaMapper.toEntity(uploadedFile);

        assertNotNull(entity);

        assertEquals(uploadedFile.storageKey(), entity.getStorageKey());
        assertEquals(uploadedFile.fileUrl(), entity.getFileUrl());
        assertEquals(uploadedFile.originalName(), entity.getOriginalName());
        assertEquals(uploadedFile.mimeType(), entity.getMimeType());
        assertEquals(uploadedFile.fileSize(), entity.getFileSize());

        assertEquals(MediaType.IMAGE, entity.getMediaType());

        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void shouldMapVideoFile() {

        UploadedFile uploadedFile = createVideoUploadedFile();

        MediaFileEntity entity = mediaMapper.toEntity(uploadedFile);

        assertEquals(MediaType.VIDEO, entity.getMediaType());
    }

    @Test
    void shouldThrowWhenMimeTypeIsNull() {

        UploadedFile uploadedFile = new UploadedFile(
                "storage-key",
                "https://cdn.test/file",
                "photo.png",
                null,
                100L
        );

        StorageException exception = assertThrows(
                StorageException.class,
                () -> mediaMapper.toEntity(uploadedFile)
        );

        assertEquals("Media type is missing.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenMimeTypeIsUnsupported() {

        UploadedFile uploadedFile = new UploadedFile(
                "storage-key",
                "https://cdn.test/file.pdf",
                "document.pdf",
                "application/pdf",
                100L
        );

        StorageException exception = assertThrows(
                StorageException.class,
                () -> mediaMapper.toEntity(uploadedFile)
        );

        assertEquals(
                "Unsupported media type: application/pdf",
                exception.getMessage()
        );
    }

    @Test
    void shouldResolveMediaType() {

        assertEquals(
                MediaType.IMAGE,
                mediaMapper.resolveMediaType("image/png")
        );

        assertEquals(
                MediaType.VIDEO,
                mediaMapper.resolveMediaType("video/mp4")
        );
    }

    // HELPERS
    private UploadedFile createImageUploadedFile() {

        return new UploadedFile(
                "image-storage-key",
                "https://cdn.test/image.png",
                "image.png",
                "image/png",
                100L
        );
    }

    private UploadedFile createVideoUploadedFile() {

        return new UploadedFile(
                "video-storage-key",
                "https://cdn.test/video.mp4",
                "video.mp4",
                "video/mp4",
                100L
        );
    }
}