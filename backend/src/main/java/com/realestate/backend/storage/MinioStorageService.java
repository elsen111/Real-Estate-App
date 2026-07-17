package com.realestate.backend.storage;

import com.realestate.backend.config.MinioProperties;
import com.realestate.backend.exception.FileStorageException;
import com.realestate.backend.exception.StorageException;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public UploadedFile upload(MultipartFile file) {

        try {

            String storageKey = generateStorageKey(file);

            minioClient.putObject(

                    PutObjectArgs.builder()

                            .bucket(properties.getBucket())

                            .object(storageKey)

                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1)

                            .contentType(file.getContentType())

                            .build());

            String url = properties.getEndpoint()
                    + "/"
                    + properties.getBucket()
                    + "/"
                    + storageKey;

            return new UploadedFile(

                    storageKey,

                    url,

                    file.getOriginalFilename(),

                    file.getContentType(),

                    file.getSize()

            );

        }

        catch (Exception e) {

            throw new StorageException(
                    "Cannot upload file.",
                    e);

        }

    }


    @Override
    public void delete(String storageKey) {

        try {

            minioClient.removeObject(

                    RemoveObjectArgs.builder()

                            .bucket(properties.getBucket())

                            .object(storageKey)

                            .build());

        }

        catch (Exception e) {

            throw new StorageException(
                    "Cannot delete file.",
                    e);

        }

    }


    @PostConstruct
    public void initializeBucket() {

        try {

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(properties.getBucket())
                            .build());

            if (!exists) {

                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(properties.getBucket())
                                .build());

            }

        }

        catch (Exception e) {

            throw new StorageException(
                    "Cannot initialize MinIO bucket.",
                    e);

        }

    }

    private String generateStorageKey(MultipartFile file) {

        String extension = FilenameUtils.getExtension(
                file.getOriginalFilename());

        return UUID.randomUUID() + "." + extension;

    }

}
