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

            log.info(
                    "File uploaded successfully: storageKey='{}', folder='{}', size={} bytes",
                    storageKey,
                    folder,
                    file.getSize()
            );

            return new UploadedFile(
                    storageKey,
                    buildFileUrl(storageKey),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize()
            );

        } catch (Exception e) {

            log.error(
                    "Failed to upload file '{}' to folder '{}'",
                    file.getOriginalFilename(),
                    folder,
                    e
            );

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

            log.info(
                    "File deleted successfully: storageKey='{}'",
                    storageKey
            );

        } catch (Exception e) {

            log.error(
                    "Failed to delete file: storageKey='{}'",
                    storageKey,
                    e
            );

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

                log.info("Created MinIO bucket '{}'", properties.getBucket());

            } else {

                log.info("MinIO bucket '{}' already exists", properties.getBucket());

            }

        } catch (Exception e) {

            log.error(
                    "Failed to initialize MinIO bucket '{}'",
                    properties.getBucket(),
                    e
            );

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