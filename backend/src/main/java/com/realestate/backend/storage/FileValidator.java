package com.realestate.backend.storage;

import com.realestate.backend.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class FileValidator {

    private static final int MAX_FILENAME_LENGTH = 255;

    private static final Pattern CONTROL_CHARACTERS =
            Pattern.compile("[\\p{Cntrl}]");

    private final Tika tika = new Tika();

    public ValidatedFile validate(
            MultipartFile file,
            MediaUploadPolicy policy
    ) {

        validatePresence(file);
        validateFilename(file);

        String originalName =
                sanitizeOriginalFilename(
                        file.getOriginalFilename()
                );

        validateFilenameLength(originalName);

        if (file.isEmpty()) {
            throw new FileStorageException(
                    "File must not be empty.",
                    HttpStatus.BAD_REQUEST
            );
        }

        validateSize(file, policy);

        String detectedMimeType = detectMimeType(file);

        validateMimeType(
                detectedMimeType,
                policy
        );

        String extension =
                FilenameUtils.getExtension(originalName)
                        .toLowerCase();

        validateExtension(
                extension,
                detectedMimeType,
                policy
        );

        return new ValidatedFile(
                originalName,
                detectedMimeType,
                policy.extensionFor(detectedMimeType),
                file.getSize(),
                policy
        );
    }

    private void validatePresence(MultipartFile file) {

        if (file == null) {
            throw new FileStorageException(
                    "File is required.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateFilename(MultipartFile file) {

        String originalFilename =
                file.getOriginalFilename();

        if (originalFilename == null
                || originalFilename.isBlank()) {

            throw new FileStorageException(
                    "Filename is required.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (originalFilename.contains("/")
                || originalFilename.contains("\\")) {

            throw new FileStorageException(
                    "Filename must not contain path separators.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (originalFilename.contains("\0")) {

            throw new FileStorageException(
                    "Filename contains invalid characters.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private String sanitizeOriginalFilename(
            String originalFilename
    ) {

        String normalized =
                Normalizer.normalize(
                        originalFilename.trim(),
                        Normalizer.Form.NFC
                );

        normalized =
                CONTROL_CHARACTERS
                        .matcher(normalized)
                        .replaceAll("");

        normalized =
                FilenameUtils.getName(normalized);

        if (normalized.isBlank()) {
            throw new FileStorageException(
                    "Filename is invalid.",
                    HttpStatus.BAD_REQUEST
            );
        }

        return normalized;
    }

    private void validateFilenameLength(
            String filename
    ) {

        if (filename.length() > MAX_FILENAME_LENGTH) {

            throw new FileStorageException(
                    "Filename must not exceed "
                            + MAX_FILENAME_LENGTH
                            + " characters.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateSize(
            MultipartFile file,
            MediaUploadPolicy policy
    ) {

        if (file.getSize() > policy.getMaxFileSize()) {

            throw new FileStorageException(
                    "File must not exceed "
                            + formatBytes(policy.getMaxFileSize())
                            + ".",
                    HttpStatus.CONTENT_TOO_LARGE
            );
        }
    }

    private String detectMimeType(
            MultipartFile file
    ) {

        try {

            /*
             * Tika examines the actual file content rather than
             * trusting the Content-Type sent by the client.
             */
            return tika.detect(file.getInputStream());

        } catch (IOException e) {

            throw new FileStorageException(
                    "Could not determine file type.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateMimeType(
            String mimeType,
            MediaUploadPolicy policy
    ) {

        if (!policy.isMimeTypeAllowed(mimeType)) {

            throw new FileStorageException(
                    "Unsupported file type: " + mimeType,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateExtension(
            String extension,
            String mimeType,
            MediaUploadPolicy policy
    ) {

        if (extension.isBlank()) {

            throw new FileStorageException(
                    "File extension is required.",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (!policy.isExtensionAllowed(
                mimeType,
                extension
        )) {

            throw new FileStorageException(
                    "File extension does not match "
                            + "the file content.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private String formatBytes(long bytes) {

        long megabytes =
                bytes / (1024 * 1024);

        return megabytes + " MB";
    }
}