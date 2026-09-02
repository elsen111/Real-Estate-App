package com.realestate.backend.storage;

import com.realestate.backend.config.MinioProperties;
import com.realestate.backend.enums.MediaFolder;
import com.realestate.backend.exception.StorageException;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
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
            MediaFolder folder
    ) {

        try {

            String storageKey = generateStorageKey(file, folder);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(storageKey)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );

            log.atInfo()
                    .setMessage("File uploaded successfully.")
                    .addKeyValue("storageKey", storageKey)
                    .addKeyValue("folder", folder)
                    .addKeyValue("sizeInBytes", file.getSize())
                    .log();

            return new UploadedFile(
                    storageKey,
                    buildFileUrl(storageKey),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize()
            );

        } catch (Exception e) {

            log.atError()
                    .setMessage("File upload failed.")
                    .addKeyValue("originalFileName", file.getOriginalFilename())
                    .addKeyValue("folder", folder)
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
                    .setMessage("File deleted successfully.")
                    .addKeyValue("storageKey", storageKey)
                    .log();

        } catch (Exception e) {

            log.atError()
                    .setMessage("File deletion failed.")
                    .addKeyValue("storageKey", storageKey)
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

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(properties.getBucket())
                            .build()
            );

            if (!exists) {

                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(properties.getBucket())
                                .build()
                );

                log.atInfo()
                        .setMessage("MinIO bucket created.")
                        .addKeyValue("bucket", properties.getBucket())
                        .log();

            } else {

                log.atInfo()
                        .setMessage("MinIO bucket already exists.")
                        .addKeyValue("bucket", properties.getBucket())
                        .log();

            }

        } catch (Exception e) {

            log.atInfo()
                    .setMessage("MinIO bucket initialization failed.")
                    .addKeyValue("bucketName", properties.getBucket())
                    .setCause(e)
                    .log();

            throw new StorageException(
                    "Failed to initialize MinIO bucket.",
                    e
            );

        }

    }

    private String generateStorageKey(
            MultipartFile file,
            MediaFolder folder
    ) {

        String extension = FilenameUtils.getExtension(
                file.getOriginalFilename()
        );

        return folder.getFolderName()
                + "/"
                + UUID.randomUUID()
                + "."
                + extension;

    }

    private String buildFileUrl(String storageKey) {

        return properties.getEndpoint()
                + "/"
                + properties.getBucket()
                + "/"
                + storageKey;

    }

}