package com.realestate.backend.storage;

import com.realestate.backend.config.MinioProperties;
import com.realestate.backend.exception.StorageException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public UploadedFile upload(
            MultipartFile file,
            ValidatedFile validatedFile
    ) {

        String storageKey =
                generateStorageKey(validatedFile);

        try {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(storageKey)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(
                                    validatedFile.mimeType()
                            )
                            .build()
            );

            log.atInfo()
                    .setMessage(
                            "File uploaded successfully."
                    )
                    .addKeyValue(
                            "storageKey",
                            storageKey
                    )
                    .addKeyValue(
                            "sizeInBytes",
                            validatedFile.fileSize()
                    )
                    .addKeyValue(
                            "mimeType",
                            validatedFile.mimeType()
                    )
                    .log();

            return new UploadedFile(
                    storageKey,
                    buildFileUrl(storageKey),
                    validatedFile.originalName(),
                    validatedFile.mimeType(),
                    validatedFile.extension(),
                    validatedFile.fileSize()
            );

        } catch (Exception e) {

            log.atError()
                    .setMessage("File upload failed.")
                    .addKeyValue(
                            "originalFileName",
                            validatedFile.originalName()
                    )
                    .addKeyValue(
                            "mimeType",
                            validatedFile.mimeType()
                    )
                    .addKeyValue(
                            "storageKey",
                            storageKey
                    )
                    .setCause(e)
                    .log();

            throw new StorageException(
                    "Failed to upload file to storage.",
                    e
            );
        }
    }

    @Override
    public void delete(String storageKey) {

        try {

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(storageKey)
                            .build()
            );

            log.atInfo()
                    .setMessage(
                            "File deleted successfully."
                    )
                    .addKeyValue(
                            "storageKey",
                            storageKey
                    )
                    .log();

        } catch (Exception e) {

            log.atError()
                    .setMessage("File deletion failed.")
                    .addKeyValue(
                            "storageKey",
                            storageKey
                    )
                    .setCause(e)
                    .log();

            throw new StorageException(
                    "Failed to delete file from storage.",
                    e
            );
        }
    }

    @PostConstruct
    public void initializeBucket() {

        try {

            boolean exists =
                    minioClient.bucketExists(
                            BucketExistsArgs.builder()
                                    .bucket(
                                            properties.getBucket()
                                    )
                                    .build()
                    );

            if (!exists) {

                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(
                                        properties.getBucket()
                                )
                                .build()
                );

                log.atInfo()
                        .setMessage(
                                "MinIO bucket created."
                        )
                        .addKeyValue(
                                "bucket",
                                properties.getBucket()
                        )
                        .log();

            } else {

                log.atInfo()
                        .setMessage(
                                "MinIO bucket already exists."
                        )
                        .addKeyValue(
                                "bucket",
                                properties.getBucket()
                        )
                        .log();
            }

        } catch (Exception e) {

            log.atError()
                    .setMessage(
                            "MinIO bucket initialization failed."
                    )
                    .addKeyValue(
                            "bucketName",
                            properties.getBucket()
                    )
                    .setCause(e)
                    .log();

            throw new StorageException(
                    "Failed to initialize MinIO bucket.",
                    e
            );
        }
    }

    private String generateStorageKey(
            ValidatedFile validatedFile
    ) {

        String folder =
                validatedFile.policy()
                        .resolveFolder(
                                validatedFile.mimeType()
                        )
                        .getFolderName();

        return folder
                + "/"
                + UUID.randomUUID()
                + "."
                + validatedFile.extension();
    }

    private String buildFileUrl(
            String storageKey
    ) {

        return properties.getEndpoint()
                + "/"
                + properties.getBucket()
                + "/"
                + storageKey;
    }
}