package com.realestate.backend.storage;

import com.realestate.backend.exception.FileStorageException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileValidatorTest {

    private final FileValidator validator =
            new FileValidator();

    @Test
    void validate_validJpeg_acceptsFile() {

        byte[] jpegBytes =
                new byte[] {
                        (byte) 0xFF,
                        (byte) 0xD8,
                        (byte) 0xFF,
                        (byte) 0xE0,
                        0,
                        16,
                        'J',
                        'F',
                        'I',
                        'F'
                };

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "house.jpg",
                        "image/jpeg",
                        jpegBytes
                );

        ValidatedFile result =
                validator.validate(
                        file,
                        MediaUploadPolicy.USER_PROFILE
                );

        assertThat(result.originalName())
                .isEqualTo("house.jpg");

        assertThat(result.mimeType())
                .isEqualTo("image/jpeg");

        assertThat(result.extension())
                .isEqualTo("jpg");
    }

    @Test
    void validate_validPng_acceptsFile() {

        byte[] pngBytes =
                new byte[] {
                        (byte) 0x89,
                        'P',
                        'N',
                        'G',
                        13,
                        10,
                        26,
                        10
                };

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "property.png",
                        "image/png",
                        pngBytes
                );

        ValidatedFile result =
                validator.validate(
                        file,
                        MediaUploadPolicy.USER_PROFILE
                );

        assertThat(result.mimeType())
                .isEqualTo("image/png");

        assertThat(result.extension())
                .isEqualTo("png");
    }

    @Test
    void validateEmptyFile_rejectsFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "empty.jpg",
                        "image/jpeg",
                        new byte[0]
                );

        assertThrows(
                FileStorageException.class,
                () -> validator.validate(
                        file,
                        MediaUploadPolicy.USER_PROFILE
                )
        );
    }

    @Test
    void validateMissingFilename_rejectsFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        null,
                        "image/jpeg",
                        new byte[] {1, 2, 3}
                );

        assertThrows(
                FileStorageException.class,
                () -> validator.validate(
                        file,
                        MediaUploadPolicy.USER_PROFILE
                )
        );
    }

    @Test
    void validatePathTraversalFilename_rejectsFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "../../secret.jpg",
                        "image/jpeg",
                        new byte[] {1, 2, 3}
                );

        assertThrows(
                FileStorageException.class,
                () -> validator.validate(
                        file,
                        MediaUploadPolicy.USER_PROFILE
                )
        );
    }

    @Test
    void validateWindowsPathFilename_rejectsFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "..\\..\\secret.jpg",
                        "image/jpeg",
                        new byte[] {1, 2, 3}
                );

        assertThrows(
                FileStorageException.class,
                () -> validator.validate(
                        file,
                        MediaUploadPolicy.USER_PROFILE
                )
        );
    }

    @Test
    void validateFilenameTooLong_rejectsFile() {

        String filename =
                "a".repeat(256) + ".jpg";

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        filename,
                        "image/jpeg",
                        new byte[] {1, 2, 3}
                );

        assertThrows(
                FileStorageException.class,
                () -> validator.validate(
                        file,
                        MediaUploadPolicy.USER_PROFILE
                )
        );
    }

    @Test
    void validateUnsupportedExtension_rejectsFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "house.exe",
                        "image/jpeg",
                        new byte[] {
                                (byte) 0xFF,
                                (byte) 0xD8,
                                (byte) 0xFF
                        }
                );

        assertThrows(
                FileStorageException.class,
                () -> validator.validate(
                        file,
                        MediaUploadPolicy.USER_PROFILE
                )
        );
    }

    @Test
    void validateContentTypeSpoofing_rejectsFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "house.jpg",
                        "image/jpeg",
                        "This is not an image."
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

        assertThrows(
                FileStorageException.class,
                () -> validator.validate(
                        file,
                        MediaUploadPolicy.USER_PROFILE
                )
        );
    }

    @Test
    void validateImageAsVideo_rejectsFile() {

        byte[] jpegBytes =
                new byte[] {
                        (byte) 0xFF,
                        (byte) 0xD8,
                        (byte) 0xFF
                };

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "video.mp4",
                        "video/mp4",
                        jpegBytes
                );

        assertThrows(
                FileStorageException.class,
                () -> validator.validate(
                        file,
                        MediaUploadPolicy.USER_PROFILE
                )
        );
    }
}